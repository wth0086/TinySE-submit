//package edu.hanyang.submit;
//
//import static org.junit.Assert.*;
//
//import java.io.*;
//import java.util.*;
//import org.apache.commons.lang3.tuple.*;
//
//
//public class practice {
//	public static Triple[] initial(int blocksize, DataInputStream fi) throws IOException {
//		Triple triple[] = new Triple[(int)(blocksize/3)];
//		try {
////		Triple triple[] = new Triple[(int)(blocksize/3)];
//		for(int i=0; i<(int)(blocksize/3);i++) {
//			int[] temp = new int[3];
//			for(int j=0; j<3; j++) {
//				temp[j] = fi.readInt();
//			}
//			triple[i] = Triple.of(temp[0], temp[1], temp[2]);
//		}
//		return triple;
//		} catch(EOFException e) {
//			return triple;
//		} catch(NullPointerException e) {
//			ArrayList<Triple> NullTri = new ArrayList<>();
//			for(Triple x : input) {
//				if(x!=null)
//					NullTri.add(x);
//				else
//					break;
//			}
//			return NullTri; //ArrayList로 바꾸기 이거 아이디어 생각하기
//		}
//	}
//	
//	public static void QuickSort(Triple[] input) {
//		sort(input,0,input.length-1);
//	}
//	
//	private static void sort(Triple[] input, int low, int high) {
//		if(low>=high) return;
//		
//		int mid = partition(input,low,high);
//		sort(input,low,mid-1);
//		sort(input,mid,high);
//	}
//	
//	private static int partition(Triple[] input, int low, int high) {
//		int pivot = (int)input[(low+high)/2].getLeft();
//		while(low<=high) {
//			while((int)input[low].getLeft()<pivot) low++;
//			while((int)input[high].getLeft()>pivot) high--;
//			if(low<=high) {
//				swap(input,low,high);
//				low++;
//				high--;
//			}
//		}
//		return low;
//	}
//	
//	private static void swap(Triple[] input, int i, int j) {
//		Triple tmp = input[i];
//		input[i] = input[j];
//		input[j] = tmp;
//	}
//	
//	
////	public static void QuickSort(Triple[] input,int left, int right) { 
////		int pivotpoint = (int)(left+right)/2;
////		if(right>left) {
////			partition(input,left,right,pivotpoint);
////		}
////	}
////	
////	public static void partition(Triple[] input, int left, int right, int pivotpoint) {//제대로 sorting안된다. 오류 찾기
////		int pivot = (int)input[pivotpoint].getLeft();
////		
////		while(left<right) {
////			while((int)input[left].getLeft()<=pivot&&(left!=input.length-1))
////				left++;
////			while((int)input[right].getLeft()>=pivot&&(right!=0))
////				right++;
////			
////			if(left<right) {
////				Triple temp = input[right];
////				input[right] = input[left];
////				input[left] = input[right];
////			}
////		}
////		QuickSort(input,left,pivotpoint-1);
////		QuickSort(input,pivotpoint+1,right);
////	}
//	
//	public static void main(String[] args) throws IOException{
//		File file = new File("C:\\Users\\LG\\git\\TinySE-submit\\src\\test\\resources\\test.data");
//		DataInputStream fi = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
//		int firstRun = ((int)(1020/12)*12);
//		for(int i=0; i<2932;i++) { //2931이 경계선
//			initial(1024,fi);
//		}
//		Triple[] input;
//		input = initial(1024, fi);
////		System.out.println(input[0]);
////		System.out.println(input[340]);
//		for(Triple i : input) {
//			System.out.println(i);
//		}
//		System.out.println("------------");
//		
//		
//		QuickSort(input);
//		for(Triple i : input) {
//			System.out.println(i);
//		}
//		
////		String tmpdir = "./tmp";
////		
////
////			File tempfile =File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
////			System.out.println(tempfile.getAbsolutePath());
////			System.out.println(tempfile.getClass().getName());
////			System.out.println(tempfile.getName());
////			tempfile.deleteOnExit();
//			
////			DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile.getName())));
////			os.writeInt((int) input[0].getLeft());
////			File file2 = new File("C:\\Temp\\tmp\\temp_18151850666954353012.data");
////			DataInputStream fi2 = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
////			System.out.println(fi2.readInt());
//		
////		System.out.println(fi.readInt());
////		if(file.exists()) {
////			try(DataInputStream fi = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
////				int len = (int)file.length();
////				System.out.println("첫번째");
////				for(int i=0; i<12; i++) {
////				System.out.println(fi.readInt());
////				}
////				System.out.println("두번째");
////				for(int i=0; i<12; i++) {
////				System.out.println(fi.readInt());
////				}
////				System.out.println(len);
////			} catch (Exception e) {
////				
////			}
////		}
////		int[] temp = new int[3];
//////		Triple<Integer,Integer,Integer> arg = Triple.of(1, 2, 3);
////		
//////		System.out.println(arg.getMiddle());
////		
////		if(file.exists()) {
////			try(DataInputStream fi = new DataInputStream(new BufferedInputStream(new FileInputStream(file),1024))){
////				int len = (int) file.length();
////				for(int i=0; i<3000000; i++) {
////					fi.readInt();
//////					temp[i%3] = fi.readInt();
////				}
//////				Triple<Integer,Integer,Integer> arg = Triple.of(temp[0], temp[1], temp[2]);
//////				System.out.println(arg.getLeft());
//////				System.out.println(arg.getMiddle());
//////				System.out.println(arg.getRight());
////				fi.readInt();
////				boolean x = (fi == null);
////				System.out.println(x);
////			} catch (Exception e) {
////				
////			}
////		}
//		
//	}
//
//}
//
//
