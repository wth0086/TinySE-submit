package edu.hanyang.submit;

import java.io.*;
import java.nio.*;
import java.util.*;

import edu.hanyang.indexer.BPlusTree;


//��¥��¥ ���� ���⺻ 6�� 8��
public class TinySEBPlusTree implements BPlusTree{
	RandomAccessFile tree;
	Map<Integer,List<Integer>> map = new HashMap<>(); // ĳ�̱���� �����ϱ� ���� ���̴�.
	List<Integer> LRU = new LinkedList<>(); // ĳ�̱���� ���� �� ��, �ֱ��� ���� ��� �ּҿ��� �Դ���, ���� ������ ���� ��� �ּҿ� ���ϴ� ���� �˷��ֱ� ���� ����Ʈ
	List<Integer> Root = new LinkedList<>();
	List<Integer> History = new LinkedList<>(); // Leaf��尡 �� ����, ��� �θ��忡�ٰ� insert�ؾ��ϴ���, �� �θ��嵵 �� ���� ��� �θ��忡 �־���ϴ���, ������ �θ��嵵 �� ����(History ����Ʈ�� �ٺ������) Root�� �־��־�� �Ѵٴ� ��
												// 0�����Ͱ� Leaf���� ���� ����� �θ����.
	String metapath; //1)Root�� ��ġ, 2)height ��  3)blocksize ��
	String savepath;
	int blocksize;
	int inputBlock; //�ڽ� ��尡 ������ blocksize���� 4����Ʈ �� ����Ǵ� ������ ���� ���⿡�� 4�� �����༭ RAF�� ���� ��ġ�� ������ ������ �Ѵ�. => �����Ӱ� 12�� �غ���
	int nblocks = 100; // ������ ���� �� ����������, �츮�� ĳ�̱���� �����ϱ� ���� ���� �����ֵ��� ���� => �׽�Ʈ������ 8�� �س���. �����׽�Ʈ ���� 50���� �س��� ��
	int height; // ���̸� �������ִ� �뵵. �θ���� �ڽĳ�带 �������ֱ� ���� �뵵. ���� ��Ÿ���Ͽ� �������־�� �ϴ� ���̴�.
	int num; // height�� �����ֱ� ���� ��
	int count; // ���ο� ������ RAF��  ��� ��ġ�� ����Ǿ�� �ϴ��� �ּҰ��� �������ֱ� ���� �뵵.
	int selecter[] = {0, 1}; //¦���� ���� 0�� Ȧ���� ���� 1�� ��ȯ�ϵ��� �ϴ� ����
	
