package edu.hanyang.submit;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.tuple.*;

public class practice6 implements Comparable<Triple>{
	static int k = 0;
	static String tmpdir = "./tmp";
	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\LG\\git\\TinySE-submit\\src\\test\\resources\\test.data");
		File resultFile = new File("C:\\Temp\\tmp\\sorted.data");
		sort(file.getAbsolutePath(),resultFile.getAbsolutePath(),1024,160);
	}
	
	static final int NumTri = 3;
	static final int TriByte = 12;
	Triple triple;
	static int K;
	int state;
	static int priorConst = -1;
	static int fistBlocks;
	static int length;
	static String tempfi[];
	static ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> new_Tree = 
			new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
	
	public static void sort(String infile, String outfile, int blocksize, int nblocks) throws IOException {
		nblocks = 85; // ���� üũ�� ���� ���� ����
		fistBlocks = nblocks-1; //M-1���� ������
		int blockByte = (int)blocksize/TriByte*TriByte;
//		int realBlock = (int)infile.length()/blockByte; //���ʿ��� ���� ������ -1�� �� ũ���̸� ������ ũ��� ������ ���� �ƴϱ⿡ �̷��� ó����
		int realBlock = 12000000/blockByte;
//		int fileLength = (int)infile.length();
		int fileLength = 12000000;
//		File resultfile = new File(outfile); �ʿ������
		DataInputStream fi = new DataInputStream(new BufferedInputStream(new FileInputStream(infile),blocksize));
		DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outfile),blocksize));
		tempfi = startRead(infile, blocksize, realBlock, blockByte, fileLength, fi);
		System.out.println(tempfi.length); //���� üũ��
		length = realBlock+1; //11765
		K = length;
		nblocks = nblocks-1; //M-1 ����
		while(K>=nblocks) { // K�� ���ϰ� ������Ű�°� �־������.
			K = (int)K/nblocks + 1;
			int len = length;
			priorConst=-1;
			ArrayList<String> outputfi = new ArrayList<String>();
			while(len/nblocks!=0) {
				priorConst++;
				File tempfile = File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
				tempfile.deleteOnExit();
				outputfi.add(tempfile.getName());
				DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile.getName()),blocksize));
				os = PriorSort(os, tempfi,blocksize, nblocks, priorConst); //M-1�� �����ؾ��Ѵ��ؼ� nblocks�� nblocks-1�� �ٲ㺽
				len = len - nblocks;
			}
			priorConst++;
			File tempfile = File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
			tempfile.deleteOnExit();
			DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile.getName()),blocksize));
			os = PriorSort(os, tempfi,blocksize, tempfi.length-((int)tempfi.length/nblocks*nblocks),priorConst); //�̰� initMerge���� �ȹٲٸ� ������ �Ϻκ� ��������.
			outputfi.add(tempfile.getName());
			length = K;
			
			tempfi = outputfi.toArray(new String[0]);
		}
		nblocks = K;
		Output = merge(Output,tempfi,blocksize, nblocks);
		
	}
	public static String[] startRead(String infile, int blocksize, int realBlock,int blockByte,int fileLength, DataInputStream fi) throws IOException{
		String tempfi[] = new String[realBlock+1];
		for(int i=0; i<realBlock;i++) {
			ArrayList<Triple<Integer,Integer,Integer>> array = start(blocksize, fi);
			File tempfile = File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
			tempfi[i] = tempfile.getName();
			DataOutputStream os = write(tempfile.getName(),array);
			tempfile.deleteOnExit();
		}
		ArrayList<Triple<Integer,Integer,Integer>> array = start(fileLength-blockByte*realBlock, fi);
		File tempfile = File.createTempFile("temp_", ".data", new File("C:\\temp"+tmpdir));
		tempfi[realBlock] = tempfile.getName();
		DataOutputStream os = write(tempfile.getName(),array);
		tempfile.deleteOnExit();
		return tempfi;
	}
	
	public static ArrayList<Triple<Integer,Integer,Integer>> start(int blocksize,DataInputStream fi) throws IOException{
		ArrayList<Triple<Integer,Integer,Integer>> array = new ArrayList<Triple<Integer,Integer,Integer>>();
		for(int i=0; i<(int)blocksize/TriByte;i++) {
			array.add(Triple.of(fi.readInt(), fi.readInt(), fi.readInt()));
		}
		Collections.sort(array);
		return array;
	}
	//�̰� writefromtree�� ��ġ�µ�?
	private static DataOutputStream write(String file,ArrayList<Triple<Integer,Integer,Integer>> input) throws IOException {
		DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		for(int j=0; j<input.size(); j++) {
			os.writeInt((int)input.get(j).getLeft());
			os.writeInt((int)input.get(j).getMiddle());
			os.writeInt((int)input.get(j).getRight());
			os.flush();
		}
		return os;
	}
	
	public static DataOutputStream PriorSort(DataOutputStream os, String[] tempfi,int blocksize, int nblocks,int priorConst) throws IOException { //externalSort
		int floor = checkLayer(nblocks); // �̰� sort���� �ѹ� �����ؼ� �ѹ��� �����ϴ°� �����ʳ�???
		ArrayList<DataInputStream> temp_input = initMerge(tempfi,blocksize,nblocks,priorConst);
		
		ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree = 
				TournamentTree(temp_input,tempfi, blocksize,nblocks,priorConst,floor);
		Triple<Triple<Integer,Integer,Integer>,Integer,Integer> result = pop(Tree,floor);
		writeFromTree(os,result.getLeft());
		while(Tree.get(0).size()!=0) { //���ѷ����� �ذ��� �ǳ�? ������ �����ΰ� ������, �Ʊ� ReCompare���� ����ߴ��� �̰Ÿ� �ذ��Ҽ�����������?
			result = ReCompare(temp_input,Tree,floor,tempfi);
			writeFromTree(os,result.getLeft());
		}
		return os;
	}
	
	public static DataOutputStream writeFromTree(DataOutputStream os, Triple<Integer,Integer,Integer> triple) throws IOException {
		os.writeInt((int)triple.getLeft());
		os.writeInt((int)triple.getMiddle());
		os.writeInt((int)triple.getRight());
		os.flush();
		return os;
	}
	
	public static DataOutputStream merge(DataOutputStream os,String[] tempfi, int blocksize, int nblocks) throws IOException {
		int floor = checkLayer(nblocks);
		ArrayList<DataInputStream> Input = initMerge(tempfi,blocksize,nblocks,0);
		ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree = 
				TournamentTree(Input,tempfi, blocksize,nblocks,0,floor); //�ӽù��� ���߿� ����ã�� ���ľ���!!, EOFException
		Triple<Triple<Integer,Integer,Integer>,Integer,Integer> result = pop(Tree,floor);
		writeFromTree(os,result.getLeft());
		while(Tree.get(0).size()!=0) {
			result = ReCompare(Input,Tree,floor,tempfi);
			writeFromTree(os,result.getLeft());
		}
		return os;
	}
	
	//Recompare�Լ��� NullException�� �������Ѵ�.
	public static Triple<Triple<Integer,Integer,Integer>,Integer,Integer>
		ReCompare(ArrayList<DataInputStream> fi,ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree,int floor,String file[]) throws IOException{
		if(Tree.get(0).size()==1) {
			Triple<Triple<Integer,Integer,Integer>,Integer,Integer> result = Tree.get(0).get(0);
			Tree.get(0).remove(0);
			return result;
		}
		int Tree_key = pop(Tree,floor).getMiddle();
		int absolute_key = pop(Tree,floor).getRight();
		try {
			int key = Tree_key;
			int term[] = {1,-1};
			int term2[]	= {0,1};
			Tree.get(0).set(key, Triple.of(MakeTri(fi.get(absolute_key)), Tree_key, absolute_key));
			for(int i=0; i<floor; i++) { //���� �� floor+1�� �Ǿ� �־���?? ,�̰� �����ϴٰ� ���� ��� �� ���� ���� ���ߴ�.
				if(Tree.get(i).size()%2==1&&key==Tree.get(i).size()-1) {
					Tree.get(i+1).set(key/2, Triple.of(Tree.get(i).get(key).getLeft(), key/2, absolute_key));
					key=key/2;
				}else {
					int key2 = Tree.get(i).get(key).getLeft().compareTo(Tree.get(i).get(key+term[key%2]).getLeft());
					Tree.get(i+1).set(key/2,Triple.of(Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getLeft(),key/2, absolute_key));
					key = key/2;
				}
			}
			Triple<Triple<Integer,Integer,Integer>,Integer,Integer> result = pop(Tree,floor);
			return result;
			}catch(EOFException e) { //���� ���� �����ǳ�, reTree���� ��������ҰŰ����� �ƴϾ� ���⼭ ���ǹ����� ������
				//Null����ó���߾��µ� �����ɷ� �ٲ㺽
				Tree.get(0).remove(Tree_key);
			//0���� ũ�Ⱑ �پ����ϱ� floor�� ���� �ٲ��� ������? �׷��� �Ʒ��� ���� ��������. 
			floor = checkLayer(Tree.get(0).size());
			new_Tree = reTree(Tree,floor);
			Triple<Triple<Integer,Integer,Integer>,Integer,Integer> new_Start = ReCompare(fi,new_Tree,floor,file);
			return new_Start;
		}
	}
	
	public static ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> 
			reTree(ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree, int floor){
		
		ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> re_Tree = 
				new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Layer =
				new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		re_Tree.add(Tree.get(0));
		for(int i=0;i<re_Tree.get(0).size();i++) {
			re_Tree.get(0).set(i, Triple.of(Tree.get(0).get(i).getLeft(), i, Tree.get(0).get(i).getRight()));
		}
		for(int j=0;j<floor;j++) {
			Layer = Compare(re_Tree.get(j));
			re_Tree.add(Layer);
		}
		return re_Tree;
	}
	public static ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> 
			TournamentTree(ArrayList<DataInputStream> fi, String tempfi[], int blocksize, int nblocks, int count, int floor) throws IOException {
		
		//clear�� ���� ������ ��ϵȰ͵� empty�� �ǳ�����.
		ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree = 
				new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Layer =
				new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		
		Layer = initTree(fi, blocksize, nblocks);
		Tree.add(Layer);
		for(int i=0; i<floor;i++) {
			Layer = Compare(Tree.get(i));
			Tree.add(Layer);
		}
		return Tree;
	}
	
	public static Triple<Triple<Integer,Integer,Integer>,Integer,Integer> 
		pop(ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree,int floor){
		int key = Tree.get(floor).get(0).getLeft().compareTo(Tree.get(floor).get(1).getLeft());
		Triple<Triple<Integer,Integer,Integer>,Integer,Integer> result = Tree.get(floor).get((key+1)/2);
		return result;
	}
	
	public static ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> 
		Compare(ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Layer){
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> tempLayer = 
				new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		if(Layer.size()%2==0) {
			for(int i=0; i<(Layer.size()/2);i++) {
				int key = Layer.get(i*2).getLeft().compareTo(Layer.get(i*2+1).getLeft());
				int result = (key+1)/2;
				tempLayer.add(Layer.get(i*2+result));
			}
			return tempLayer;
		}else {
			for(int i=0; i<(Layer.size()/2);i++) {
				int key = Layer.get(i*2).getLeft().compareTo(Layer.get(i*2+1).getLeft());
				int result = (key+1)/2;
				tempLayer.add(Layer.get(i*2+result));
			}
			tempLayer.add(Layer.get(Layer.size()-1));
			return tempLayer;
		}
	}
	
	public static ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> 
		initTree(ArrayList<DataInputStream> fi,int blocksize, int nblocks) throws IOException{
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> arrayList = 
					new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		for(int i=0; i<nblocks;i++) { //85��� �׽�Ʈ�� ���� ����, ������ nblocks����. ���� Ʋ���� ������
			arrayList.add(Triple.of(MakeTri(fi.get(i)), i, i));
		}
		return arrayList;
	}
	
	public static ArrayList<DataInputStream> initMerge(String tempfi[],int blocksize, int nblocks,int count) throws IOException{//�̰� ��ü ������ �е��� �����ؾ���. �����Ϸ�
		ArrayList<DataInputStream> fi = new ArrayList<DataInputStream>();
		for(int i=0; i<nblocks; i++) {
			fi.add(new DataInputStream(new BufferedInputStream(new FileInputStream(tempfi[i+fistBlocks*count]),blocksize)));
		}
		return fi;
	}
	
	public static Triple<Integer,Integer,Integer> MakeTri(DataInputStream fi) throws IOException{
		Triple<Integer,Integer,Integer> triple = Triple.of(fi.readInt(), fi.readInt(), fi.readInt());
		return triple;
	}

	public static int checkLayer(int nblocks) {
		int i = 0;
		nblocks--;
		nblocks = nblocks/2;
		while(nblocks!=0) {
			nblocks = nblocks/2;
			i++;
		}
		return i;
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
