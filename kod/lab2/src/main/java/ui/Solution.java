package ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class Solution {
	
	static ArrayList<LinkedHashSet<String>> initialClauses = new ArrayList<LinkedHashSet<String>>();
	static HashMap<String,String> parents = new HashMap<String,String>();
	static ArrayList<String> indexes = new ArrayList<String>();
	static LinkedHashSet<String> goal = new LinkedHashSet<String>();
	static ArrayList<String> goalClauses = new ArrayList<String>();
	static LinkedHashSet<String> negatedGoal = new LinkedHashSet<String>();

	public static void main(String[] args) {
		
		boolean cooking = false;
		boolean resolution = false;
		
		//pregled danog inputa
		if(args.length == 2) {        //RESOLUTION
			if(args[0].equals("resolution")) {
				resolution = true;
			} else {
				System.out.println("Invalid input!");
			}	
		} else if(args.length == 3) {  //COOKING
			if(args[0].equals("cooking")) {
				cooking = true;
			} else {
				System.out.println("Invalid input!");
			}
		}else {                       //INVALID INPUT
			System.out.println("Invalid input!");
		}
		
		//ucitavam podatke iz zadane datoteke s klauzulama
		List<String> lines = null;
		try {
			lines = Files.readAllLines(
					Paths.get(/*"src/main/resources/" +*/args[1]), 
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//ucitavam klauzule u listu setova (koristim setove kroz kod da se automatski rijesim duplikata)
		int lineCounter = 0;
		for (String line: lines) {
			if(!line.startsWith("#")) {
				line = line.toLowerCase();
				
				String[] orStatesOfSingleClause = line.split(" v ");
				
				//eliminiraju se duplici unutar jedne clause
				LinkedHashSet<String> singleClause = new LinkedHashSet<String>();
				for(String state : orStatesOfSingleClause) {
					singleClause.add(state);
				}
				if(!removeIrrelevant(singleClause)) {
					initialClauses.add(singleClause);
				}		
			}
			lineCounter++;
		}
		
		//zadnja linija iz datoteke se negira jer je to cilj
		if(resolution) {
			goal = initialClauses.get(initialClauses.size()-1);
			for(String g : goal) {
				
				if(g.contains("~")) {
					negatedGoal.add(g.replace("~", ""));
					goalClauses.add(g.replace("~", ""));
				} else {
					negatedGoal.add("~" + g);
					goalClauses.add("~" + g);
				}
			}
			initialClauses.remove(initialClauses.size()-1);
		}
		
	
		//zovem algoritam
		if(resolution) {
			plResolution();	
		}
		
		//cooking
		if(cooking) {
			List<String> lines2 = null;
			try {
				lines2 = Files.readAllLines(
						Paths.get(/*"src/main/resources/" +*/ args[2]), 
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			cooker(lines2, args[1]);	
		}
		
		
	}



	private static void plResolution() {
		
		ArrayList<LinkedHashSet<String>> sos = new ArrayList<LinkedHashSet<String>>();
		ArrayList<LinkedHashSet<String>> newlyFound = new ArrayList<LinkedHashSet<String>>();
		for(String g : goalClauses) {
			LinkedHashSet<String> oneGoal = new LinkedHashSet<String>();
			oneGoal.add(g);
			sos.add(oneGoal);
			parents.put(g, null);
			indexes.add(g);
		}
		String parent1 = null;
		String parent2 = null;
		
		for(LinkedHashSet<String> c : initialClauses) {
			parents.put(stringify(c), null);
			indexes.add(stringify(c));
		}
		
		
		boolean nil = false;
		int sosOldSize = 0;
		
		while(!nil && sosOldSize != sos.size()) {
			
			sosOldSize = sos.size();
			
			for(LinkedHashSet<String> s : sos) { //lista kroz klauzule sos-a
				for(String el: s) {              //lista kroz zasebne elemente svake klauzule
					if(!el.contains("~")) {
						
						for(LinkedHashSet<String> initClause : initialClauses) {  //prolaz kroz inicijalne za provjeru
							
							if(initClause.contains("~" + el)) {  //stvaramo novu klauzulu
								LinkedHashSet<String> newSet = new LinkedHashSet<String>();
								newSet.addAll(initClause);
								newSet.addAll(s);
								newSet.remove(el);
								newSet.remove("~" + el);
								if(newSet.isEmpty()) {
									nil = true;
									parent1 = stringify(s);
									parent2 = stringify(initClause);
								}
								if(!removeIrrelevant(newSet)) {
									
									if(!sos.contains(newSet) && !newlyFound.contains(newSet)) {
										parents.put(stringify(newSet), stringify(s) + "," + stringify(initClause));
										indexes.add(stringify(newSet));
									}
									newlyFound.add(newSet);
								}
							}
						}
						
					} else {
	                    for(LinkedHashSet<String> initClause : initialClauses) {  //prolaz kroz inicijalne za provjeru
							
							if(initClause.contains(el.replace("~", ""))) {  //stvaramo novu klauzulu
								LinkedHashSet<String> newSet = new LinkedHashSet<String>();
								newSet.addAll(initClause);
								newSet.addAll(s);
								newSet.remove(el);
								newSet.remove(el.replace("~", ""));
								if(newSet.isEmpty()) {
									nil = true;
									parent1 = stringify(s);
									parent2 = stringify(initClause);
								}
								if(!removeIrrelevant(newSet)) {
									
									if(!sos.contains(newSet) && !newlyFound.contains(newSet)) {
										parents.put(stringify(newSet), stringify(s) + "," + stringify(initClause));
										indexes.add(stringify(newSet));
									}
									newlyFound.add(newSet);
								}
							}
						}
					}
				}
			}
			
			
			
			for(int j = 0; j < sos.size(); j++) { //lista kroz klauzule sos-a
				for(String el: sos.get(j)) {              //lista kroz zasebne elemente svake klauzule
					if(!el.contains("~")) {
						
						for(int k = 0; k < sos.size(); k++) {  //prolaz kroz inicijalne za provjeru
							if(k != j) {
								
								if(sos.get(k).contains("~" + el)) {  //stvaramo novu klauzulu
									LinkedHashSet<String> newSet = new LinkedHashSet<String>();
									newSet.addAll(sos.get(k));
									newSet.addAll(sos.get(j));
									newSet.remove(el);
									newSet.remove("~" + el);
									if(newSet.isEmpty()) {
										nil = true;
										parent1 = stringify(sos.get(k));
										parent2 = stringify(sos.get(j));
									}
									if(!removeIrrelevant(newSet)) {
										
										if(!sos.contains(newSet) && !newlyFound.contains(newSet)) {
											parents.put(stringify(newSet), stringify(sos.get(k)) + "," + stringify(sos.get(j)));
											indexes.add(stringify(newSet));
										}
										newlyFound.add(newSet);
									}
								}
							}
						}
						
					} else {
						for(int k = 0; k < sos.size(); k++) {  //prolaz kroz inicijalne za provjeru
							if(k != j) {
								
								if(sos.get(k).contains(el.replace("~", ""))) {  //stvaramo novu klauzulu
									LinkedHashSet<String> newSet = new LinkedHashSet<String>();
									newSet.addAll(sos.get(k));
									newSet.addAll(sos.get(j));
									newSet.remove(el);
									newSet.remove(el.replace("~", ""));
									if(newSet.isEmpty()) {
										nil = true;
										parent1 = stringify(sos.get(k));
										parent2 = stringify(sos.get(j));
									}
									if(!removeIrrelevant(newSet)) {
										
										if(!sos.contains(newSet) && !newlyFound.contains(newSet)) {
											parents.put(stringify(newSet), stringify(sos.get(k)) + "," + stringify(sos.get(j)));
											indexes.add(stringify(newSet));
										}
										newlyFound.add(newSet);
									} 
								}
							}
						}
					}
				}
			}
			
			
			LinkedHashSet<LinkedHashSet<String>> sos2 = new LinkedHashSet<LinkedHashSet<String>>();
			sos2.addAll(sos);
			sos2.addAll(newlyFound);
			sos.clear();
			sos.addAll(removeRedundant(sos2));
			newlyFound.clear();
			sos2.clear();

			
		}
		
		LinkedList<String> path = new LinkedList<String>();
		TreeSet<Integer> indexOfIndexes = new TreeSet<Integer>();
		path.add(parent2);
		path.add(parent1);

		
		//pracenje puta koje klauzule su koristene za stvaranje nove
		while(!path.isEmpty()) {
			
			if(indexes.indexOf(path.getLast()) > initialClauses.size()) {
				indexOfIndexes.add(indexes.indexOf(path.getLast()));
				String[] newParents = new String[2];
				if(parents.get(path.getLast()) != null) {
					String[] split = parents.get(path.getLast()).split(",");
					if(split != null && split.length == 2) {
						newParents[0] = split[0];
						newParents[1] = split[1];
					}
				}
				path.removeLast();
				if(newParents != null) {
					path.add(newParents[0]);
					path.add(newParents[1]);
				}
			} else {
				path.removeLast();
			}
			
		}
		
		
		//ISPIS
		for(int i = 0; i <= initialClauses.size(); i ++) {
			System.out.println((i+1) + ". " + indexes.get(i));
		}
		System.out.println("===============");
		int remember = initialClauses.size();
		for(Integer i : indexOfIndexes) {
			remember++;
			System.out.println((remember+1) + ". " + indexes.get(i) + " (" + parents.get(indexes.get(i)) + ")");
		}
		if(nil) {
			remember++;
			System.out.println((remember+1) + ". " + "NIL (" + parent1 + "," + parent2 + ")");
		}
		System.out.println("===============");
		if(nil) {
			System.out.println("[CONCLUSION]: " + stringify(goal) + " is true");
		} else {
			System.out.println("[CONCLUSION]: " + stringify(goal) + " is unknown");
		}
	}
	
	
	private static void cooker(List<String> lines2, String book) {
		
		for(String l : lines2) {
			
			if(l.endsWith(" +")) {   //dodaj u book
				ArrayList<String> lines = null;
				try {
					lines = (ArrayList<String>) Files.readAllLines(
							Paths.get(/*"src/main/resources/" +*/ book), 
							StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
				lines.add(l.substring(0, l.length()-2));
				
				Path output = Paths.get(/*"src/main/resources/" + */book);
			    try {
			        Files.write(output, lines);
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
				System.out.println("User’s command: " + l);
				System.out.println("Added " + l.substring(0, l.length()-2));
				System.out.println();
				
			} else if(l.endsWith(" -")) {  //makni iz book
				String removeMe = l.substring(0, l.length()-2);
				ArrayList<String> lines = null;
				try {
					lines = (ArrayList<String>) Files.readAllLines(
							Paths.get(/*"src/main/resources/" + */book), 
							StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
				lines.remove(removeMe);
				
				Path output = Paths.get(/*"src/main/resources/" + */book);
			    try {
			        Files.write(output, lines);
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			    System.out.println("User’s command: " + l);
				System.out.println("Removed " + l.substring(0, l.length()-2));
				System.out.println();
				
				
			} else if(l.endsWith(" ?")) {   //query
				
				String goalLine = l.substring(0, l.length()-2);
				goalLine = goalLine.toLowerCase();
				String[] separateGoalLine = goalLine.split(" v ");
				goal.clear();
				negatedGoal.clear();
				goalClauses.clear();
				initialClauses.clear();
				indexes.clear();
				parents.clear();
				
				List<String> lines = null;
				try {
					lines = Files.readAllLines(
							Paths.get(/*"src/main/resources/" +*/book), 
							StandardCharsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				int lineCounter = 0;
				for (String line: lines) {
					if(!line.startsWith("#")) {
						line = line.toLowerCase();
						
						String[] orStatesOfSingleClause = line.split(" v ");
						
						//eliminiraju se duplici unutar jedne clause
						LinkedHashSet<String> singleClause = new LinkedHashSet<String>();
						for(String state : orStatesOfSingleClause) {
							singleClause.add(state);
						}
						if(!removeIrrelevant(singleClause)) {
							initialClauses.add(singleClause);
						}		
					}
					lineCounter++;
				}
				for(String gl : separateGoalLine) {
					goal.add(gl);
				}
				for(String g : goal) {
					
					if(g.contains("~")) {
						negatedGoal.add(g.replace("~", ""));
						goalClauses.add(g.replace("~", ""));
					} else {
						negatedGoal.add("~" + g);
						goalClauses.add("~" + g);
					}
				}
				System.out.println("User’s command: " + l);
				plResolution();
				
				
			} else {
				System.out.println("Invalid user input: " + l);
			}
			
		}
		
	}


	private static Collection<? extends LinkedHashSet<String>> removeRedundant(LinkedHashSet<LinkedHashSet<String>> sos2) {
		
		LinkedHashSet<LinkedHashSet<String>> remove = new LinkedHashSet<LinkedHashSet<String>>();
		for(LinkedHashSet<String> s1 : sos2) {
			for(LinkedHashSet<String> s2 : sos2) {
				if(!s1.equals(s2)) {
					if(s1.containsAll(s2) && s2.size() == 1) {
						remove.add(s1);
					}
				}
			}
		}
		for(LinkedHashSet<String> s : remove) {
			sos2.remove(s);
		}
		return sos2;
	}


	private static String stringify(LinkedHashSet<String> set) {
		
		String s = "";
		for(String n : set) {
			s = s + n + " v ";
		}	
		if(s.length() > 3) {
			s = s.substring(0, s.length()-3);
		}
		return s;
	}


	private static boolean removeIrrelevant(LinkedHashSet<String> newSet) {
		
		for(String s : newSet) {			
			if(s.contains("~")) {
				
				if(newSet.contains(s.replace("~", ""))) {
					return true;
				}				
			} else {				
				if(newSet.contains("~" + s)) {
					return true;
				}	
			}			
		}			
		return false;
	}
}
