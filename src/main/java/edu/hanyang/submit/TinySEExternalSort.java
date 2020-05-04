package edu.hanyang.submit;

import java.io.*;
import java.util.*;
import org.apache.commons.lang3.tuple.*;
import edu.hanyang.indexer.ExternalSort;

//5월 2일 최종 수정
public class TinySEExternalSort implements ExternalSort, Comparable<Triple> {
	Triple triple;
	int realBlockSize;
	int triByte = 12;
	int InitBlockSize; //mergeSort 할 때 반복 횟수에 곱해줄 M-1 값
	int N_th=0; //mergeSort 할 때, 필요한 반복 횟수
	int K; //최종 merge를 시키기 위한 상수
	int check_key; //메모리 체크를 위한 상수
	ArrayList<ArrayList<Triple<Integer,Integer,Integer>>> firstArray = new ArrayList<ArrayList<Triple<Integer,Integer,Integer>>>();
	ArrayList<String> MiddleArray = new ArrayList<String>();
	ArrayList<Triple<Integer,Integer,Integer>> tempArray = new ArrayList<Triple<Integer,Integer,Integer>>();
	ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>> Tree = new ArrayList<ArrayList<Triple<Triple<Integer,Integer,Integer>,Integer,Integer>>>();
	ArrayList<String> FileName = new ArrayList<String>();
	
	
	public void sort(String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException {
		realBlockSize = (int) blocksize/triByte*triByte; //테스트 예는 1020
		InitBlockSize = nblocks-1; //M-1정렬을 위한 값

		DataInputStream InputFile = new DataInputStream(new BufferedInputStream(new FileInputStream(infile),blocksize));
		firstSort(InputFile,tmpdir,nblocks);
		InputFile.close();
		
		K = FileName.size();
		while(K>=nblocks) {
			mergeSort(FileName, blocksize, nblocks, tmpdir);
			K = FileName.size();
		}
		N_th=0; // 중간 merge값 초기화 시켜줘야지
		finalMerge(FileName, outfile);
	}
	
	public void firstSort(DataInputStream Input, String tmpdir, int nblocks) throws IOException { //시작할 때의 QuickSort를 구현 한 것
		// 주어진 Block size만큼 1Block을 만들고 Block size만큼 묶어서 QuickSort 진행
		//메모리 뻑 안나게 블록 하나 만들 때 마다 임시파일에 저장하도록 수정해라 -> 수정했다
		try {
			while(true) {
//				tempArray = new ArrayList<Triple<Integer,Integer,Integer>>(); //이게 메모리 아웃의 원인일 수 있다.
				tempArray.clear();
				
				for(int i=0;i<170500 ;i++) {
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
	
	public void mergeSort(ArrayList<String> FileName,int blocksize, int nblocks,String tmpdir) throws IOException { //여기서 오류 생기기 쉬우니 체크
		MiddleArray = new ArrayList<String>();
		N_th=0;
		for(int i=0; i<this.FileName.size()/InitBlockSize;i++) { //tempArray.size를 FileName.size로 변경
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
		Sorting(FileName,Output,this.FileName.size()-InitBlockSize*N_th); //tempArray.size를 FileName.size로 변경
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
		tempFile = getTempFile(firstTempFile, BlockNum); //이거 Count 증가시키는거 까먹지 마
		ArrayList<DataInputStream> Input = getInput(tempFile);
		InitTree(Input);
		int floor = checkFloor(Tree.get(0).size());
		
		while(checkLoop) {
			boolean innerCheck = true;
			if(Tree.get(0).size()==1) {
				Output.writeInt(Tree.get(0).get(0).getLeft().getLeft());
				Output.writeInt(Tree.get(0).get(0).getLeft().getMiddle());
				Output.writeInt(Tree.get(0).get(0).getLeft().getRight());
				Output.close();
				Tree.get(0).remove(0);
				checkLoop = false;
			}else {
				while(innerCheck) {
					floor = checkFloor(Tree.get(0).size()); //추가
					innerCheck = Re_Compare(Input, floor); //이렇게 하는건 어때?
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
	
	public boolean Re_Compare(ArrayList<DataInputStream> Input, int floor) throws IOException { //AbsoluteKey값 꼼꼼히 확인해볼것, 여기서 오류 생기기가 쉽네
		try {
			int term[] = {1,-1}; //compareTo를 위한 상수
			int term2[]	= {0,1}; //다음 층의 값을 세팅하기 위한 상수
			
			int TreeKey = Tree.get(floor).get(0).getMiddle(); //전 단계에서 얻은 최적의 값이 어느 Tree Node에서 나왔는지 확인하기 위한 값
			int AbsoluteKey = Tree.get(floor).get(0).getRight(); //전 단계에서 얻은 최적의 값이 어느 File에서 나왔는지 확인하기 위한 값
			int key = TreeKey;
			
			floor = checkFloor(Tree.get(0).size()); //Tree의 0층의 Node들의 수가 줄었을 때를 대비해서 만든 값
			
			for(int i=0; i<floor; i++) {
				if(Tree.get(i).size()%2==1 && key==Tree.get(i).size()-1) {
					Tree.get(i+1).set(key/2, Triple.of(Tree.get(i).get(key).getLeft(), TreeKey, AbsoluteKey));
					key=key/2;
				}else {
					int key2 = Tree.get(i).get(key).getLeft().compareTo(Tree.get(i).get(key+term[key%2]).getLeft());
					AbsoluteKey = Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getRight(); //더 작은값 노드의 Absolute key를 반환하도록
					TreeKey = Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getMiddle(); //더 작은 값 노드의 Tree Key를 반환하도록
					Tree.get(i+1).set(key/2,Triple.of(Tree.get(i).get(key+term[key%2]*term2[(int)(key2+1)/2]).getLeft(),TreeKey, AbsoluteKey)); //AbsoluteKey 설정이 잘못되어있었네, Tree Key도
					key = key/2;
				}
			}
			check_key = AbsoluteKey;
			Tree.get(0).set(TreeKey, Triple.of(Triple.of(Input.get(AbsoluteKey).readInt(), Input.get(AbsoluteKey).readInt(), Input.get(AbsoluteKey).readInt()), TreeKey, AbsoluteKey));
			return true;
		}catch(EOFException e) {
			Input.get(check_key).close(); //메모리 터지는걸 막기 위해 추가해봄
			return false;
		}
	}
	
	//Tree의 시작을 담당한다. Tree의 형태를 구현하기 위한 함수이다.
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
	
	public void ReTree() { //reTree때 size가 1이면 어떻게 되는거지?
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
		for(int i=0; i<nblocks;i++) { //M-1 정렬을 위해 n blocks -1 의 값만큼 반복시킨다. 아니다 이 함수의 안정성을 위해 n blocks로 받겠다. n blocks - 1 은 들어올 때 해결되도록 하라.
			TempFile.add(File.get(i+InitBlockSize*N_th));
		}
		return TempFile;
	}
	
	//Tree의 LeafNode의 수를 입력 받아 Tree의 층의 수를 리턴한다.
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

