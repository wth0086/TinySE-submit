package edu.hanyang.submit;

import java.io.*;

import org.apache.commons.lang3.tuple.*;

public class practice2 {
	static final int NumTri = 3;
	static final int TriByte = 12;

	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\LG\\git\\TinySE-submit\\src\\test\\resources\\test.data");
		DataInputStream fi = new DataInputStream(new BufferedInputStream(new FileInputStream(file),1024));
		int blockByte = (int)1024/TriByte*12;
		int realBlock = (int)file.length()/blockByte; //총필요한 블럭의 갯수에 -1을 한 크기이며 마지막 크기는 완전한 블럭이 아니기에 이렇게 처리함
		int fileLength = (int) file.length();
		String tmpdir = "./tmp";
		String tempfi[] = firstRead(file.getAbsolutePath(), tmpdir, 1024,realBlock,blockByte,fileLength, fi);
		DataInputStream fi2 = new DataInputStream(new BufferedInputStream(new FileInputStream(tempfi[0])));
	}
	
	
	public static Triple[] initial(int blocksize, DataInputStream fi) throws IOException {
		Triple triple[] = new Triple[(int)(blocksize/TriByte)];
		for(int i=0; i<(int)(blocksize/TriByte);i++) {
			int[] temp = new int[3];
			for(int j=0; j<3; j++) {
				temp[j] = fi.readInt();
			}
			triple[i] = Triple.of(temp[0], temp[1], temp[2]);
		}
		return triple;
	}

	public static String[] firstRead(String infile, String tmpdir, int blocksize,int realBlock,int blockByte,int fileLength, DataInputStream fi) throws IOException {
		String tempfi[] = new String[realBlock+1];
		for(int i=0; i<realBlock;i++) {
			Triple[] input = initial(blocksize,fi);
			QuickSort(input);
			File tempfile = File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
			tempfi[i] = tempfile.getName();
			DataOutputStream os = write(tempfile.getName(),input);
			tempfile.deleteOnExit();
		}
		Triple[] input = initial(fileLength-blockByte*realBlock,fi); //infile.length로 했더니 중간에 크기가 줄어서 오류가 남
		QuickSort(input);
		File tempfile = File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
		tempfi[realBlock] = tempfile.getName();
		DataOutputStream os = write(tempfile.getName(),input);
		tempfile.deleteOnExit();
		return tempfi;
	}

	public static DataOutputStream write(String file,Triple[] input) throws IOException {
		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		for(int j=0; j<input.length; j++) {
			os.writeInt((int)input[j].getLeft());
			os.writeInt((int)input[j].getMiddle());
			os.writeInt((int)input[j].getRight());
		}
		os.close();
		return os;
	}

	public static void QuickSort(Triple[] input) {
		Q_sort(input,0,input.length-1);
	}

	private static void Q_sort(Triple[] input, int low, int high) {
		if(low>=high) return;
		
		int mid = partition(input,low,high);
		Q_sort(input,low,mid-1);
		Q_sort(input,mid,high);
	}

	private static int partition(Triple[] input, int low, int high) {
		int pivot = (int)input[(low+high)/2].getLeft();
		while(low<=high) {
			while((int)input[low].getLeft()<pivot) low++;
			while((int)input[high].getLeft()>pivot) high--;
			if(low<=high) {
				swap(input,low,high);
				low++;
				high--;
			}
		}
		return low;
	}

	private static void swap(Triple[] input, int i, int j) {
		Triple tmp = input[i];
		input[i] = input[j];
		input[j] = tmp;
	}
}
