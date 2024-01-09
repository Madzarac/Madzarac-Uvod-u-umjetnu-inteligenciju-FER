package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class Solution {
	
	static ArrayList<Integer> usedFeatures = new ArrayList<Integer>();
	static ArrayList<String> predictions = new ArrayList<String>();
	static String maxOc;
	
	public static void main(String[] args) {
		
		ArrayList<ArrayList<String>> examplesD = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> parentD = new ArrayList<ArrayList<String>>();
		LinkedHashMap<String, ArrayList<String>> features = new LinkedHashMap<String, ArrayList<String>>();
		ArrayList<String> featuresKeys = new ArrayList<String>();
		
		ArrayList<ArrayList<String>> testSet = new ArrayList<ArrayList<String>>();
		ArrayList<String> testSolutions = new ArrayList<String>();
		
		if(!(args.length == 2 || args.length == 3)) {
			System.out.println("Wrong input");
			
		} else {			
			
			//train set
			List<String> lines = new ArrayList<String>();
			try {
				try (BufferedReader br = new BufferedReader(new FileReader(/*"src/main/resources/" + */args[0]))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				        lines.add(line);
				    }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			boolean first = true;
			String[] arr;
			TreeSet<String> labelsY = new TreeSet<String>();
			for (String line: lines) {
				
				if(first) {
					
					String[] arrF = line.split(",");
					featuresKeys = new ArrayList<String>(Arrays.asList(arrF));
					first = false;
					
					
				} else {
					arr = line.split(",");
					ArrayList<String> example = new ArrayList<String>(Arrays.asList(arr));
					examplesD.add(example);
					ArrayList<String> parent = new ArrayList<String>(Arrays.asList(arr));
					parentD.add(example);
					labelsY.add(arr[arr.length -1]);
				}
			}
			
			//test set
			List<String> lines2 = new ArrayList<String>();
			try {
				try (BufferedReader br = new BufferedReader(new FileReader(/*"src/main/resources/" +*/ args[1]))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				        lines2.add(line);
				    }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			boolean first2 = true;
			String[] arr2;
			TreeSet<String> labelsY2 = new TreeSet<String>();
			for (String line: lines2) {
				
				if(first2) {
					first2 = false;
				} else {
					arr2 = line.split(",");
					ArrayList<String> example2 = new ArrayList<String>(Arrays.asList(arr2));
					example2.remove(arr2.length - 1);
					testSet.add(example2);
					testSolutions.add(arr2[arr2.length -1]);
				}
			}
			
			
			
			
			
			//ading options to features
			for(int i = 0; i < featuresKeys.size(); ++i) {
				ArrayList<String> options = new ArrayList<String>();
				for(int j = 0; j < examplesD.size(); j++) {
					if(!options.contains(examplesD.get(j).get(i))) {
						options.add(examplesD.get(j).get(i));
					}
				}
				features.put(featuresKeys.get(i), options);
			}
		
			
			maxOccurence(examplesD, labelsY);
			
			if(args.length == 2) {
				Node<String> root = id3(examplesD, parentD, features, labelsY);
				printPaths(root, "", 1, root.name);
				for(ArrayList<String> test: testSet) { 
					predict(test, root, features);
				}
				 printPredictions();
				 accuracy(testSolutions);
				 confusionMatrix(testSolutions);
				 
			} else {
				int limit =Integer.parseInt(args[2]);
				Node<String> root = id3LimitedDepth(examplesD, parentD, features, labelsY, limit);
				printPaths(root, "", 1, root.name);
				for(ArrayList<String> test: testSet) { 
					predict(test, root, features);
				}
				 printPredictions();
				 accuracy(testSolutions);
				 confusionMatrix(testSolutions);
			}
			
			
		}	
	}
	
	
	
	
	
	
	private static void maxOccurence(ArrayList<ArrayList<String>> examplesD, TreeSet<String> labelsY) {
		
		//making a map label-0
		TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
		for(String label: labelsY) {
			counter.put(label, 0);
		}
		
		//counting occurrences of labels
		for(ArrayList<String> example : examplesD) {
			String l = example.get(example.size() -1);
			int val = counter.get(l) + 1;
			counter.put(l, val);
		}
		
		//finding label with most occurences
		String maxOccurence = null;
		int maxValue = 0;
		for(Map.Entry<String, Integer> entry: counter.entrySet()) {
			if(entry.getValue() > maxValue) {
				maxValue = entry.getValue();
				maxOccurence = entry.getKey();
			}
		}
		maxOc = maxOccurence;
		
	}






	private static void confusionMatrix(ArrayList<String> testSolutions) {
		TreeSet<String> testSet = new TreeSet<>(testSolutions);
		TreeSet<String> predictionSet = new TreeSet<>(predictions);
	
		
		int matrixSize = Math.max(testSet.size(), predictionSet.size());
		int[][] matrix = new int[matrixSize][matrixSize];
		Arrays.stream(matrix).forEach(a -> Arrays.fill(a, 0));
		for(int i = 0; i < testSolutions.size(); i++) { 
				int row = testSet.headSet(testSolutions.get(i)).size();
				int col = predictionSet.headSet(predictions.get(i)).size();
				matrix[row][col]++;
			
		}
		System.out.println("[CONFUSION_MATRIX]:");
		for (int i = 0; i < matrixSize; i++) {
		    for (int j = 0; j < matrixSize; j++) {
		        System.out.print(matrix[i][j] + " ");
		    }
		    System.out.println(); // move to the next row
		}
		
		
	}






	private static void accuracy(ArrayList<String> testSolutions) {
		int correct = 0;
		for(int i = 0; i < predictions.size(); i++) {
			if(testSolutions.get(i).equals(predictions.get(i))) {
				correct++;
			}
		}
		double accuracy = 1.0 * correct / testSolutions.size();
		DecimalFormat df = new DecimalFormat("0.00000");		
		System.out.println("[ACCURACY]: " + df.format(accuracy));
		
	}






	private static void printPredictions() {
		String s = "";
		for(String prediction: predictions) {
			s += " " + prediction;
		}
		System.out.println("[PREDICTIONS]:" + s);
		
	}




	//goint through tree and making predictions
	private static void predict(ArrayList<String> test, Node<String> node, LinkedHashMap<String, ArrayList<String>> features) {
		
		    
			
			if(node.leaf) {
				predictions.add(node.name);
				
			} else {
				
				int index = 0;
				int i = 0;
				for(String feature : features.keySet()) {
					if(feature.equals(node.name)) {
						index = i;
					}
					i++;
				}
				if(!features.get(node.name).contains(test.get(index))) {
				
					predictions.add(maxOc);
					
				} else {
					Node<String> newNode = node.children.get(test.get(index));
					predict(test, newNode, features);
				}				
			}
			
	}







	private static Node<String> id3(ArrayList<ArrayList<String>> examplesD, ArrayList<ArrayList<String>> parentD,
			       LinkedHashMap<String, ArrayList<String>> features, TreeSet<String> labelsY) {
		
		
		if(examplesD.size() == 0) {
			
			//making a map label-0
			TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
			for(String label: labelsY) {
				counter.put(label, 0);
			}
			
			//counting occurrences of labels
			for(ArrayList<String> example : parentD) {
				String l = example.get(example.size() -1);
				int val = counter.get(l) + 1;
				counter.put(l, val);
			}
			
			
			//finding label with most occurences
			String maxOccurence = null;
			int maxValue = 0;
			for(Map.Entry<String, Integer> entry: counter.entrySet()) {
				if(entry.getValue() > maxValue) {
					maxValue = entry.getValue();
					maxOccurence = entry.getKey();
				}
			}	
			
			return new Node<String>(maxOccurence);
		}
		
		
		//check D = Dx=v or X is empty - makes leaf of most frequent label
		TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
		for(String label: labelsY) {
			counter.put(label, 0);
		}
		//counting occurrences of labels
		for(ArrayList<String> example : examplesD) {
			String l = example.get(example.size() -1);
			int val = counter.get(l) + 1;
			counter.put(l, val);
		}
		//finding label with most occurences
		String maxOccurence = null;
		int maxValue = 0;
		for(Map.Entry<String, Integer> entry: counter.entrySet()) {
			if(entry.getValue() > maxValue) {
				maxValue = entry.getValue();
				maxOccurence = entry.getKey();
			}
		}
		if(counter.containsValue(examplesD.size()) || features.size() == 0) {
			
			return new Node<String>(maxOccurence);
		}
		
		
		//Calculate IG and find most discriminative feature
		String featureName = maxIG(features, examplesD, counter);
		
		
		//map for subtrees
		LinkedHashMap<String, Node<String>> children = new LinkedHashMap<String, Node<String>>();
		//getting X\{x}
		LinkedHashMap<String, ArrayList<String>> newFeatures = new LinkedHashMap<String, ArrayList<String>>();
		int index = 0;
		int i = 0;
		for (Map.Entry<String, ArrayList<String>> entry : features.entrySet()) {
			if(!entry.getKey().equals(featureName)) {
				String key = entry.getKey();
			    ArrayList<String> value = entry.getValue();
			    ArrayList<String> newValue = new ArrayList<>(value);
			    newFeatures.put(key, newValue);
			} else {
				index = i;
			}
			i++;
		}
		
		
		//for v ∈ V (x)
		for(Map.Entry<String, ArrayList<String>> feature : features.entrySet()) {
			if(feature.getKey().equals(featureName)) {
				for(int j = 0; j < feature.getValue().size(); j++) {
					
					String featureType = feature.getValue().get(j);
					ArrayList<ArrayList<String>> newD = new ArrayList<ArrayList<String>>(); 
					for (ArrayList<String> innerList : examplesD) { 
					    if (innerList.size() > index && featureType.equals(innerList.get(index))) { 
					        ArrayList<String> newInnerList = new ArrayList<>(innerList); 
					        newD.add(newInnerList); 
					    }
					}
					usedFeatures.add(index);
					Node<String> node = id3(newD,examplesD,features,labelsY);
					usedFeatures.remove(usedFeatures.indexOf(index));
					children.put(featureType, node);
				}				
			}
		}
				
		return new Node<String>(featureName, children);
	}





	private static String maxIG(LinkedHashMap<String, ArrayList<String>> features, ArrayList<ArrayList<String>> examplesD, TreeMap<String, Integer> counter) {
		
		//Entropy of the initial dataset D
		double nodeE = 0;
		for(Map.Entry<String, Integer> entry: counter.entrySet()) {
			double all = examplesD.size();
			nodeE = nodeE -((entry.getValue() / all) * log2(entry.getValue() / all));
		}
		
		
		TreeMap<String, Double> iG = new TreeMap<String, Double>();
		int i = 0;
		for(Map.Entry<String, ArrayList<String>> feature: features.entrySet()) {
			if(i < features.size() -1 && !usedFeatures.contains(i)) {
				TreeMap<String, HashMap<String, Double>> branchChoice = new TreeMap<String, HashMap<String, Double>>();
				HashMap<String, Double> labelVal = new HashMap<String, Double>();
				for(String label : counter.keySet()) {
					labelVal.put(label, 0.0);
				}
				for(String fOption: feature.getValue()) {
					branchChoice.put(fOption, labelVal);
				}
				for(ArrayList<String> example: examplesD) {
					    HashMap<String, Double> val = new HashMap<String, Double>();
					    for(Map.Entry<String, Double> entry: branchChoice.get(example.get(i)).entrySet()) {
					    	val.put(entry.getKey(), entry.getValue());
					    }
					    val.put(example.get(example.size() - 1), val.get(example.get(example.size() - 1)) + 1.0);
						branchChoice.put(example.get(i), val);
				}
				double en = nodeE;
				for(Map.Entry<String, HashMap<String, Double>> bc: branchChoice.entrySet()) {
					int bTimes = 0;
					for(Map.Entry<String, Double> entry: bc.getValue().entrySet()) {
						bTimes += entry.getValue();
					}
					double eOption = 0.0;
					for(Map.Entry<String, Double> entry: bc.getValue().entrySet()) {
						if(entry.getValue() == 0.0) {
							eOption = 0;
						} else {
							eOption = eOption -((entry.getValue() / bTimes) * log2(entry.getValue() / bTimes));
						}
						
					}
					double all = examplesD.size();
					en = en - bTimes / all * eOption;
					
				}
				iG.put(feature.getKey(), en);
			}
			i++;
		
		}
		double maxVal = 0.0;
		String maxKey = null;
		for(Map.Entry<String, Double> entry: iG.entrySet()) {
			if(entry.getValue() > maxVal) {
				maxVal = entry.getValue();
				maxKey = entry.getKey();
			}
		}
		System.out.println(iG);
		return maxKey;
	}
	
	
	
	//calculate log2(N)
	public static double log2(double N) { 
        return Math.log(N) / Math.log(2);
    }


	
	public static void printPaths(Node<String> root, String path, int level, String pickedVal) {
	    // If the root node is null, there is nothing to print
	    if (root == null) {
	        return;
	    }

	    // Append the current node's name to the path
	    if(path.equals("")) {
	    	System.out.println("[BRANCHES]:");
	    	path += level + ":" + root.name;
	    } else if (root.leaf) {
	    	path += "=" + pickedVal + " " + root.name;
	    } else {
	    	path += "=" + pickedVal + " " + level + ":" + root.name;
	    }  

	    // If the current node is a leaf node, print the path
	    if (root.leaf) {
	        System.out.println(path);
	    } else {
	        // Recursively call printPaths for each child node
	        for (Entry<String, Node<String>> child : root.children.entrySet()) {
	            printPaths(child.getValue(), path, level + 1, child.getKey());
	        }
	    }
	}
	
	
	private static Node<String> id3LimitedDepth(ArrayList<ArrayList<String>> examplesD, ArrayList<ArrayList<String>> parentD,
		                                        LinkedHashMap<String, ArrayList<String>> features, TreeSet<String> labelsY, int limit) {
	
		if(limit == 0) {
			//making a map label-0
			TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
			for(String label: labelsY) {
				counter.put(label, 0);
			}
			
			//counting occurrences of labels
			if(examplesD.size() > 0) {
				for(ArrayList<String> example : examplesD) {
					String l = example.get(example.size() -1);
					int val = counter.get(l) + 1;
					counter.put(l, val);
				}
			} else {
				for(ArrayList<String> example : parentD) {
					String l = example.get(example.size() -1);
					int val = counter.get(l) + 1;
					counter.put(l, val);
				}
			}
			
			
			
			//finding label with most occurences
			String maxOccurence = null;
			int maxValue = 0;
			for(Map.Entry<String, Integer> entry: counter.entrySet()) {
				if(entry.getValue() > maxValue) {
					maxValue = entry.getValue();
					maxOccurence = entry.getKey();
				}
			}	
			
			return new Node<String>(maxOccurence);
		}
		
		if(examplesD.size() == 0) {
			
			//making a map label-0
			TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
			for(String label: labelsY) {
				counter.put(label, 0);
			}
			
			//counting occurrences of labels
			for(ArrayList<String> example : parentD) {
				String l = example.get(example.size() -1);
				int val = counter.get(l) + 1;
				counter.put(l, val);
			}
			
			
			//finding label with most occurences
			String maxOccurence = null;
			int maxValue = 0;
			for(Map.Entry<String, Integer> entry: counter.entrySet()) {
				if(entry.getValue() > maxValue) {
					maxValue = entry.getValue();
					maxOccurence = entry.getKey();
				}
			}	
			
			return new Node<String>(maxOccurence);
		}
		
		
		//check D = Dx=v or X is empty - makes leaf of most frequent label
		TreeMap<String, Integer> counter = new TreeMap<String, Integer>();
		for(String label: labelsY) {
			counter.put(label, 0);
		}
		//counting occurrences of labels
		for(ArrayList<String> example : examplesD) {
			String l = example.get(example.size() -1);
			int val = counter.get(l) + 1;
			counter.put(l, val);
		}
		//finding label with most occurences
		String maxOccurence = null;
		int maxValue = 0;
		for(Map.Entry<String, Integer> entry: counter.entrySet()) {
			if(entry.getValue() > maxValue) {
				maxValue = entry.getValue();
				maxOccurence = entry.getKey();
			}
		}
		if(counter.containsValue(examplesD.size()) || features.size() == 0) {
			
			return new Node<String>(maxOccurence);
		}
		
		
		//Calculate IG and find most discriminative feature
		String featureName = maxIG(features, examplesD, counter);
		
		
		//map for subtrees
		LinkedHashMap<String, Node<String>> children = new LinkedHashMap<String, Node<String>>();
		//getting X\{x}
		LinkedHashMap<String, ArrayList<String>> newFeatures = new LinkedHashMap<String, ArrayList<String>>();
		int index = 0;
		int i = 0;
		for (Map.Entry<String, ArrayList<String>> entry : features.entrySet()) {
			if(!entry.getKey().equals(featureName)) {
				String key = entry.getKey();
			    ArrayList<String> value = entry.getValue();
			    ArrayList<String> newValue = new ArrayList<>(value);
			    newFeatures.put(key, newValue);
			} else {
				index = i;
			}
			i++;
		}
		
		
		//for v ∈ V (x)
		for(Map.Entry<String, ArrayList<String>> feature : features.entrySet()) {
			if(feature.getKey().equals(featureName)) {
				for(int j = 0; j < feature.getValue().size(); j++) {
					
					String featureType = feature.getValue().get(j);
					ArrayList<ArrayList<String>> newD = new ArrayList<ArrayList<String>>(); 
					for (ArrayList<String> innerList : examplesD) { 
					    if (innerList.size() > index && featureType.equals(innerList.get(index))) { 
					        ArrayList<String> newInnerList = new ArrayList<>(innerList); 
					        newD.add(newInnerList); 
					    }
					}
					usedFeatures.add(index);
					Node<String> node = id3LimitedDepth(newD,examplesD,features,labelsY, limit - 1);
					usedFeatures.remove(usedFeatures.indexOf(index));
					children.put(featureType, node);
				}				
			}
		}
				
		return new Node<String>(featureName, children);
}





	public static class Node<V> {
		
		private String name;
		private boolean leaf;
		private Map<String, Node<String>> children;
		
		
		public Node(String name, Map<String, Node<String>> children) {
			this.name = name;
			this.children = children;
			this.leaf = false;
		}
		
		public Node(String name) {
			this.name = name;
			this.leaf = true;
			this.children = null;
		}
		
		public String getName() {
			return this.name;
		}
		
		@Override
		public String toString() {
		    return toStringHelper("", true);
		}

		private String toStringHelper(String prefix, boolean isTail) {
		    StringBuilder sb = new StringBuilder();
		    sb.append(prefix).append(isTail ? "└── " : "├── ").append(name).append("\n");

		    if (!leaf && children != null) {
		        int i = 1;
		        for (Map.Entry<String, Node<String>> childEntry : children.entrySet()) {
		            boolean isChildTail = i++ == children.size();
		            sb.append(childEntry.getValue().toStringHelper(prefix + (isTail ? "    " : "│   "), isChildTail));
		        }
		    }

		    return sb.toString();
		}

		
	}

}
