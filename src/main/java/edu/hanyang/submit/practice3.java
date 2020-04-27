package edu.hanyang.submit;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.tuple.*;

public class practice3 {
	public static void main(String[] args) throws IOException {
		ArrayList<ArrayList<Triple<Integer,Integer,Integer>>> firstArray = new ArrayList<ArrayList<Triple<Integer,Integer,Integer>>>();
		ArrayList<Triple<Integer,Integer,Integer>> tempArray = new ArrayList<Triple<Integer,Integer,Integer>>();
		File file = new File("C:\\Users\\LG\\git\\TinySE-submit\\src\\test\\resources\\test.data");
		DataInputStream Input = new DataInputStream(new BufferedInputStream(new FileInputStream(file),1024));
		try {
		while(true) {
			tempArray = new ArrayList<Triple<Integer,Integer,Integer>>();
			for(int i=0;i<13600;i++) {
				tempArray.add(Triple.of(Input.readInt(), Input.readInt(), Input.readInt()));
			}
			firstArray.add(tempArray);
		}
	} catch(EOFException e) {
		firstArray.add(tempArray);
		System.out.println("에러 발생");
	}
		System.out.println(firstArray.size());
		System.out.println(firstArray.get(0).size());
		System.out.println(firstArray.get(18).size());
		System.out.println(firstArray.get(firstArray.size()-1).size());
		System.out.println(firstArray.get(firstArray.size()-1).get(firstArray.get(firstArray.size()-1).size()-1));
		System.out.println("이제는 성공하고싶어요.");
		
	}
}
