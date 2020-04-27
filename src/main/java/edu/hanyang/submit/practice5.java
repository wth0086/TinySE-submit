package edu.hanyang.submit;

import java.util.*;

import org.apache.commons.lang3.tuple.*;

import edu.hanyang.indexer.*;

public class practice5 implements Comparable<Triple>{
	
	public static void main(String[] args) {
		ArrayList<ArrayList<Triple>> arraylist = new ArrayList<ArrayList<Triple>>();
		ArrayList<Triple> triple = new ArrayList<Triple>();
		ArrayList<Triple> triple2 = new ArrayList<Triple>();
		triple.add(Triple.of(1, 2, 3));
		triple.add(Triple.of(2, 3, 4));
		arraylist.add(triple);
		triple2.add(Triple.of(3, 2, 1));
		triple2.add(Triple.of(2, 3, 1));
		triple2.add(Triple.of(7, 1, 4));
		arraylist.add(triple2);
		System.out.println(arraylist);
//		arraylist.get(0).remove(0);
//		arraylist.get(0).remove(0);
//		if(arraylist.get(0).size() == 0) {
//			System.out.println("이거");
//		}
//		System.out.println(arraylist.get(0));
//		System.out.println(arraylist.get(1).get(1));
		
		ArrayList<ArrayList<Triple>> arraylist2 = new ArrayList<ArrayList<Triple>>();
		arraylist2 = null;
		System.out.println(arraylist2);
		arraylist2 = arraylist;
		System.out.println(arraylist2);
//		arraylist = arraylist2;
//		System.out.println(arraylist2);
//		arraylist2.add(null);
//		System.out.println(arraylist2.get(0));
//		triple.clear();
//		triple.add(Triple.of(3, 2, 1));
//		triple.add(Triple.of(2, 3, 1));
//		triple.add(Triple.of(7, 1, 4));
//		arraylist.add(triple);
//		
//		
//		System.out.println(arraylist);
//		Triple triple = null;
//		Triple triple2 = Triple.of(1, 2, 3);
//		Pair pair = Pair.of(triple, 0);
//		System.out.println(pair);
//		ArrayList<Triple> arraylist = new ArrayList<Triple>();
//		arraylist.add(triple);
//		result(triple,triple2);

		
	}
	
//	public static int result(Triple triple, Triple triple2) {
//		try {
//			int x = triple.compareTo(triple2);
//			return x;
//		}catch(NullPointerException e) {
//			System.out.println("null오류발생");
//			return 0;
//		}
//	}
	private Triple triple;
	@Override
	public int compareTo(Triple o) {
		if((int)this.triple.getLeft()>(int)o.getLeft()) {
			return 1;
		}else if((int)this.triple.getLeft()==(int)o.getLeft()) {
			if((int)this.triple.getMiddle()>(int)o.getMiddle()) {
				return 1;
			}else if((int)this.triple.getMiddle()==(int)o.getMiddle()) {
				if((int)this.triple.getRight()>(int)o.getRight()) {
					return 1;
				}
			}
		}
		return -1;
	}
}
