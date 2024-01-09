package ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Solution {
	
	static String startState = null;
	static ArrayList<String> goalStates = new ArrayList<String>();
	static HashMap<String, TreeMap<String, Double>> transitions = new HashMap<String, TreeMap<String, Double>>();
	static TreeMap<String, Double> heuristics = new TreeMap<String, Double>();
	static String h = null;
	
	public static void main(String[] args) {
		
		boolean startFound = false;
		boolean goalFound = false;
		
		
		String alg = null;
		String ss = null;
		String hCheck = null;
		
		//provjeravam koje su zastavice postavljene 
		if(args[0].equals("--alg")) {
			alg = args[1];
		} else if(args[0].equals("--ss")) {
			ss = args[1];
		} else if(args[0].equals("--h")) {
			h = args[1];
		}
		
		if(args[2].equals("--alg")) {
			alg = args[3];
		} else if(args[2].equals("--ss")) {
			ss = args[3];
		} else if(args[2].equals("--h")) {
			h = args[3];
		}

		if(args.length == 6) {
			if(args[4].equals("--alg")) {
				alg = args[5];
			} else if(args[4].equals("--ss")) {
				ss = args[5];
			} else if(args[4].equals("--h")) {
				h = args[5];
			}
		}
				
		//ucitavam podatke iz zadane datoteke
		List<String> lines = null;
		try {
			lines = Files.readAllLines(
					Paths.get(ss), 
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (String line: lines) {
		
			if(!line.startsWith("#")) {
				
				if(startFound && goalFound) {
					
					String[] separatedLine = line.split(" ");
					String keyState = separatedLine[0].replace(":","");
					TreeMap<String, Double> transitionStates = new TreeMap<String, Double>();
					for(int i = 1; i < separatedLine.length; i++) {
						String[] stateAndCost = separatedLine[i].split(",");
						transitionStates.put(stateAndCost[0], Double.parseDouble(stateAndCost[1]));
					}
					transitions.put(keyState, transitionStates);
					
				} else if(!startFound) {
					startState = line.trim();
					startFound = true;
					
				} else {
					String[] g = line.split(" ");
					for(int i = 0; i < g.length; i++) {
						goalStates.add(g[i]);
					}
					goalFound = true;
				}		
			}			
		}
		
		lines.clear();
		//ucitavam heuristiku ako je zadana
		if(args.length == 6 || args.length == 5) {
			try {
				lines = Files.readAllLines(
						Paths.get(h), 
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (String line: lines) {
				
				if(!line.startsWith("#")) {
					String[] separatedLine = line.split(": ");
					heuristics.put(separatedLine[0], Double.parseDouble(separatedLine[1]));
				}
			}
		}
		
		//odabirem koji algoritam/provjera je zadan
		if(args.length == 5) {
			if(args[4].equals("--check-optimistic")) {			
				checkOptimistic();
				
			} else if(args[4].equals("--check-consistent")) {				
				checkConsistent();
			}	
		} else if(alg.equals("bfs")) {
			breadthFirstSearch();
		} else if (alg.equals("ucs")) {
			uniformCostSearch();
		} else if (alg.equals("astar")) {
			aStar();
		}
		
		
	}
	
	//BFS algoritam
	//dodajem pocetno stanje u red te potom ulazim u petlju
	//u petlji se vrtim dok ne dodem do konacnog stanja ili red ne ostane prazan (sto bi znacilo daa je nemoguce od pocetnog doci do zavrsnog stanja)
	//pri svakom prolasku kroz perlju vadim po jedno stanje iz reda, provjeravam je li ono zavrsno stanje, ukoliko nije dodajem svu njegovu djecu u queue 
	//radi smanjenje kompleksnosti stanja koja izvadim iz reda upisujem u set visited te ukoliko ponovo naidem na njih kao djecu trenutnog stanja više ih ne dodajem u visited
	private static void breadthFirstSearch() {
		
		Queue<AlgorithmNode> open = new LinkedList<AlgorithmNode>();
		AlgorithmNode first = new AlgorithmNode(startState, 0.0, null);
		AlgorithmNode last = null;
		HashSet<String> visited = new HashSet<String>();
		
		open.add(first);
		boolean found = false;
		int i = 0;
		while(open.peek()!= null && !found) {
			
				
			AlgorithmNode n = open.poll();
			visited.add(n.getName());
			i++;
			
			if(goalStates.contains(n.getName())) {
				found = true;
				last = n;
			} else {
				TreeMap<String, Double> children = transitions.get(n.getName());
				for(Map.Entry<String,Double> child : children.entrySet()) {
					if(!visited.contains(child.getKey())) {
						AlgorithmNode newChild = new AlgorithmNode(child.getKey(), child.getValue(), n);
						open.add(newChild);
					}
				}
			}
		}
			
		//ispis za BFS
		System.out.println("# BFS");
		if(found) {
			System.out.println("[FOUND_SOLUTION]: yes");
			Stack<AlgorithmNode> path = new Stack<AlgorithmNode>();
			double cost = 0;
			while(last != null) {
				path.add(last);
				cost += last.getValue();
				last = last.getParent();
			}
			
			
			System.out.println("[STATES_VISITED]: " + i);
			System.out.println("[PATH_LENGTH]: " + path.size());
			System.out.println("[TOTAL_COST]: " + cost);
			System.out.print("[PATH]: ");
			while(path.size() > 0) {
				
				if(path.size() == 1) {
					System.out.println(path.pop().getName());
				} else {
					System.out.print(path.pop().getName() + " => ");
				}
			}
		}else {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}
	
	//UCS
	//vrlo slican BFS-u, ali svakom novom cvoru koji dodaje u listu open za vrijednost postavnja zbroj njegove vrijednosti i vrijednosti roditelja
	//koristi prioritetni red, poredan po vrijednostima od najmanje do najvece
	private static void uniformCostSearch() {
		
		PriorityQueue<AlgorithmNode> open = new PriorityQueue<AlgorithmNode>(15, new NodeComparator());
		AlgorithmNode first = new AlgorithmNode(startState, 0.0, null);
		AlgorithmNode last = null;
		HashMap<String, Double> visited = new HashMap<String, Double>();
		
		open.add(first);
		boolean found = false;
		int i = 0;
		while(open.peek()!= null && !found) {
				
			AlgorithmNode n = open.poll();
			visited.put(n.getName(), n.getValue());
			i++;
			
			
			if(goalStates.contains(n.getName())) {
				found = true;
				last = n;
			} else {
				TreeMap<String, Double> children = transitions.get(n.getName());
				for(Map.Entry<String,Double> child : children.entrySet()) {
					Double newValue = child.getValue() + n.getValue();
					if(!visited.containsKey(child.getKey()) || visited.get(child.getKey()) > newValue) {
						AlgorithmNode newChild = new AlgorithmNode(child.getKey(), newValue, n);
						open.add(newChild);
					}
				}
			}
		}
			
		//ispis za UCS
		System.out.println("# UCS");
		if(found) {
			System.out.println("[FOUND_SOLUTION]: yes");
			Stack<AlgorithmNode> path = new Stack<AlgorithmNode>();
			double cost = last.getValue();
			while(last != null) {
				path.add(last);
				last = last.getParent();
			}
					
			System.out.println("[STATES_VISITED]: " + i);
			System.out.println("[PATH_LENGTH]: " + path.size());
			System.out.println("[TOTAL_COST]: " + cost);
			System.out.print("[PATH]: ");
			while(path.size() > 0) {
				
				if(path.size() == 1) {
					System.out.println(path.pop().getName());
				} else {
					System.out.print(path.pop().getName() + " => ");
				}
			}
		}else {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}
	
	//A* algoritam
	//slican UCS-u, ali u prioritetnom redu poreda stanja po zbroju trenutne vrijednostii stanja i pripadajuce heuristike
	private static void aStar() {
		
		PriorityQueue<AlgorithmNode> open = new PriorityQueue<AlgorithmNode>(15, new NodeHeuristicComparator());
		AlgorithmNode first = new AlgorithmNode(startState, 0.0, null);
		AlgorithmNode last = null;
		HashMap<String, Double> visited = new HashMap<String, Double>();
		HashMap<String, Double> openNames = new HashMap<String, Double>();
		
		open.add(first);
		openNames.put(first.getName(), first.getValue());
		boolean found = false;
		int i = 0;
		while(open.peek()!= null && !found) {
				
				
			AlgorithmNode n = open.poll();
			if(!visited.containsKey(n.getName())) {
				visited.put(n.getName(), n.getValue());
			}
			i++;
					
			if(goalStates.contains(n.getName())) {
				found = true;
				if(last == null || last.getValue() > n.getValue()) {
					last = n;
				}
			} else {
				TreeMap<String, Double> children = transitions.get(n.getName());
				for(Map.Entry<String,Double> child : children.entrySet()) {
					Double newValue = child.getValue() + n.getValue();
					if(!visited.containsKey(child.getKey()) || visited.get(child.getKey()) > newValue) {
						visited.put(child.getKey(), newValue);
						if(!openNames.containsKey(child.getKey()) || openNames.get(child.getKey()) > newValue) {
							AlgorithmNode newChild = new AlgorithmNode(child.getKey(), newValue, n);
							open.add(newChild);
						}
					}
				}
			}
		}
		
		//ispis za A*
		System.out.println("# A-STAR " + h);
		if(found) {
			System.out.println("[FOUND_SOLUTION]: yes");
			Stack<AlgorithmNode> path = new Stack<AlgorithmNode>();
			double cost = last.getValue();
			while(last != null) {
				path.add(last);
				last = last.getParent();
			}
					
			System.out.println("[STATES_VISITED]: " + i);
			System.out.println("[PATH_LENGTH]: " + path.size());
			System.out.println("[TOTAL_COST]: " + cost);
			System.out.print("[PATH]: ");
			while(path.size() > 0) {
				
				if(path.size() == 1) {
					System.out.print(path.pop().getName());
				} else {
					System.out.print(path.pop().getName() + " => ");
				}
			}
		} else {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}
	
	//provjera je li heuristika  optimisticna
	//svako postojeće stanje stavljam u ucs algoritam kako bih dobila najkrace udaljenosti koje potom usporedujem sa heuristikama
	//ukoliko su heuristike manje ili jednake izracunatim udaljenostima radi se o optimalnoj heuristici
	private static void checkOptimistic() {
		 			 
			TreeMap<String, Double> listOfReachedStates = new TreeMap<String, Double>();
			
			for(String s : heuristics.keySet()) {
				PriorityQueue<AlgorithmNode> open = new PriorityQueue<AlgorithmNode>(15, new NodeComparator());
				AlgorithmNode first = new AlgorithmNode(s, 0.0, null);
				AlgorithmNode last = null;
				HashMap<String, Double> visited = new HashMap<String, Double>();
				
				open.add(first);
				boolean found = false;
				int i = 0;
				while(open.peek()!= null && !found) {
						
					AlgorithmNode n = open.poll();
					visited.put(n.getName(), n.getValue());
					i++;
					
					
					if(goalStates.contains(n.getName())) {
						found = true;
						last = n;
					} else {
						TreeMap<String, Double> children = transitions.get(n.getName());
						for(Map.Entry<String,Double> child : children.entrySet()) {
							Double newValue = child.getValue() + n.getValue();
							if(!visited.containsKey(child.getKey()) || visited.get(child.getKey()) > newValue) {
								AlgorithmNode newChild = new AlgorithmNode(child.getKey(), newValue, n);
								open.add(newChild);
							}
						}
					}
				}
				listOfReachedStates.put(s, last.getValue());
			}
			
			
			System.out.println("# HEURISTIC-OPTIMISTIC " + h);
			boolean err = false;
			for(Map.Entry<String,Double> s : listOfReachedStates.entrySet()) {
				System.out.print("[CONDITION]: ");
				if(heuristics.get(s.getKey()).compareTo(s.getValue()) <= 0) {
					System.out.print("[OK] ");
				} else {
					System.out.print("[ERR] ");
					err = true;
				}
				System.out.println("h(" + s.getKey() + ") <= h*: " + heuristics.get(s.getKey()) +
				                   " <= " + s.getValue());
			}
			if(err) {
				System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
			} else {
				System.out.println("[CONCLUSION]: Heuristic is optimistic.");
			}	
		 
	
	}
	
	//h(s1) <= h(s2) + c(izmedi s1 i s2)
	//provjera je li heuristika konzistentna
	//za svako stanje prode kroz sve njegove prijelaze te provjeri monotonost heuristike
	private static void checkConsistent() {
		
		System.out.println("# HEURISTIC-CONSISTENT " + h);
		boolean err = false;
		for(Map.Entry<String,Double> s : heuristics.entrySet()) {
			TreeMap<String, Double> children = transitions.get(s.getKey());
			for(Map.Entry<String,Double> child : children.entrySet()) {
				System.out.print("[CONDITION]: ");
				if(s.getValue().compareTo(child.getValue() + heuristics.get(child.getKey())) <= 0) {
					System.out.print("[OK] ");
				} else {
					System.out.print("[ERR] ");
					err = true;
				}
				System.out.println("h(" + s.getKey() + ") <= h(" + child.getKey() + ") + c: " + s.getValue() +
				                   " <= " + heuristics.get(child.getKey()) + " + " + child.getValue());
			}
		}
		if(err) {
			System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		} else {
			System.out.println("[CONCLUSION]: Heuristic is consistent.");
		}
		
	}
	
	//ovaj razred koristim kako bi laske pretila put od zavrsnog stanja nazad do pocetnog stanja
	public static class AlgorithmNode {
		
		private String name;
		private Double value;
		private AlgorithmNode parent;
		
		
		public AlgorithmNode(String name, Double value, AlgorithmNode parent) {
			this.name = name;
			this.value = value;
			this.parent = parent;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Double getValue() {
			return this.value;
		}
		
		
		public AlgorithmNode getParent() {
			return this.parent;
		}
		
	}
	
	//komprator u prioritetnom redu za UCS algoritam
	static class NodeComparator implements Comparator<AlgorithmNode>{
        public int compare(AlgorithmNode n1, AlgorithmNode n2) {  
        	
        	if(n1.getValue().compareTo(n2.getValue()) != 0) {
        		return n1.getValue().compareTo(n2.getValue());
        	}
        	return n1.getName().compareTo(n2.getName());       	
        }
    }
	
	//komparator za prioritetni red za A* algoritam
	static class NodeHeuristicComparator implements Comparator<AlgorithmNode>{
        public int compare(AlgorithmNode n1, AlgorithmNode n2) {  
        	
        	if(Double.compare((n1.getValue() + heuristics.get(n1.getName())),(n2.getValue() + heuristics.get(n2.getName()))) != 0) {
        		return Double.compare((n1.getValue() + heuristics.get(n1.getName())),(n2.getValue() + heuristics.get(n2.getName())));
        	}
        	return n1.getName().compareTo(n2.getName());       	
        }
    }

}
