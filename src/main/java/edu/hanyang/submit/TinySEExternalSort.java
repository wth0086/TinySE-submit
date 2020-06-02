package edu.hanyang.submit;

import java.io.*;
import java.util.*;
import org.apache.commons.lang3.tuple.*;

import edu.hanyang.indexer.ExternalSort;

//5�� 15�� ���� ����22
//@Ignore("Delete this line to unit test stage 3")
public class TinySEExternalSort implements ExternalSort, Comparable<Triple> {
	Triple triple;
	int realBlockSize;
	int triByte = 12;
	int InitBlockSize; //mergeSort �� �� �ݺ� Ƚ���� ������ M-1 ��
	int N_th=0; //mergeSort �� ��, �ʿ��� �ݺ� Ƚ��
	int K; //���� merge�� ��Ű�� ���� ���
	int check_key; //�޸� üũ�� ���� ���
	ArrayList<String> MiddleArray = new ArrayList<String>();
 	ArrayList<Triple<Integer,Integer,Integer>> tempArray = new ArrayList<Triple<Integer,Integer,Integer>>();
	ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree = new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
	ArrayList<String> FileName = new ArrayList<String>();
	
	
	public void sort(String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException {
		realBlockSize = (int) blocksize/triByte*triByte; //�׽�Ʈ ���� 1020
		InitBlockSize = nblocks-1; //M-1������ ���� ��

		DataInputStream InputFile = new DataInputStream(new BufferedInputStream(new FileInputStream(infile),blocksize));
		
		firstSort(InputFile,tmpdir,nblocks);
		InputFile.close();
		
		K = FileName.size();
		while(K>=nblocks) {
			mergeSort(FileName, blocksize, nblocks, tmpdir);
			K = FileName.size();
		}
		N_th=0; // �߰� merge�� �ʱ�ȭ ���������
		finalMerge(FileName, outfile);
	}
	
	public void firstSort(DataInputStream Input, String tmpdir, int nblocks) throws IOException { //������ ���� QuickSort�� ���� �� ��
		// �־��� Block size��ŭ 1Block�� ����� Block size��ŭ ��� QuickSort ����
		//�޸� �� �ȳ��� ��� �ϳ� ���� �� ���� �ӽ����Ͽ� �����ϵ��� �����ض� -> �����ߴ�
		try {
			while(true) {
				tempArray.clear();
				for(int i=0;i<170500 ;i++) { //�̰� �ٽ� 170500 ���� ���ľ� �� , 186500���� ���
					tempArray.add(Triple.of(Input.readInt(), Input.readInt(), Input.readInt()));
				}
				Collections.sort(tempArray);
				File tempfile = File.createTempFile("temp", ".data", new File(tmpdir));
				tempfile.deleteOnExit();
				FileName.add(tempfile.getAbsolutePath());
				DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile),realBlockSize));
				for(int i=0; i<tempArray.size(); i++) {
					Output.writeInt(tempArray.get(i).getLeft());
					Output.writeInt(tempArray.get(i).getMiddle());
					Output.writeInt(tempArray.get(i).getRight());
				}
				Output.close();
				
			}
		} catch(EOFException e) {
			Collections.sort(tempArray);
			File tempfile = File.createTempFile("temp", ".data", new File(tmpdir));
			tempfile.deleteOnExit();
			FileName.add(tempfile.getAbsolutePath());
			DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile),realBlockSize));
			for(int i=0; i<tempArray.size(); i++) {
				Output.writeInt(tempArray.get(i).getLeft());
				Output.writeInt(tempArray.get(i).getMiddle());
				Output.writeInt(tempArray.get(i).getRight());
			}
			Output.close();
		return;
		}
	}
	
	public void mergeSort(ArrayList<String> FileName,int blocksize, int nblocks,String tmpdir) throws IOException { //���⼭ ���� ����� ����� üũ
		MiddleArray = new ArrayList<String>();
		N_th=0;
		for(int i=0; i<this.FileName.size()/InitBlockSize;i++) { //tempArray.size�� FileName.size�� ����
			File tempfile = File.createTempFile("temp", ".data", new File(tmpdir));
			tempfile.deleteOnExit();
			DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile),realBlockSize));
			Sorting(FileName,Output,InitBlockSize);
			MiddleArray.add(tempfile.getAbsolutePath());
			N_th++;
		}
		File tempfile = File.createTempFile("temp", ".data", new File(tmpdir));
		tempfile.deleteOnExit();
		DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile),realBlockSize));
		Sorting(FileName,Output,this.FileName.size()-InitBlockSize*N_th); //tempArray.size�� FileName.size�� ����
		MiddleArray.add(tempfile.getAbsolutePath());
		this.FileName = MiddleArray;
	}
	
	public void finalMerge(ArrayList<String> FileName, String outfile) throws IOException {
		DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outfile),realBlockSize));
		Sorting(FileName,Output,FileName.size());
	}
	
	public String writeFromTree(ArrayList<Triple<Integer,Integer,Integer>> TripleArray, String tmpdir) throws IOException {
		String fileName = new String();
		File tempfile = File.createTempFile("temp", ".data", new File(tmpdir));
		write(tempfile,TripleArray);
		fileName = tempfile.getName();
		tempfile.deleteOnExit();
		
		return fileName;
	}
	
	public void Sorting(ArrayList<String> firstTempFile,DataOutputStream Output, int BlockNum) throws IOException {
		ArrayList<String> tempFile = new ArrayList<String>();
		boolean checkLoop = true;
		ArrayList<Triple<Integer,Integer,Integer>> tripleArray = new ArrayList<Triple<Integer,Integer,Integer>>();
		tempFile = getTempFile(firstTempFile, BlockNum); //�̰� Count ������Ű�°� ����� ��
		ArrayList<DataInputStream> Input = getInput(tempFile);
		InitTree(Input);
		int floor = checkFloor(Tree.get(0).size());
		
		while(checkLoop) {
			boolean innerCheck = true;
			if(Tree.get(0).size()==1) {
				Output.writeInt(Tree.get(0).get(0).getLeft().getLeft());
				Output.writeInt(Tree.get(0).get(0).getLeft().getMiddle());
				Output.writeInt(Tree.get(0).get(0).getLeft().getRight());
				while(Input.get(Tree.get(0).get(0).getRight()).available() != 0) {
					Output.writeInt(Input.get(Tree.get(0).get(0).getRight()).readInt());
					Output.writeInt(Input.get(Tree.get(0).get(0).getRight()).readInt());
					Output.writeInt(Input.get(Tree.get(0).get(0).getRight()).readInt());
				}
				Output.close();
				Tree.get(0).remove(0);
				checkLoop = false;
			}else {
				while(innerCheck) {
					floor = checkFloor(Tree.get(0).size()); //�߰�
					innerCheck = Re_Compare(Input, floor); //�̷��� �ϴ°� �?
					Output.writeInt(Tree.get(floor).get(0).getLeft().getLeft());
					Output.writeInt(Tree.get(floor).get(0).getLeft().getMiddle());
					Output.writeInt(Tree.get(floor).get(0).getLeft().getRight());
				}
				
				floor = checkFloor(Tree.get(0).size());
				int TreeKey = Tree.get(floor).get(0).getMiddle();
				Tree.get(0).remove(TreeKey);
				ReTree();
			}
		}
		return;
	}
	
	public boolean Re_Compare(ArrayList<DataInputStream> Input, int floor) throws IOException { //AbsoluteKey�� �Ĳ��� Ȯ���غ���, ���⼭ ���� ����Ⱑ ����
		try {
			int term[] = {1,-1}; //compareTo�� ���� ���
			int term2[]	= {0,1}; //���� ���� ���� �����ϱ� ���� ���
			
			int TreeKey = Tree.get(floor).get(0).getMiddle(); //�� �ܰ迡�� ���� ������ ���� ��� Tree Node���� ���Դ��� Ȯ���ϱ� ���� ��
			int AbsoluteKey = Tree.get(floor).get(0).getRight(); //�� �ܰ迡�� ���� ������ ���� ��� File���� ���Դ��� Ȯ���ϱ� ���� ��
			int key = TreeKey;
			
			floor = checkFloor(Tree.get(0).size()); //Tree�� 0���� Node���� ���� �پ��� ���� ����ؼ� ���� ��
			
			for(int i=0; i<floor; i++) {
				if(Tree.get(i).size()%2==1 && key==Tree.get(i).size()-1) {
					Tree.get(i+1).set(key/2, Triple.of(Tree.get(i).get(key).getLeft(), TreeKey, AbsoluteKey));
					key=key/2;
				}else {
					int key2 = Tree.get(i).get(key).getLeft().compareTo(Tree.get(i).get(key+term[key%2]).getLeft());
					AbsoluteKey = Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getRight(); //�� ������ ����� Absolute key�� ��ȯ�ϵ���
					TreeKey = Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getMiddle(); //�� ���� �� ����� Tree Key�� ��ȯ�ϵ���
					Tree.get(i+1).set(key/2,Triple.of(Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getLeft(),TreeKey, AbsoluteKey)); //AbsoluteKey ������ �߸��Ǿ��־���, Tree Key��
					key = key/2;
				}
			}
			check_key = AbsoluteKey;
			Tree.get(0).set(TreeKey, Triple.of(Triple.of(Input.get(AbsoluteKey).readInt(), Input.get(AbsoluteKey).readInt(), Input.get(AbsoluteKey).readInt()), TreeKey, AbsoluteKey));
			return true;
		}catch(EOFException e) {
			Input.get(check_key).close(); //�޸� �����°� ���� ���� �߰��غ�
			return false;
		}
	}
	
	//Tree�� ������ ����Ѵ�. Tree�� ���¸� �����ϱ� ���� �Լ��̴�.
	public void InitTree(ArrayList<DataInputStream> Input) throws IOException{
		int floor = checkFloor(Input.size());
		ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> tempTree = new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Layer = new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		
		for(int i=0; i<Input.size(); i++) {
			Layer.add(Triple.of(Triple.of(Input.get(i).readInt(), Input.get(i).readInt(), Input.get(i).readInt()), i, i));
		}
		tempTree.add(Layer);
		for(int j=0; j<floor; j++) {
			Layer = Compare(Layer);
			tempTree.add(Layer);
		}
		
		Tree = tempTree;
		return;
	}
	
	public ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Compare(ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Layer) {
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> tempLayer = new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		
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
	
	public void ReTree() { //reTree�� size�� 1�̸� ��� �Ǵ°���?
		if(Tree.get(0).size()==1) {
			return;
		}
		ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> tempTree = new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
		ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>> Layer = new ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>();
		int floor = checkFloor(Tree.get(0).size());
		for(int i=0; i<Tree.get(0).size();i++) {
			Layer.add(Triple.of(Tree.get(0).get(i).getLeft(), i, Tree.get(0).get(i).getRight()));
		}
		tempTree.add(Layer);
		for(int i=0; i<floor; i++) {
			Layer = Compare(Layer);
			tempTree.add(Layer);
		}
		
		Tree= tempTree;
		return;
	}
	
	public void write(File OutFile,ArrayList<Triple<Integer,Integer,Integer>> array) throws IOException {
		DataOutputStream Output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(OutFile)));
		for(int i=0; i<array.size(); i++) {
			Output.write(array.get(i).getLeft());
			Output.write(array.get(i).getMiddle());
			Output.write(array.get(i).getRight());
		}
		Output.close();
	}
	
	
	public ArrayList<DataInputStream> getInput(ArrayList<String> File) throws IOException{
		ArrayList<DataInputStream> InputFile = new ArrayList<DataInputStream>();
		for(int i=0; i<File.size();i++) {
			InputFile.add(new DataInputStream(new BufferedInputStream(new FileInputStream(File.get(i)),realBlockSize)));
		}
		
		return InputFile;
	}
	
	public ArrayList<String> getTempFile(ArrayList<String> File,int nblocks){
		ArrayList<String> TempFile = new ArrayList<String>();
		for(int i=0; i<nblocks;i++) { //M-1 ������ ���� n blocks -1 �� ����ŭ �ݺ���Ų��. �ƴϴ� �� �Լ��� �������� ���� n blocks�� �ްڴ�. n blocks - 1 �� ���� �� �ذ�ǵ��� �϶�.
			TempFile.add(File.get(i+InitBlockSize*N_th));
		}
		return TempFile;
	}
	
	//Tree�� LeafNode�� ���� �Է� �޾� Tree�� ���� ���� �����Ѵ�.
	public int checkFloor(int LeafNode) {
		int i = 0;
		LeafNode--;
		LeafNode = LeafNode/2;
		while(LeafNode!=0) {
			LeafNode = LeafNode/2;
			i++;
		}
		return i+1;
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
