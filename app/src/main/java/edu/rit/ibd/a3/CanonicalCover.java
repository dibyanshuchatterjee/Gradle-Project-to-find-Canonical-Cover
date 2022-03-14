package edu.rit.ibd.a3;

import com.google.common.collect.Sets;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class CanonicalCover {

	public static void main(String[] args) throws Exception {
//		final String relation = "r(A,B,C)";
//		final String fdsStr = "A->B,C;B->C;A->B;A,B->C";
//		final String outputFile = "/Users/dibyanshuchatterjee/Downloads/BigData Assingments/Assingment 3/TNFOutpu";
		final String relation = args[0];
		final String fdsStr = args[1];
		final String outputFile = args[2];
		Set<String> attributes = new HashSet<>();
		Set<String> fds = new HashSet<>();
		// TODO 0: Your code here!
		attributes = parseAttributes(relation); //TODO: check me
		// Parse the input functional dependencies. Recall that attributes can be formed by multiple letters.
		//TODO: check me
		Map<Set<String>, Set<String>> fdInOrder = new HashMap<>(parseFDS(fdsStr)); //function to parse all the attributes
		Map<Set<String>,Set<String>> fdPrime = new HashMap<>(checkForUnion(fdInOrder)); //merging the fds
		System.out.println("28 " + fdPrime);
		Map<Set<String>,Set<String>> fdPrimeAfterLHS =  new HashMap<>(lhsExtraneousRemoval(fdPrime));
		//send the merged fds for lhs removal
		Map<Set<String>,Set<String>> fdPrimeAfterMergeAgain = new HashMap<>(checkForUnion(fdPrimeAfterLHS)); //send the merged fds foranother merge
		Map<Set<String>,Set<String>> fdPrimeAfterRHS = new HashMap<>(rhsExtraneousRemoval(fdPrimeAfterMergeAgain)); //rhs removal
		Map<Set<String>,Set<String>> fdPrimeAfterRHSMerge= new HashMap<>(checkForUnion(fdPrimeAfterRHS));
		// TODO 0: End of your code.
//		System.out.println("printing = " + fdPrimeAfterRHSMerge);
		//TODO: uncomment from here
		for (Map.Entry<Set<String>,Set<String>> map: fdPrimeAfterRHSMerge.entrySet()){
			String LHS = "";
			for (int i = 0; i<map.getValue().size(); i++) {
				List<String> str = new ArrayList<>(map.getValue());
				Collections.sort(str);
				LHS += str.get(i);

				if (i < map.getValue().size() - 1) {
					LHS += "," + " ";
				}
			}
			LHS += " -> ";
			String RHS = "";
			for (int i = 0; i<map.getKey().size(); i++) {
				List<String> str = new ArrayList<>(map.getKey());
				Collections.sort(str);
				RHS += str.get(i);

				if (i < map.getKey().size() - 1) {
					RHS += "," + " ";
				}
			}
			String done = LHS + RHS;
			fds.add(done);

		}
		System.out.println("60 " + fds);
		PrintWriter writer = new PrintWriter(new File(outputFile));
		for (Object fd : fds)
			writer.println(fd);
		writer.close(); //TODO: till here
	}
	public static Set<String> parseAttributes(String relation){
		Set<String> attributesParsed = new HashSet<>();
		int firstBracket = relation.indexOf("(");
		int secondBracket = relation.indexOf(")");
		String toParse = relation.substring(firstBracket+1,secondBracket);
		String[] toStore = toParse.split(",");
		for (String str:toStore){
			String store = str.trim();
			attributesParsed.add(store);
		}


		return  attributesParsed;
	}
	public static Map<Set<String>,Set<String>> parseFDS(String fdStr){
		Map<Set<String>,Set<String>> fds = new HashMap<>();
		String [] str = fdStr.split(";");
		List<ArrayList<String>> listToput = new ArrayList<ArrayList<String>>();
		for (String i:str){
			String trimmed = i.trim();
			String[] splitOnArrow;
			splitOnArrow = trimmed.split("->");
			String [] toMakeListForRight = splitOnArrow[1].split(",");//this goes as key
			String[] toMakeListForLeft = splitOnArrow[0].split(",");
			Set<String> leftList = new HashSet<>();
			for (String j:toMakeListForLeft){
				leftList.add(j.trim());
			}
			Set<String> rightList = new HashSet<>();
			for (String k:toMakeListForRight){
				rightList.add(k.trim());
			}
			fds.put(rightList,leftList);
		}
		return fds;
	}
	public static Map<Set<String>, Set<String>> checkForUnion(Map<Set<String>, Set<String>> fdInOrder){ //flips into right order
		Map<Set<String>, Set<String>> temp = new HashMap<>(fdInOrder);
		Map<Set<String>, Set<String>> reverseMap = new HashMap<>();

		for (Map.Entry<Set<String>,Set<String>> entry : temp.entrySet()) {
			if (!reverseMap.containsKey(entry.getValue())) {
				reverseMap.put(entry.getValue(), new HashSet<>());
			}
			Set<String> keys = reverseMap.get(entry.getValue());
			keys.addAll(entry.getKey());
			reverseMap.put(entry.getValue(), keys);
		}
		return reverseMap;
	}
	public static Map<Set<String>, Set<String>> lhsExtraneousRemoval(Map<Set<String>, Set<String>> fdPrime){
		Map<Set<String>, Set<String>> copyMap = new HashMap<>();
		Map<Set<String>, Set<String>> firstCopy = new HashMap<>(fdPrime);
		for (Map.Entry<Set<String>, Set<String>> entry:fdPrime.entrySet()){
			if(entry.getKey().size()>1){
				List<String> newList = new ArrayList<>(entry.getKey());
				for (int size = 1; size<entry.getKey().size(); size++){
					for (Set<String> comb:Sets.combinations(entry.getKey(),size)){
						newList.removeAll(comb);
						if (computeClosureForLHS(fdPrime,new HashSet<>(newList)).containsAll(entry.getValue())){
							copyMap.put(entry.getValue(),new HashSet<>(newList));//TODO: pay attention..value is lhs again
							firstCopy.remove(entry.getKey());
						}
						newList.addAll(comb);

					}
				}
			}
		}
		for (Map.Entry<Set<String>, Set<String>> passing:firstCopy.entrySet()){
			copyMap.put(passing.getValue(),passing.getKey());
		}
		return copyMap;
	}
	public static Map<Set<String>, Set<String>> rhsExtraneousRemoval(Map<Set<String>,Set<String>> fdPrimeAfterMergeAgain){
//		Map<Set<String>, Set<String>> result = new HashMap<>(fdPrimeAfterMergeAgain);
//		boolean flag = false;
//		for (Map.Entry<Set<String>, Set<String>> entry: fdPrimeAfterMergeAgain.entrySet()){
//			if (entry.getValue().size() > 1){
//				Set<String> anotherNewList = new HashSet<>(entry.getKey());
//				for (String str: entry.getKey()){
//					Map<Set<String>, Set<String>> copyMap = new HashMap<>(fdPrimeAfterMergeAgain);
//					anotherNewList.remove(str);
//					copyMap.remove(entry.getKey());
//					copyMap.put(anotherNewList,entry.getValue());
//					if (computeClosureforRHS(copyMap,entry.getValue()).contains(str)){
//						result.put(new HashSet<>(entry.getValue()),new HashSet<>(anotherNewList));
//						flag = true;
//					}
//
//
//				}
//			}
//		}
//		if (flag){
//			return result;
//		}
//		return fdPrimeAfterMergeAgain;
		Map<Set<String>, Set<String>> firstCopy = new HashMap<>(fdPrimeAfterMergeAgain);
		Map<Set<String>, Set<String>> result = new HashMap<>();
		for (Map.Entry<Set<String>, Set<String>> entry: fdPrimeAfterMergeAgain.entrySet()){
			Set<String> values = new HashSet<>(entry.getValue());
			for (int size = 1; size<entry.getValue().size(); size++){
				for (Set<String> combos : Sets.combinations(entry.getValue(),size)){
					Map<Set<String>, Set<String>> copy = new HashMap<>(fdPrimeAfterMergeAgain);
					values.removeAll(combos);
//					System.out.println("copy before = " + copy);
					copy.put(entry.getKey(),values);
//					System.out.println("copy after = " + copy);
//					System.out.println("Closure after removing " + combos + " from " + entry.getValue() + " is " + computeClosureforRHS(copy,entry.getKey()));
					if (computeClosureforRHS(copy,entry.getKey()).containsAll(combos)){
						fdPrimeAfterMergeAgain.put(entry.getKey(),values);
						Set<String> tempSet = new HashSet<>(values);
						result.put(entry.getKey(),tempSet);
						firstCopy.remove(entry.getKey());
					}
					values.addAll(combos);
//					copy.put(entry.getKey(),entry.getValue());
				}
			}
		}
		result.putAll(firstCopy);
		return result;
	}

	public static Set<String> computeClosureForLHS(Map<Set<String>,Set<String>> fdInOrder, Set<String> newList) {
		Set<String> temp = new HashSet<>(newList);
		boolean flag = false;
		for (Map.Entry<Set<String>,Set<String>> entry:fdInOrder.entrySet()){
			for (int size = 1; size<=newList.size(); size++){
				for (Set<String> comb: Sets.combinations(newList,size)){
					if (comb.containsAll(entry.getKey())){
						if (!newList.containsAll(entry.getValue())){
							flag = true;
							temp.addAll(entry.getValue());
						}
					}
				}
			}
		}
		if (flag){
			return computeClosureForLHS(fdInOrder,temp);
		}
		return temp;
	}
	public static Set<String> computeClosureforRHS(Map<Set<String>,Set<String>> copyMap, Set<String> theGetKey){
		Set<String> temp = new HashSet<>(theGetKey);
		boolean flag = false;
		for (Map.Entry<Set<String>,Set<String>> entry:copyMap.entrySet()){
			for (int size = 1; size<=theGetKey.size(); size++){
				for (Set<String> comb:Sets.combinations(theGetKey,size)){
					if (comb.containsAll(entry.getKey())){
						if (!theGetKey.containsAll(entry.getValue())){
							flag = true;
							temp.addAll(entry.getValue());
						}
					}
				}
			}
		}
		if (flag){
			return computeClosureforRHS(copyMap,temp);
		}
		return temp;
	}
}
