package edu.hanyang.submit;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.tuple.*;

public class project4 implements Comparable<Triple>{
	Triple triple;

	public static void main(String[] args) throws IOException  {
//		File file = new File("C:\\Users\\LG\\git\\TinySE-submit\\src\\test\\resources\\test.data");
//		DataInputStream fi = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
//		
//		Triple triple = Triple.of(fi.readInt(), fi.readInt(), fi.readInt());
//		System.out.println(triple);
//		for(int i=0; i<file.length();i++) {
//			System.out.println(fi.readInt());
//		}
//		PriorityQueue<Triple> priorityQueue = getPriortyQueue();
//		
//		while(!priorityQueue.isEmpty()) {
//			Triple tri = priorityQueue.poll();
//			System.out.println(tri);
//		}
		
		Triple tri1 = Triple.of(5, 2, 1);
		Triple tri2 = Triple.of(5, 1, 3);
		Triple tri3 = Triple.of(2, 4, 6);
		Triple tri4 = Triple.of(2, 4, 5);
		
		System.out.println(tri1.compareTo(tri2));
		System.out.println(tri2.compareTo(tri1));
		System.out.println(tri3.compareTo(tri4));
		System.out.println(tri4.compareTo(tri3));
		System.out.println(tri1.compareTo(tri4));
		
//		System.out.println(2/2);
//		System.out.println(0/2);
		
//		ArrayList<Triple> numbers =new ArrayList<>();
//		numbers.add(tri1);
//		numbers.add(tri2);
//		numbers.add(tri3);
//		System.out.println(numbers.size());
//		System.out.println(numbers.get(1));
//		
//		ArrayList<Triple>[] numbers2 = new ArrayList<Triple>();
//		numbers2[0].add(tri1);
//		numbers2[0].add(tri2);
//		numbers2[1].add(tri1);
//		System.out.println(numbers2);
		
		
		
	}

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
		return 0;
	}
	
	private static PriorityQueue<Triple> getPriortyQueue(){
		Triple tri1 = Triple.of(5, 2, 1);
		Triple tri2 = Triple.of(5, 1, 3);
		Triple tri3 = Triple.of(2, 4, 6);
		Triple tri4 = Triple.of(2, 4, 5);
		Triple tri5 = Triple.of(0, 1, 2);
		Triple tri6 = Triple.of(1, 4, 7);
		Triple tri7 = Triple.of(7, 7, 7);
		
		PriorityQueue<Triple> priorityQueue = new PriorityQueue<Triple>();
		
		priorityQueue.offer(tri1);
		priorityQueue.offer(tri2);
		priorityQueue.offer(tri3);
		priorityQueue.offer(tri4);
		priorityQueue.offer(tri5);
		priorityQueue.offer(tri6);
		priorityQueue.offer(tri7);
		
		return priorityQueue;
	}

}
