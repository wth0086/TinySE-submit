package edu.hanyang.submit;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.lang3.tuple.*;

public class practice7 implements Comparable<Triple>{
	Triple triple;
	static int i=0;
	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\LG\\git\\TinySE-submit\\tmp\\sorted.data");
		DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file),1024));
		try {
				for(int k=0; k<200;k++) {
					input.readInt();
					input.readInt();
					input.readInt();
				}
				for(int j=0; j<200;j++) {
					System.out.print(input.readInt() + ",");
					System.out.print(input.readInt() + ",");
					System.out.println(input.readInt());
					i++;
					if(i%10 == 0) {
						System.out.println("----------------------------");
						i = 0;
					}
				}
			
		}catch(EOFException e) {
			System.out.println("에러발생");
		}
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
		return -1;
	}

}