	@Override
	public void close() throws IOException {
		File file = new File(metapath);
		if(!file.exists()) { //Insert ���
			file.createNewFile();
			DataOutputStream input = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metapath)));
			input.writeInt(count*inputBlock);
			input.writeInt(height);
			input.writeInt(blocksize);
			input.close();
			writeRoot();
			writeRest();
			tree.close();
		}else { //Search ���
			tree.close();
		}
	}
	
	private void writeRoot() throws IOException {
		byte[] Bytes = ListToByte(Root);
		tree.seek(count*inputBlock);
		tree.write(Bytes);
	}
	
	private void writeRest() throws IOException {
		byte[] Bytes = new byte[inputBlock];
		for(int i=0; i<LRU.size(); i++) {
			tree.seek(LRU.get(i));
			Bytes = ListToByte(map.get(LRU.get(i)));
			tree.write(Bytes);
		}
	}
	
	@Override
	public void insert(int key, int address) throws IOException { //num ������Ű�� ���� ����� ������ Ȯ���ؾ��Ѵ�.
		//�ʱ�ȭ �۾�
		num = 0;
		History.clear();
		
		if(height>0) { //ù ��Ʈ�� �ƴ� ���
			add(key, address);
		}else { //ù ��Ʈ�� ���
			if(Root.size()<(blocksize/4)) {//Root�� ���̸� �ȳѾ��� ��
				firstRoot(key, address);
			}else { //Root�� ���̸� �Ѿ��� ��
				firstRoot(key,address);
				firstSplit(key, address);
			}
		}
	}
	
	//Insert 1�� ������ �� �̴�.
	private void firstRoot(int key, int address) {
		int len = Root.size()/2;
		
		if(!Root.isEmpty()) {
			for(int i=0; i<len; i++) {
				if(key<Root.get(2*i+1)) {
					Root.add(2*i,key);
					Root.add(2*i,address);
					break; //if���� ���� ���� �� ����������� break���� �־�� �ߴ�. �ȱ׷��� �ߺ� ���� �������
				}
			}
			if(key>=Root.get(Root.size()-1)) {
				Root.add(address);
				Root.add(key);
			}
		}else { // ���� ó���� �Է��� ���
			Root.add(address);
			Root.add(key);
		}
	}
	
	//Insert 2���� ������ �� �̴�.
	private void firstSplit(int key, int address) {
		List<Integer> tempList = subList(Root, 0, Root.size()/2 + selecter[(Root.size()/2)%2]);
		tempList.add((count+1)*inputBlock); //���� ����� ���� ���� ����� ��ġ�� �˷��ִ� ���̴�.
		map.put(count*inputBlock, tempList);
		LRU.add(0, count*inputBlock);
		count++;
		
		tempList = subList(Root, Root.size()/2 + selecter[(Root.size()/2)%2], Root.size());
		tempList.add(-666); //-666�� Ʈ���� ������ ���� ���� ����� ������ ��Ÿ����.
		map.put(count*inputBlock, tempList);
		LRU.add(0, count*inputBlock);
		count++;
		
		Root.clear();
		Root.add(LRU.get(1));
		Root.add(tempList.get(1));
		Root.add(LRU.get(0));
		
		height++; // Root�� �ɰ��ϱ� ���̸� ���������ش�.
	}
	
	// Insert 3���� ������ �� �̴�. , ���� �ʿ�
	private void add(int key, int address) throws IOException {
		int nodeAddress = findAtRoot(key); //Root���� ã�� �ּ�
		History.add(nodeAddress);
		
		while(num<height) { //Leaf��忡 ������ ������ while���� ������
			nodeAddress = findAtParent(key, nodeAddress);
			History.add(0,nodeAddress); //����ؿԴ� �θ��� ���� �� �� ����ϰ� -> �̰� Insert�� �������� ����־�� �Ѵ�. -> Insert���� �� �� ���� �����ϴ°ɷ� �߾�
		}
		List<Integer> LeafNode = HitToCache(nodeAddress);
		addToLeaf(LeafNode, key, address);
	}
	
	// Insert 4���� ������ �� �̴�. , ���� �ʿ�
	private void addToLeaf(List<Integer> LeafNode, int key, int address) throws IOException { //map���� ����ͼ� �߰����ָ� �ڵ����� �ݿ��Ǵ��� �˾ƺ���
		int len = LeafNode.size()/2;
		
		if(LeafNode.size()<blocksize/4) { //��忡 ������ �ִٸ�
			for(int i=0; i<len; i++) {
				if(key<LeafNode.get(i*2+1)) {
					LeafNode.add(i*2, key);
					LeafNode.add(i*2, address);
					break;
				}
			}
			if(key>=LeafNode.get(LeafNode.size()-2)) {
				LeafNode.add(LeafNode.size()-1, key);
				LeafNode.add(LeafNode.size()-2,address); //������ add�� ���־ ����Ʈ�� ���̰� �ϳ� �þ�� ���̹Ƿ� -2���־���.
			}
		}else { //��尡 �� á�ٸ� 
			//�켱 key�� address�� �߰����ְ�
			for(int i=0; i<len; i++) {
				if(key<LeafNode.get(i*2+1)) {
					LeafNode.add(i*2, key);
					LeafNode.add(i*2, address);
					break;
				}
			}
			if(key>=LeafNode.get(LeafNode.size()-2)) {
				LeafNode.add(LeafNode.size()-1, key);
				LeafNode.add(LeafNode.size()-2,address); //������ add�� ���־ ����Ʈ�� ���̰� �ϳ� �þ�� ���̹Ƿ� -2���־���.
			}
			
			//������ �ֱ� ����
			split_Leaf(LeafNode); //split_leaf�� ���� �����ұ�? �ڲ� History�� ���� ����ϱ�?? �׸��� addToParent�� Histoy.remove�� ���� ó���Ÿ� ���ִ°���
		}
	}
	
	private void split_Leaf(List<Integer> list) throws IOException {
		List<Integer> tempList;
		int nodeAddress = History.get(0);
		if(History.size()>1) { //��Ʈ��尡 �ƴ� �θ��忡�Լ� ������ LeafNode �� �� -> addToParent
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]);
			tempList.add(count*inputBlock);
			addToCache_split(tempList,nodeAddress);
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock);
			count++;
			
			History.remove(0);
			addToParent(nodeAddress, tempList.get(1), (count-1)*inputBlock);
		}else { //��Ʈ��� �ٷ� �Ʒ��� LeafNode �� �� -> addToRoot
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]);
			tempList.add(count*inputBlock);
			addToCache_split(tempList,nodeAddress);
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock);
			count++;
			
			History.remove(0);
			addToRoot(nodeAddress, tempList.get(1), (count-1)*inputBlock);
		}
	}
	
	//Insert 4, 5�� ����, ���� �ʿ�
	private void split(List<Integer> list) throws IOException {
		List<Integer> tempList;
		int nodeAddress = History.get(0);
		if(History.size()>1) { // ������ �θ��尡 Root��尡 �ƴ� ���
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]+1); //+1 �߰�����
			addToCache_split(tempList,nodeAddress); //split�� ����Ʈ�� ĳ�ÿ� �������־�� �Ѵ�.
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock); //�������� ����Ʈ �� �ι�° ����Ʈ�� RAF�� ���� �߰� �Ǵ� ����̱� ������ ���ο� �ּڰ��� �޾ƾ��Ѵ�.
			count++; //count ��������� ���������ִ°� ����� ��
			
			History.remove(0);
			addToParent(nodeAddress, tempList.get(1), (count-1)*inputBlock); //�������� ����Ʈ�� ��Ÿ���ֱ� ���� ������ �θ� ��忡 �������־���Ѵ�.
		}else{ // ������ �θ��尡 Root����� ���
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]+1);
			addToCache_split(tempList,nodeAddress);
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock);
			count++;
			
			History.remove(0);
			addToRoot(nodeAddress, tempList.get(1), (count-1)*inputBlock);			
		}
	}
	
	//���� �ʿ�
	private void addToRoot(int LeftChild, int key, int RightChild) throws IOException { //�����ؾ��Ѵ�. -> Root�� ������ ���� ������ ��� -> ������ �ɰ����� height�� ������ų �� �ڵ尡 �ٽ� ���� �κ��� ���� ���� ������ ������ ���µ�. Ȥ�� �𸣴� �����غ���
		int len = Root.size()/2; 
		
		if(Root.size()<blocksize/4) { //Root�� ���� ���� ��
			for(int i=0; i<len; i++) {
				if(key<Root.get(i*2+1)) {
					Root.add(i*2+1, RightChild);
					Root.add(i*2+1, key);
					break;
				}
			}
			if(key>=Root.get(Root.size()-2)) {
				Root.add(key);
				Root.add(RightChild);
			}
		}else { //Root�� �� á�ٸ� -> �켱 ���� �߰����ְ� �ɰ��� ����
			for(int i=0; i<len; i++) {
				if(key<Root.get(i*2+1)) {
					Root.add(i*2+1, RightChild);
					Root.add(i*2+1, key);
					break;
				}
			}
			if(key>=Root.get(Root.size()-2)) {
				Root.add(key);
				Root.add(RightChild);
			}
			
			split_Root();
		}
	}
	
	//���� �ʿ�
	private void split_Root() throws IOException {
		List<Integer> tempList;
		tempList = subList(Root, 0, Root.size()/2 + selecter[(Root.size()/2)%2]+1); //+1 �߰�����
//		tempList.add((count+1)*inputBlock); //���� ����� ù��° �� ��������
		addToCache_split(tempList, count*inputBlock); //��Ʈ�� �ɰ��� �ƿ� ���ο� ����̴ϱ�
		count++;
		
		tempList = subList(Root, Root.size()/2 + selecter[(Root.size()/2)%2], Root.size());
		addToCache_split(tempList, count*inputBlock);
		count++;
		
		Root.clear(); //�̰� ��Ʈ Ŭ���� �Ѵٰ� tempList �� ����Ʈ �Ǵ°Ŵ� �ƴϰ���?
		Root.add((count-2)*inputBlock); //�ɰ��� ��Ʈ�� ���� ��带 ����Ű�� �ּҰ�
		Root.add(tempList.get(1)); //�ɰ��� ���� ����� ù��° key ��
		Root.add((count-1)*inputBlock); //�ɰ��� ��Ʈ�� ���� ��带 ����Ű�� �ּҰ�
		
		height++; //Root�� �ɰ��־����� ���̸� ������������
	}
	
	//���� �ʿ�
	private void addToCache_split(List<Integer> list, int address) throws IOException {
		if(map.size()<nblocks) { //ĳ�ÿ� ����� ������ �ִٸ�
			if(map.get(address)==null) {
				LRU.add(0,address);
			}
			map.put(address, list); //������ �ִ� address�� �־��ٸ� �̰ɷ� �˾Ƽ� ��������ٰ� �Ѵ�.
		}else { //ĳ�� ������ �� á�ٸ�
			remove(); //map.remove(LRU.get(LRU.size()-1)) �̰� ���ƴ�.
			if(map.get(address)==null) {
				LRU.add(0,address);
			}
			map.put(address, list);
		}
	}
	
	
	//���� �ʿ�
	private void addToParent(int LeftChild, int ChildKey, int RightChild) throws IOException { //LeftChild-> ���ʿ� �߰��Ǵ� �ڽ� ����� �ּҰ�, ChildKey-> ���� �ڽ� ����� ù��° key��, RightChild->������ �߰��Ǵ� �ڽ� ����� �ּҰ�
		List<Integer> tempList = HitToCache(History.get(0)); //�θ��� Ȥ�ö� ĳ�ÿ� ���� ��쿡
		int len = tempList.size()/2;
		
		if(tempList.size()<blocksize/4) { //�θ��忡 ����� ������ �ִٸ�
			for(int i=0; i<len;i++) {
				if(ChildKey<tempList.get(i*2+1)) {
					tempList.add(i*2+1,RightChild); //�� ��ġ�� �� ������ �߰����ִ°� �´��� �����غ� ��
					tempList.add(i*2+1,ChildKey);
					break;
				}
			}
			if(ChildKey>=tempList.get(tempList.size()-2)) {
				tempList.add(ChildKey);
				tempList.add(RightChild);
			}
		}else { //�θ��尡 �� á�ٸ�
			for(int i=0; i<len;i++) {
				if(ChildKey<tempList.get(i*2+1)) {
					tempList.add(i*2+1,RightChild); //�� ��ġ�� �� ������ �߰����ִ°� �´��� �����غ� ��
					tempList.add(i*2+1,ChildKey);
					break;
				}
			}
			if(ChildKey>=tempList.get(tempList.size()-2)) {
				tempList.add(ChildKey);
				tempList.add(RightChild);
			}
			split(tempList);
		}
	}
	
	//���� �ʿ�
	private int findAtParent(int key, int nodeAddress) throws IOException { // Parent��忡�� key�� ��ġ �� �� ã��
		num++; //�θ𿡼� ��ġ�� ã�´ٴ� ���� �� �� �Ʒ��� ���������̶�� ���̹Ƿ� num�� ���������ش�.
		List<Integer> parentNode = HitToCache(nodeAddress);
		for(int i=0; i<parentNode.size()/2; i++) {
			if(key<parentNode.get(i*2+1)) {
				return parentNode.get(i*2);
			}
		}
		return parentNode.get(parentNode.size()-1); //�ּҸ� �����ϴϱ� ������� 1�� ���� �����ϴ°� �´�.
	}
	
	//���� �ʿ�
	private List<Integer> HitToCache(int nodeAddress) throws IOException { //ĳ�ÿ� �ִ� ����Ʈ�� ���� ���� �޼ҵ�
		if(map.get(nodeAddress)!=null) { //ĳ�ÿ� �ּҿ� �ش��ϴ� ����Ʈ�� �ִٸ�
			LRU.remove((Object)nodeAddress);
			LRU.add(0,nodeAddress);
			return map.get(nodeAddress);
		}else { //ĳ�ÿ� �ּҿ� �ش��ϴ� ����Ʈ�� ���ٸ�
			addToCache(nodeAddress);
			return map.get(nodeAddress);
		}
	}
	
	//���� �ʿ�
	private void addToCache(int nodeAddress) throws IOException{ //ĳ�ÿ� �ش��ϴ� �ּҰ� ���ٸ� �߰����ִ� �޼ҵ�
		if(map.size()<nblocks) { //ĳ�ÿ� ���� ���� ������ �ִٸ�
			map.put(nodeAddress, getList(nodeAddress));
			LRU.add(0,nodeAddress);
		}else { //ĳ�ÿ� ���� ������ ���ٸ�
			remove();
			map.put(nodeAddress, getList(nodeAddress));
			LRU.add(0, nodeAddress);
		}
	}
	
	//���� �ʿ�
	private void remove() throws IOException { //ĳ�ð� �� �������� ���� ���� �Ȱ� �����ش�. ���� ���� �� RAF�� �������ش�.
		//����� ���� �������ִ� �۾�
		int removeObject = LRU.get(LRU.size()-1); //���� ������ �ּ��� ���̴�.
		byte Bytes[] = ListToByte(map.get(removeObject)); // �̰� �̷��� �ǳ�? -> �ȴ�
		tree.seek(removeObject);
		tree.write(Bytes);
		
		//�� ���������� ĳ�ÿ��� �ش� ��带 �������ش�.
		LRU.remove((Object)removeObject); //�Ҿ��ؼ� Object�� �������־���.
		map.remove(removeObject);
	}
	
	private int findAtRoot(int key) { //Root���� Ű�� ���� �� ��ġ�� ã���ִ� �Լ�
		num++; //Root���� �� �� �Ʒ��� ���������̹Ƿ� ���� ���������ش�.
		
		for(int i=0; i<Root.size()/2; i++) { //�̷��� ������  K0 <= A1 < K1 �� �����Ǵ� ���̴�.
			if(key<Root.get(i*2+1))
				return Root.get(i*2);
		}
		
		return Root.get(Root.size()-1);
	}
	
	private List<Integer> subList(List<Integer> list, int start, int end){ //����Ʈ�� sublist�Լ��� ������ ConcurrentModificationException�� �߻��Ͽ� ���� ����.
		List<Integer> tempList = new LinkedList<>();
		for(int i=start; i<end; i++) {
			tempList.add(list.get(i));
		}
		return tempList;
	}
	
	//���� �ʿ�
	private List<Integer> getList(int nodeAddress) throws IOException{
		byte[] Bytes = new byte[inputBlock];
		tree.seek(nodeAddress);
		tree.read(Bytes);
		List<Integer> tempList = ByteToList(Bytes);
		
		return tempList;
	}
	
	//���� �ʿ�
	private List<Integer> ByteToList(byte[] Bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(Bytes);
		List<Integer> tempList = new LinkedList<>();
		for(int i=0; i<Bytes.length/4; i++) {
			if(buffer.getInt(i*4) != -1) { //-1�� ���̸� �ű�� ����Ʈ�� ���̶�� ���̴�.
				tempList.add(buffer.getInt(i*4));
			}else {
				break;
			}
		}
		return tempList;
	}
	
	//���� �ʿ�
	private byte[] ListToByte(List<Integer> list) { // �� ���� allocate�� �ؾ��ϴ� Insert��� Search��� ������ ���߰ڴµ�??
		ByteBuffer buf = ByteBuffer.allocate(inputBlock); //���� �Ҵ����ٶ��� ������ -> �̰� �����ؾߵ�: Hint 32�ε� 36���� �Էµ� 44�Է� �� �ɰ�����
		for(int i=0; i<list.size(); i++) { //putInt�� �� ���� ��ġ�� �˾Ƽ� �Ѿ���� Ȯ���غ���
			buf.putInt(list.get(i));
		}
		for(int j=list.size(); j<inputBlock/4; j++) { //�ٵ� �̷��� �ϸ� �ٽ� �� �� ������? ����غ���
			buf.putInt(-1);
		}
		
		return buf.array();
	}

	@Override
	public void open(String metapath, String savepath, int blocksize, int nblocks) throws IOException {
		File treefile = new File(savepath);
		this.metapath = metapath;
		this.savepath = savepath;
		if(blocksize<=1024) {
			if(blocksize%8==0) {
				this.blocksize = blocksize;
				inputBlock = this.blocksize + 12; 
			}else {
				this.blocksize = (blocksize/8)*8;
				inputBlock = this.blocksize + 12; 
			}
		}else { //blocksize�� 1024���� ũ�ٸ�
			blocksize=1024; //���߿� �����Ұ�
			if(blocksize%8==0) {
				this.blocksize = blocksize;
				inputBlock = this.blocksize + 12; 
			}else {
				this.blocksize = (blocksize/8)*8;
				inputBlock = this.blocksize + 12; 
			}
		}
		
		
		if(!treefile.exists()) { //Insert ���
			treefile.createNewFile();
			tree = new RandomAccessFile(savepath,"rw");
		}else { //Search ���
			tree = new RandomAccessFile(savepath, "rw"); //ĳ���� ���� ������ ���� �͵� �ʿ��ϴ�
			InitSearch();
		}
	}
	
	private void InitSearch() throws IOException {
		DataInputStream metaInput = new DataInputStream(new BufferedInputStream(new FileInputStream(metapath)));
		int RootAddress = metaInput.readInt();
		this.height = metaInput.readInt();
		this.blocksize = metaInput.readInt();
		this.inputBlock = this.blocksize + 12;
		byte[] Bytes = new byte[inputBlock];
		tree.seek(RootAddress);
		tree.read(Bytes);
		Root = ByteToList(Bytes);
		metaInput.close();
	}
	
	@Override
	public int search(int key) throws IOException {
		num = 0;
		int nodeAddress = searchAtRoot(key); //���⼭ num�� 1�̵�
		
		if(height==0) { //Ʈ���� Root�ۿ� ���� ��� =>�ӽ� Test�� �ش�Ǵµ� ����������?
			return nodeAddress;
		}
		while(num<height) { //LeafNode�� �ٷ� ���κ� ������ ������ �ϱ�
			nodeAddress = findAtParent(key, nodeAddress);
		}
		
		nodeAddress = searchAtLeaf(key,nodeAddress);
		
		return nodeAddress; //ã�� ���ϸ� -1 ȣ��
	}
	
	private int searchAtRoot(int key) {
		for(int i=0; i<Root.size()/2; i++) {
			if(key<Root.get(i*2+1)) {
				num++;
				return Root.get(i*2);
			}
		}
		if(Root.size()%2==0) {
			if(key==Root.get(Root.size()-1)) {
				return Root.get(Root.size()-1);
			}else {
				return -1;
			}
		}else {
			num++;
			return Root.get(Root.size()-1);
		}
	}
	
	private int searchAtLeaf(int key,int nodeAddress) throws IOException {
		List<Integer> LeafNode = getList(nodeAddress);
		for(int i=0; i<LeafNode.size()/2; i++) {
			if(key == LeafNode.get(i*2+1)) {
				return LeafNode.get(i*2);
			}
		}
		return -1;
	}
}

