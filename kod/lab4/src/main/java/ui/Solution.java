package ui;

import java.io.BufferedReader;
import java.util.Random;
import java.util.TreeMap;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Solution {

	static ArrayList<String> namesOfValues = new ArrayList<String>();
	static ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
	static ArrayList<Double> targetValues = new ArrayList<Double>();
	static int inputDimensions = 0;
	static int hiddenLayerDimension = 0;
	static int hiddenLayerDimension2 = 0;
	
	static ArrayList<String> namesOfValuesTest = new ArrayList<String>();
	static ArrayList<ArrayList<Double>> dataTest = new ArrayList<ArrayList<Double>>();
	static ArrayList<Double> targetValuesTest = new ArrayList<Double>();
	
	static int popsize = 0;
	static int elitism = 0;
	static double p = 0.0;
	static double scaleK = 0.0;
	static int iter = 0;
	
	static double najboljiErrGeneracije = 0.0;
	static int indexOfBest = 0;
	
	static Random random = new Random();
	
	public static void main(String[] args) {	
		
		
		
		ArrayList<String> inputValues = new ArrayList<String>(Arrays.asList(args));
		for(int t = 0; t < inputValues.size(); t +=2) {
			
			if(inputValues.get(t).equals("--train")) {
				//train set
				List<String> lines = new ArrayList<String>();
				try {
					try (BufferedReader br = new BufferedReader(new FileReader(/*"src/main/resources/" +*/ args[t + 1]))) {
					    String line;
					    while ((line = br.readLine()) != null) {
					        lines.add(line);
					    }
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				boolean first = true;
				for (String line: lines) {
					
					if(first) {
						
						String[] arrS = line.split(",");
						namesOfValues = new ArrayList<String>(Arrays.asList(arrS));
						first = false;
						
					} else {
						String[] arrS = line.split(",");
						Double[] arr = new Double[arrS.length];
						for(int i = 0; i < arrS.length; i++) {
							arr[i] = Double.parseDouble(arrS[i]);
						}
						ArrayList<Double> example = new ArrayList<Double>(Arrays.asList(arr));
						data.add(example);
						targetValues.add(arr[arr.length -1]);
					}
				}
				
			} else if(inputValues.get(t).equals("--test")) {
				//test set
				List<String> lines2 = new ArrayList<String>();
				try {
					try (BufferedReader br = new BufferedReader(new FileReader(/*"src/main/resources/" + */ args[t + 1]))) {
					    String line;
					    while ((line = br.readLine()) != null) {
					        lines2.add(line);
					    }
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				boolean first2 = true;
				for (String line: lines2) {
					
					if(first2) {
						String[] arrTest = line.split(",");
						namesOfValuesTest = new ArrayList<String>(Arrays.asList(arrTest));
						first2 = false;
					} else {
						String[] arrTest = line.split(",");
						Double[] arr = new Double[arrTest.length];
						for(int i = 0; i < arrTest.length; i++) {
							arr[i] = Double.parseDouble(arrTest[i]);
						}
						ArrayList<Double> example = new ArrayList<Double>(Arrays.asList(arr));
						dataTest.add(example);
						targetValuesTest.add(arr[arr.length -1]);
					}
				}
				
			} else if(inputValues.get(t).equals("--nn")) {
				String nn = inputValues.get(t + 1);
				if(nn.length() == 2) {
					hiddenLayerDimension = 5;
				} else if (nn.length() == 3) {
					hiddenLayerDimension = 20;
				} else if (nn.length() == 4) {
					hiddenLayerDimension = 5;
					hiddenLayerDimension2 = 5;
				}
				
				
			} else if(inputValues.get(t).equals("--popsize")) {
				popsize = Integer.parseInt(inputValues.get(t + 1));
				
			} else if(inputValues.get(t).equals("--elitism")) {
				elitism = Integer.parseInt(inputValues.get(t + 1));
				
			} else if(inputValues.get(t).equals("--p")) {
				p = Double.parseDouble(inputValues.get(t + 1));
				
			} else if(inputValues.get(t).equals("--K")) {
				scaleK = Double.parseDouble(inputValues.get(t + 1));
				
			} else if(inputValues.get(t).equals("--iter")) {
				iter = Integer.parseInt(inputValues.get(t + 1));
				
			} else {
				System.out.println("Wrong input format." + args[t]);
			}
		}
		
		inputDimensions = namesOfValues.size() - 1;
		
		if(hiddenLayerDimension2 == 0) {
			geneticAlgorithm();
		} else {
			geneticAlgorithm2();
		}
		
		
		
		
	}


	private static void geneticAlgorithm() {
		
		//Random random = new Random();
		
		ArrayList<Double[]> listIzlazniSloj = new ArrayList<Double[]>();
		ArrayList<Double[][]> listSkriveniSloj = new ArrayList<Double[][]>();
		TreeMap<Double, Integer> errorAndIndex = new TreeMap<Double, Integer>();
		
		ArrayList<NeuralNetwork> staraPopulacija = new ArrayList<NeuralNetwork>();
		ArrayList<NeuralNetwork> populacija = new ArrayList<NeuralNetwork>();
				
		for(int i = 0; i < popsize; i++) {
			NeuralNetwork nn = oneLayerNetwork();
			errorAndIndex.put(nn.getError(), i);
			listSkriveniSloj.add(nn.getSkriveniSloj());
			listIzlazniSloj.add(nn.getIzlazniSloj());
			populacija.add(nn);
		}
		
		
		
		
		for(int iteracije = 0; iteracije <= iter; iteracije++) {
			
			staraPopulacija.clear();
			staraPopulacija.addAll(populacija);
			populacija.clear();
			errorAndIndex.clear();
			listSkriveniSloj.clear();
			listIzlazniSloj.clear();
			
			int ind = 0;
			for(NeuralNetwork nn: staraPopulacija) {
				errorAndIndex.put(nn.getError(), ind);
				listSkriveniSloj.add(nn.getSkriveniSloj());
				listIzlazniSloj.add(nn.getIzlazniSloj());
				ind++;
			}
			
			//prenosenje najboljih u novu generaciju
			for(int i = 0; i < elitism; i++) {
				double bestErr = errorAndIndex.firstKey();
				int index = errorAndIndex.get(bestErr);
				if(i == 0) {
					najboljiErrGeneracije = bestErr;
					indexOfBest = index;
				}
				NeuralNetwork jedinka = new NeuralNetwork(listSkriveniSloj.get(index), listIzlazniSloj.get(index), bestErr);
				populacija.add(jedinka);
				errorAndIndex.remove(bestErr);
			}
			
			
			while(populacija.size() < popsize) {
				
		        //selekcija proporcionalna dobroti
		        //treba izracunati zbroj svih dobrota
		        double zbrojDobrota = 0;
		        for(NeuralNetwork jed: staraPopulacija) {
		        	zbrojDobrota += (1 / jed.getError());
		        }
		        
		        //prvi roditelj
		        double randomDouble = random.nextDouble() * zbrojDobrota;
		        double traziPoDobroti = 0;
		        NeuralNetwork parent1 = null;
		        for(NeuralNetwork jed: staraPopulacija) {
		        	traziPoDobroti += (1 / jed.getError());
		        	if(traziPoDobroti > randomDouble) {
		        		parent1 = jed;
		        		break;
		        	}
		        }
		        
		        //drugi roditelj
		        double randomDouble2 = random.nextDouble() * zbrojDobrota;
		        double traziPoDobroti2 = 0;
		        NeuralNetwork parent2 = null;
		        for(NeuralNetwork jed: staraPopulacija) {
		        	traziPoDobroti2 += (1 / jed.getError());
		        	if(traziPoDobroti2 > randomDouble2) {
		        		parent2 = jed;
		        		break;
		        	}
		        }
		        
		        //dijete je  aritmeticka sredina roditelja
		        Double[][] dijeteSkriveni = new Double[hiddenLayerDimension][inputDimensions + 1];
		        Double[] dijeteIzlazni = new Double[hiddenLayerDimension + 1];
		        for(int i = 0; i < dijeteIzlazni.length; i++) {
		        	dijeteIzlazni[i] = (parent1.getIzlazniSloj()[i] + parent2.getIzlazniSloj()[i]) / 2;

		        }
		        for(int i = 0; i < hiddenLayerDimension; i++) {
		        	for(int j = 0; j < inputDimensions + 1; j++) {
		        		dijeteSkriveni[i][j] = (parent1.getSkriveniSloj()[i][j] + parent2.getSkriveniSloj()[i][j]) / 2;
		        	}
		        }
		        
		        //mutacije skrivenog sloja
		        for(int i = 0; i < hiddenLayerDimension; i++) {
		        	for(int j = 0; j < inputDimensions + 1; j++) {
		        		double randomChance = random.nextDouble();
				        if(randomChance < p) {  //doslo je do mutacije
				        	double mutation = random.nextGaussian() * scaleK;
		        		    dijeteSkriveni[i][j] += mutation;
		        		}
		        	}
		        }
		        //mutacija izllaznog sloja
	        	for(int j = 0; j < hiddenLayerDimension + 1; j++) {
	        		double randomChance = random.nextDouble();
	    	        if(randomChance < p) {  //doslo je do mutacije
	    	        	double mutation = random.nextGaussian() * scaleK;
	        			dijeteIzlazni[j] += mutation;
	        		}
	        	}
	        	
	        	NeuralNetwork novaNN = new NeuralNetwork(dijeteSkriveni, dijeteIzlazni, 0.0);
		        populacija.add(novaNN);
		        
		        
			}
			
			staraPopulacija.clear();
			staraPopulacija.addAll(populacija);
			populacija.clear();
			//izracunati izlaze nove populacije (populacija)
			for(NeuralNetwork nn : staraPopulacija) {

				//racun prve linearne transformacije i prijenosne funkcije
				ArrayList<ArrayList<Double>> izlazi = new ArrayList<ArrayList<Double>>();
				ArrayList<Double> vrijednostiIzlazaDatasetova = new ArrayList<Double>();
				int t = 0;
				for(ArrayList<Double> dataset : data) {
					ArrayList<Double> skriveniSlojIzlazi = new ArrayList<Double>();
					//double result;
					for(int j = 0; j < hiddenLayerDimension; j++) {
						double result = nn.getSkriveniSloj()[j][0];
						for(int i = 0; i < dataset.size() - 1; i++) {
							result += (nn.getSkriveniSloj()[j][i + 1] * dataset.get(i));
						}
						//provuci kroz funkciju
						result = 1 / (1 + Math.exp(-result)); //sigmoida
						skriveniSlojIzlazi.add(result);
					}
					//racun druge linearne transformacije
					double rez = nn.getIzlazniSloj()[0];
					for(int k = 0; k < skriveniSlojIzlazi.size(); k++) {
						rez += (skriveniSlojIzlazi.get(k) * nn.getIzlazniSloj()[k + 1]);
					}
					vrijednostiIzlazaDatasetova.add(rez);
		            t++;
		            izlazi.add(skriveniSlojIzlazi);
				}
				
				//pogreska
				Double err = 0.0;
				for(int i = 0; i < targetValues.size(); i++) {
					err += Math.pow(vrijednostiIzlazaDatasetova.get(i) - targetValues.get(i), 2);
				}
				err /= targetValues.size();
				
				NeuralNetwork jedinka = new NeuralNetwork(nn.getSkriveniSloj(), nn.getIzlazniSloj(), err);
				populacija.add(jedinka);
				
			}
			
			
			if(iteracije % 2000 == 0 && iteracije != 0) {
				System.out.println("[Train error @" + Integer.toString(iteracije) + "]: " + Double.toString(najboljiErrGeneracije));
			}
		
			
		}
		
		//koristiti najbolju jedinku na test setu
		NeuralNetwork bestNN = new NeuralNetwork(listSkriveniSloj.get(indexOfBest), listIzlazniSloj.get(indexOfBest), 0.0);
		//racun prve linearne transformacije i prijenosne funkcije
		ArrayList<ArrayList<Double>> izlazi = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> vrijednostiIzlazaDatasetova = new ArrayList<Double>();
		int c = 0;
		for(ArrayList<Double> dataset : dataTest) {
			ArrayList<Double> skriveniSlojIzlazi = new ArrayList<Double>();
			//double result;
			for(int j = 0; j < hiddenLayerDimension; j++) {
				double result = bestNN.getSkriveniSloj()[j][0];
				for(int i = 0; i < dataset.size() - 1; i++) {
					result += (bestNN.getSkriveniSloj()[j][i + 1] * dataset.get(i));
				}
				//provuci kroz funkciju
				result = 1 / (1 + Math.exp(-result)); //sigmoida
				skriveniSlojIzlazi.add(result);
			}
			//racun druge linearne transformacije
			double rez = bestNN.getIzlazniSloj()[0];
			for(int k = 0; k < skriveniSlojIzlazi.size(); k++) {
				rez += (skriveniSlojIzlazi.get(k) * bestNN.getIzlazniSloj()[k + 1]);
			}
			vrijednostiIzlazaDatasetova.add(rez);
            c++;
            izlazi.add(skriveniSlojIzlazi);
		}
		
		//pogreska
		Double err = 0.0;
		for(int i = 0; i < targetValuesTest.size(); i++) {
			err += Math.pow(vrijednostiIzlazaDatasetova.get(i) - targetValuesTest.get(i), 2);
		}
		err /= targetValuesTest.size();
		System.out.println("[Test error]: " + err);
			
		
		
	}

	//stvara 5s i 20s NN
	private static NeuralNetwork oneLayerNetwork() {
		
		//generiranje pocetnih vrijednosti svih tezina neuronske mreze, iz normalne razdiobe sa standardnom devijacijom 0.01.
		//prvo za prvi neuron pa drugi itd
		//prvo w0 (b) pa tezine za ulaze
		Double[][] tezine = new Double[hiddenLayerDimension][inputDimensions + 1];
		//Random random = new Random();
		for (int i = 0; i < tezine.length; i++) {
            for (int j = 0; j < tezine[i].length; j++) {
                tezine[i][j] = random.nextGaussian() * 0.01;
            }
        }
		//tezine izlaznog sloja
		Double[] tezineIzlazniSloj = new Double[hiddenLayerDimension + 1];
		for (int i = 0; i < tezineIzlazniSloj.length; i++) {
			tezineIzlazniSloj[i] = random.nextGaussian() * 0.01;
        }
		
		
		//racun prve linearne transformacije i prijenosne funkcije
		ArrayList<ArrayList<Double>> izlazi = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> vrijednostiIzlazaDatasetova = new ArrayList<Double>();
		int t = 0;
		for(ArrayList<Double> dataset : data) {
			ArrayList<Double> skriveniSlojIzlazi = new ArrayList<Double>();
			//double result;
			for(int j = 0; j < hiddenLayerDimension; j++) {
				double result = tezine[j][0];
				for(int i = 0; i < dataset.size() - 1; i++) {
					result += (tezine[j][i + 1] * dataset.get(i));
				}
				//provuci kroz funkciju
				result = 1 / (1 + Math.exp(-result)); //sigmoid
				skriveniSlojIzlazi.add(result);
			}
			//racun druge linearne transformacije
			double rez = tezineIzlazniSloj[0];
			for(int k = 0; k < skriveniSlojIzlazi.size(); k++) {
				rez += (skriveniSlojIzlazi.get(k) * tezineIzlazniSloj[k + 1]);
			}
			vrijednostiIzlazaDatasetova.add(rez);
            t++;
            izlazi.add(skriveniSlojIzlazi);
		}
		
		//pogreska
		Double err = 0.0;
		for(int i = 0; i < targetValues.size(); i++) {
			err += Math.pow(vrijednostiIzlazaDatasetova.get(i) - targetValues.get(i), 2);
		}
		err /= targetValues.size();
		
		NeuralNetwork jedinka = new NeuralNetwork(tezine, tezineIzlazniSloj, err);
		return jedinka;
	}
	
	
	
    public static class NeuralNetwork {
		
		private Double[][] skriveniSloj;
		private Double[][] skriveniSloj2;
		private Double[] izlazniSloj;
		private Double error;
		
		public NeuralNetwork(Double[][] skriveniSloj, Double[] izlazniSloj, Double error) {
			this.skriveniSloj = skriveniSloj;
			this.izlazniSloj = izlazniSloj;
			this.error = error;
		}
		public NeuralNetwork(Double[][] skriveniSloj, Double[][] skriveniSloj2, Double[] izlazniSloj, Double error) {
			this.skriveniSloj = skriveniSloj;
			this.skriveniSloj2 = skriveniSloj2;
			this.izlazniSloj = izlazniSloj;
			this.error = error;
		}
		
		public Double[][] getSkriveniSloj() {
			return skriveniSloj;
		}
		
		public Double[][] getSkriveniSloj2() {
			return skriveniSloj2;
		}
		
		public Double[] getIzlazniSloj() {
			return izlazniSloj;
		}
		
		public Double getError() {
			return error;
		}	
		
		@Override
		public String toString() {
			String s = "Skriveni sloj: " + Arrays.asList(skriveniSloj) + "\nIzlazni sloj: " + Arrays.asList(izlazniSloj) + "\nError: " + error;
			return s;
		}
	}
    
    // za 5s5s
    private static NeuralNetwork twoLayerNetwork() {
    	
    	//Random random = new Random();
    	
    	//generiranje pocetnih vrijednosti svih tezina neuronske mreze, iz normalne razdiobe sa standardnom devijacijom 0.01.
    			//prvo za prvi neuron pa drugi itd
    			//prvo w0 (b) pa tezine za ulaze
    			Double[][] tezine = new Double[hiddenLayerDimension][inputDimensions + 1];
    			
    			for (int i = 0; i < tezine.length; i++) {
    	            for (int j = 0; j < tezine[i].length; j++) {
    	                tezine[i][j] = random.nextGaussian() * 0.01;
    	            }
    	        }
    			//tezine drugog skrivenog sloja
    			Double[][] tezine2 = new Double[hiddenLayerDimension2][hiddenLayerDimension + 1];
    			for (int i = 0; i < tezine2.length; i++) {
    	            for (int j = 0; j < tezine2[i].length; j++) {
    	                tezine2[i][j] = random.nextGaussian() * 0.01;
    	            }
    	        }
    			//tezine izlaznog sloja
    			Double[] tezineIzlazniSloj = new Double[hiddenLayerDimension2 + 1];
    			for (int i = 0; i < tezineIzlazniSloj.length; i++) {
    				tezineIzlazniSloj[i] = random.nextGaussian() * 0.01;
    	        }
    			
    			
    			//racun prve linearne transformacije i prijenosne funkcije
    			//ArrayList<ArrayList<Double>> izlazi = new ArrayList<ArrayList<Double>>();
    			ArrayList<Double> vrijednostiIzlazaDatasetova = new ArrayList<Double>();
    			for(ArrayList<Double> dataset : data) {
    				ArrayList<Double> skriveniSlojIzlazi = new ArrayList<Double>();
    				ArrayList<Double> skriveniSlojIzlazi2 = new ArrayList<Double>();
    				//double result;
    				for(int j = 0; j < hiddenLayerDimension; j++) {
    					double result = tezine[j][0];
    					for(int i = 0; i < dataset.size() - 1; i++) {
    						result += (tezine[j][i + 1] * dataset.get(i));
    					}
    					//provuci kroz funkciju
    					result = 1 / (1 + Math.exp(-result)); //sigmoid
    					skriveniSlojIzlazi.add(result);
    				}
    				//racun drugog skrivenog sloja
    				for(int j = 0; j < hiddenLayerDimension2; j++) {
    					double result = tezine2[j][0];
    					for(int i = 0; i < skriveniSlojIzlazi.size() - 1; i++) {
    						result += (tezine2[j][i + 1] * skriveniSlojIzlazi.get(i));
    					}
    					//provuci kroz funkciju
    					result = 1 / (1 + Math.exp(-result)); //sigmoid
    					skriveniSlojIzlazi2.add(result);
    				}
    				
    				//racun druge linearne transformacije
    				double rez = tezineIzlazniSloj[0];
    				for(int k = 0; k < skriveniSlojIzlazi2.size(); k++) {
    					rez += (skriveniSlojIzlazi2.get(k) * tezineIzlazniSloj[k + 1]);
    				}
    				vrijednostiIzlazaDatasetova.add(rez);
    	            //izlazi.add(skriveniSlojIzlazi);
    			}
    			
    			//pogreska
    			Double err = 0.0;
    			for(int i = 0; i < targetValues.size(); i++) {
    				err += Math.pow(vrijednostiIzlazaDatasetova.get(i) - targetValues.get(i), 2);
    			}
    			err /= targetValues.size();
    			
    			NeuralNetwork jedinka = new NeuralNetwork(tezine, tezine2, tezineIzlazniSloj, err);
    			return jedinka;
    }
    
    
private static void geneticAlgorithm2() {
		
		//Random random = new Random();
		
		ArrayList<Double[]> listIzlazniSloj = new ArrayList<Double[]>();
		ArrayList<Double[][]> listSkriveniSloj = new ArrayList<Double[][]>();
		ArrayList<Double[][]> listSkriveniSloj2 = new ArrayList<Double[][]>();
		TreeMap<Double, Integer> errorAndIndex = new TreeMap<Double, Integer>();
		
		ArrayList<NeuralNetwork> staraPopulacija = new ArrayList<NeuralNetwork>();
		ArrayList<NeuralNetwork> populacija = new ArrayList<NeuralNetwork>();
				
		for(int i = 0; i < popsize; i++) {
			NeuralNetwork nn = twoLayerNetwork();
			errorAndIndex.put(nn.getError(), i);
			listSkriveniSloj.add(nn.getSkriveniSloj());
			listSkriveniSloj2.add(nn.getSkriveniSloj2());
			listIzlazniSloj.add(nn.getIzlazniSloj());
			populacija.add(nn);
		}
		
		
		
		
		for(int iteracije = 0; iteracije <= iter; iteracije++) {
			
			staraPopulacija.clear();
			staraPopulacija.addAll(populacija);
			populacija.clear();
			errorAndIndex.clear();
			listSkriveniSloj.clear();
			listSkriveniSloj2.clear();
			listIzlazniSloj.clear();
			
			int ind = 0;
			for(NeuralNetwork nn: staraPopulacija) {
				errorAndIndex.put(nn.getError(), ind);
				listSkriveniSloj.add(nn.getSkriveniSloj());
				listSkriveniSloj2.add(nn.getSkriveniSloj2());
				listIzlazniSloj.add(nn.getIzlazniSloj());
				ind++;
			}
			
			//prenosenje najboljih u novu generaciju
			for(int i = 0; i < elitism; i++) {
				double bestErr = errorAndIndex.firstKey();
				int index = errorAndIndex.get(bestErr);
				if(i == 0) {
					najboljiErrGeneracije = bestErr;
					indexOfBest = index;
				}
				NeuralNetwork jedinka = new NeuralNetwork(listSkriveniSloj.get(index), listSkriveniSloj2.get(index), listIzlazniSloj.get(index), bestErr);
				populacija.add(jedinka);
				errorAndIndex.remove(bestErr);
			}
			
			
			while(populacija.size() < popsize) {
				
		        //selekcija proporcionalna dobroti
		        //treba izracunati zbroj svih dobrota
		        double zbrojDobrota = 0;
		        for(NeuralNetwork jed: staraPopulacija) {
		        	zbrojDobrota += (1 / jed.getError());
		        }
		        
		        //prvi roditelj
		        double randomDouble = random.nextDouble() * zbrojDobrota;
		        double traziPoDobroti = 0;
		        NeuralNetwork parent1 = null;
		        for(NeuralNetwork jed: staraPopulacija) {
		        	traziPoDobroti += (1 / jed.getError());
		        	if(traziPoDobroti > randomDouble) {
		        		parent1 = jed;
		        		break;
		        	}
		        }
		        
		        //drugi roditelj
		        double randomDouble2 = random.nextDouble() * zbrojDobrota;
		        double traziPoDobroti2 = 0;
		        NeuralNetwork parent2 = null;
		        for(NeuralNetwork jed: staraPopulacija) {
		        	traziPoDobroti2 += (1 / jed.getError());
		        	if(traziPoDobroti2 > randomDouble2) {
		        		parent2 = jed;
		        		break;
		        	}
		        }
		        
		        //dijete je  aritmeticka sredina roditelja
		        Double[][] dijeteSkriveni = new Double[hiddenLayerDimension][inputDimensions + 1];
		        Double[][] dijeteSkriveni2 = new Double[hiddenLayerDimension2][hiddenLayerDimension + 1];
		        Double[] dijeteIzlazni = new Double[hiddenLayerDimension2 + 1];
		        for(int i = 0; i < dijeteIzlazni.length; i++) {
		        	dijeteIzlazni[i] = (parent1.getIzlazniSloj()[i] + parent2.getIzlazniSloj()[i]) / 2;

		        }
		        for(int i = 0; i < hiddenLayerDimension; i++) {
		        	for(int j = 0; j < inputDimensions + 1; j++) {
		        		dijeteSkriveni[i][j] = (parent1.getSkriveniSloj()[i][j] + parent2.getSkriveniSloj()[i][j]) / 2;
		        	}
		        }
		        for(int i = 0; i < hiddenLayerDimension2; i++) {
		        	for(int j = 0; j < hiddenLayerDimension + 1; j++) {
		        		dijeteSkriveni2[i][j] = (parent1.getSkriveniSloj2()[i][j] + parent2.getSkriveniSloj2()[i][j]) / 2;
		        	}
		        }
		        
		        //mutacije skrivenog sloja
		        for(int i = 0; i < hiddenLayerDimension; i++) {
		        		for(int j = 0; j < inputDimensions + 1; j++) {
		        			double randomChance = random.nextDouble();
				        	if(randomChance < p) {  //doslo je do mutacije
				        		double mutation = random.nextGaussian() * scaleK;
		        			    dijeteSkriveni[i][j] += mutation;
		        		}
		        	}
		        }
		      //mutacije drugog skrivenog sloja
		        for(int i = 0; i < hiddenLayerDimension2; i++) {
		        		for(int j = 0; j < inputDimensions + 1; j++) {
		        			double randomChance = random.nextDouble();
				        	if(randomChance < p) {  //doslo je do mutacije
				        		double mutation = random.nextGaussian() * scaleK;
		        			    dijeteSkriveni2[i][j] += mutation;
		        		}
		        	}
		        }
		        //mutacija izllaznog sloja
	        	for(int j = 0; j < hiddenLayerDimension + 1; j++) {
	        		double randomChance = random.nextDouble();
		        	if(randomChance < p) {  //doslo je do mutacije
		        		double mutation = random.nextGaussian() * scaleK;
	        			dijeteIzlazni[j] += mutation;
	        		}
	        	}
		        
	        	NeuralNetwork novaNN = new NeuralNetwork(dijeteSkriveni, dijeteSkriveni2, dijeteIzlazni, 0.0);
		        populacija.add(novaNN);
		        
			}
			
			staraPopulacija.clear();
			staraPopulacija.addAll(populacija);
			populacija.clear();
			//izracunati izlaze nove populacije (populacija)
			for(NeuralNetwork nn : staraPopulacija) {

				//racun prve linearne transformacije i prijenosne funkcije
				ArrayList<Double> vrijednostiIzlazaDatasetova = new ArrayList<Double>();
				for(ArrayList<Double> dataset : data) {
					//prvi skriveni sloj
					ArrayList<Double> skriveniSlojIzlazi = new ArrayList<Double>();
					for(int j = 0; j < hiddenLayerDimension; j++) {
						double result = nn.getSkriveniSloj()[j][0];
						for(int i = 0; i < dataset.size() - 1; i++) {
							result += (nn.getSkriveniSloj()[j][i + 1] * dataset.get(i));
						}
						//provuci kroz funkciju
						result = 1 / (1 + Math.exp(-result)); //sigmoida
						skriveniSlojIzlazi.add(result);
					}
					//drugi skriveni sloj
					ArrayList<Double> skriveniSlojIzlazi2 = new ArrayList<Double>();
					for(int j = 0; j < hiddenLayerDimension2; j++) {
						double result = nn.getSkriveniSloj2()[j][0];
						for(int i = 0; i < skriveniSlojIzlazi.size() - 1; i++) {
							result += (nn.getSkriveniSloj2()[j][i + 1] * skriveniSlojIzlazi.get(i));
						}
						//provuci kroz funkciju
						result = 1 / (1 + Math.exp(-result)); //sigmoida
						skriveniSlojIzlazi2.add(result);
					}
					//racun druge linearne transformacije
					double rez = nn.getIzlazniSloj()[0];
					for(int k = 0; k < skriveniSlojIzlazi2.size(); k++) {
						rez += (skriveniSlojIzlazi2.get(k) * nn.getIzlazniSloj()[k + 1]);
					}
					vrijednostiIzlazaDatasetova.add(rez);
				}
				
				//pogreska
				Double err = 0.0;
				for(int i = 0; i < targetValues.size(); i++) {
					err += Math.pow(vrijednostiIzlazaDatasetova.get(i) - targetValues.get(i), 2);
				}
				err /= targetValues.size();
				
				NeuralNetwork jedinka = new NeuralNetwork(nn.getSkriveniSloj(), nn.getSkriveniSloj2(), nn.getIzlazniSloj(), err);
				populacija.add(jedinka);
				
			}
			
			
			if(iteracije % 2000 == 0 && iteracije != 0) {
				System.out.println("[Train error @" + Integer.toString(iteracije) + "]: " + Double.toString(najboljiErrGeneracije));
			}
		
			
		}
		
		//koristiti najbolju jedinku na test setu
		NeuralNetwork bestNN = new NeuralNetwork(listSkriveniSloj.get(indexOfBest), listSkriveniSloj2.get(indexOfBest), listIzlazniSloj.get(indexOfBest), 0.0);
		//racun prve linearne transformacije i prijenosne funkcije
		ArrayList<Double> vrijednostiIzlazaDatasetova = new ArrayList<Double>();
		for(ArrayList<Double> dataset : dataTest) {
			ArrayList<Double> skriveniSlojIzlazi = new ArrayList<Double>();
			//prvi skriveni sloj
			for(int j = 0; j < hiddenLayerDimension; j++) {
				double result = bestNN.getSkriveniSloj()[j][0];
				for(int i = 0; i < dataset.size() - 1; i++) {
					result += (bestNN.getSkriveniSloj()[j][i + 1] * dataset.get(i));
				}
				//provuci kroz funkciju
				result = 1 / (1 + Math.exp(-result)); //sigmoida
				skriveniSlojIzlazi.add(result);
			}
			ArrayList<Double> skriveniSlojIzlazi2 = new ArrayList<Double>();
			//drugi skriveni sloj
			for(int j = 0; j < hiddenLayerDimension2; j++) {
				double result = bestNN.getSkriveniSloj2()[j][0];
				for(int i = 0; i < skriveniSlojIzlazi.size() - 1; i++) {
					result += (bestNN.getSkriveniSloj2()[j][i + 1] * skriveniSlojIzlazi.get(i));
				}
				//provuci kroz funkciju
				result = 1 / (1 + Math.exp(-result)); //sigmoida
				skriveniSlojIzlazi2.add(result);
			}
			//racun druge linearne transformacije
			double rez = bestNN.getIzlazniSloj()[0];
			for(int k = 0; k < skriveniSlojIzlazi2.size(); k++) {
				rez += (skriveniSlojIzlazi2.get(k) * bestNN.getIzlazniSloj()[k + 1]);
			}
			vrijednostiIzlazaDatasetova.add(rez);
		}
		
		//pogreska
		Double err = 0.0;
		for(int i = 0; i < targetValuesTest.size(); i++) {
			err += Math.pow(vrijednostiIzlazaDatasetova.get(i) - targetValuesTest.get(i), 2);
		}
		err /= targetValuesTest.size();
		System.out.println("[Test error]: " + err);
			
		
		
	}
	

}
