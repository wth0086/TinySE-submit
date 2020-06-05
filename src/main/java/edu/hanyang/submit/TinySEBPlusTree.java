package edu.hanyang.submit;

import java.io.*;
import java.nio.*;
import java.util.*;

import edu.hanyang.indexer.BPlusTree;


//중간테스트 통과
public class TinySEBPlusTree implements BPlusTree{
	RandomAccessFile tree;
	Map<Integer,List<Integer>> map = new HashMap<>(); // 캐싱기법을 구현하기 위한 맵이다.
	List<Integer> LRU = new LinkedList<>(); // 캐싱기법을 구현 할 때, 최근의 노드는 어느 주소에서 왔는지, 가장 오래된 노드는 어느 주소에 속하는 지를 알려주기 위한 리스트
	List<Integer> Root = new LinkedList<>();
	List<Integer> History = new LinkedList<>(); // Leaf노드가 꽉 차면, 어느 부모노드에다가 insert해야하는지, 그 부모노드도 다 차면 어느 부모노드에 넣어야하는지, 마지막 부모노드도 꽉 차면(History 리스트가 다비워지면) Root에 넣어주어야 한다는 뜻
												// 0번부터가 Leaf노드와 가장 가까운 부모노드다.
	String metapath; //1)Root의 위치, 2)height 값  3)blocksize 값
	String savepath;
	int blocksize;
	int inputBlock; //자식 노드가 기존의 blocksize보다 4바이트 더 저장되는 현상을 위해 여기에는 4를 더해줘서 RAF에 값이 겹치는 문제가 없도록 한다. => 여유롭게 12로 해보자
	int nblocks = 200; // 원래는 시작 때 정해주지만, 우리는 캐싱기법을 적용하기 위해 값을 정해주도록 하자 => 테스트때문에 8로 해놓음. 최종테스트 때는 50으로 해놓을 것
	int height; // 높이를 저장해주는 용도. 부모노드와 자식노드를 구분해주기 위한 용도. 또한 메타파일에 저장해주어야 하는 값이다.
	int num; // height와 비교해주기 위한 값
	int count; // 새로운 노드들이 RAF의  어느 위치에 저장되어야 하는지 주소값을 저장해주기 위한 용도.
	int selecter[] = {0, 1}; //짝수일 때는 0을 홀수일 때는 1을 반환하도록 하는 아이
	
	@Override
	public void close() throws IOException {
		File file = new File(metapath);
		if(!file.exists()) { //Insert 모드
			file.createNewFile();
			DataOutputStream input = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metapath)));
			input.writeInt(count*inputBlock);
			input.writeInt(height);
			input.writeInt(blocksize);
			input.close();
			writeRoot();
			writeRest();
			tree.close();
		}else { //Search 모드
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
	public void insert(int key, int address) throws IOException { //num 증가시키는 곳이 제대로 들어가는지 확인해야한다.
		//초기화 작업
		num = 0;
		History.clear();
		
		if(height>0) { //첫 루트가 아닌 경우
			add(key, address);
		}else { //첫 루트인 경우
			if(Root.size()<(blocksize/4)) {//Root가 길이를 안넘었을 때
				firstRoot(key, address);
			}else { //Root가 길이를 넘었을 때
				firstRoot(key,address);
				firstSplit(key, address);
			}
		}
	}
	
	//Insert 1번 구현한 것 이다.
	private void firstRoot(int key, int address) {
		int len = Root.size()/2;
		
		if(!Root.isEmpty()) {
			for(int i=0; i<len; i++) {
				if(key<Root.get(2*i+1)) {
					Root.add(2*i,key);
					Root.add(2*i,address);
					break; //if문을 만족 했을 때 빠져나가라는 break문이 있어야 했다. 안그러면 중복 값이 생기더라
				}
			}
			if(key>=Root.get(Root.size()-1)) {
				Root.add(address);
				Root.add(key);
			}
		}else { // 제일 처음의 입력인 경우
			Root.add(address);
			Root.add(key);
		}
	}
	
	//Insert 2번을 구현한 것 이다.
	private void firstSplit(int key, int address) {
		List<Integer> tempList = subList(Root, 0, Root.size()/2 + selecter[(Root.size()/2)%2]);
		tempList.add((count+1)*inputBlock); //좌측 노드의 끝에 다음 노드의 위치를 알려주는 값이다.
		map.put(count*inputBlock, tempList);
		LRU.add(0, count*inputBlock);
		count++;
		
		tempList = subList(Root, Root.size()/2 + selecter[(Root.size()/2)%2], Root.size());
		tempList.add(-666); //-666은 트리의 한층의 가장 끝의 노드의 끝임을 나타낸다.
		map.put(count*inputBlock, tempList);
		LRU.add(0, count*inputBlock);
		count++;
		
		Root.clear();
		Root.add(LRU.get(1));
		Root.add(tempList.get(1));
		Root.add(LRU.get(0));
		
		height++; // Root를 쪼개니까 높이를 증가시켜준다.
	}
	
	// Insert 3번을 구현한 것 이다. , 점검 필요
	private void add(int key, int address) throws IOException {
		int nodeAddress = findAtRoot(key); //Root에서 찾은 주소
		History.add(nodeAddress);
		
		while(num<height) { //Leaf노드에 도달할 때까지 while문을 돌려라
			nodeAddress = findAtParent(key, nodeAddress);
			History.add(0,nodeAddress); //사용해왔던 부모노드 추적 할 때 사용하게 -> 이거 Insert가 끝날때는 비워주어야 한다. -> Insert시작 할 때 비우고 시작하는걸로 했어
			num++; //num을 여기서 증가시켜주는게 맞겠지?
		}
		List<Integer> LeafNode = HitToCache(nodeAddress);
		addToLeaf(LeafNode, key, address);
	}
	
	// Insert 4번을 구현한 것 이다. , 점검 필요
	private void addToLeaf(List<Integer> LeafNode, int key, int address) throws IOException { //map에서 갖고와서 추가해주면 자동으로 반영되는지 알아보자
		int len = LeafNode.size()/2;
		
		if(LeafNode.size()<blocksize/4) { //노드에 여유가 있다면
			for(int i=0; i<len; i++) {
				if(key<LeafNode.get(i*2+1)) {
					LeafNode.add(i*2, key);
					LeafNode.add(i*2, address);
					break;
				}
			}
			if(key>=LeafNode.get(LeafNode.size()-2)) {
				LeafNode.add(LeafNode.size()-1, key);
				LeafNode.add(LeafNode.size()-2,address); //위에서 add를 해주어서 리스트의 길이가 하나 늘어났을 것이므로 -2해주었다.
			}
		}else { //노드가 꽉 찼다면 
			//우선 key와 address를 추가해주고
			for(int i=0; i<len; i++) {
				if(key<LeafNode.get(i*2+1)) {
					LeafNode.add(i*2, key);
					LeafNode.add(i*2, address);
					break;
				}
			}
			if(key>=LeafNode.get(LeafNode.size()-2)) {
				LeafNode.add(LeafNode.size()-1, key);
				LeafNode.add(LeafNode.size()-2,address); //위에서 add를 해주어서 리스트의 길이가 하나 늘어났을 것이므로 -2해주었다.
			}
			
			//나누어 주기 시작
			split_Leaf(LeafNode); //split_leaf로 따로 구현할까? 자꾸 History에 문제 생기니까?? 그리고 addToParent의 Histoy.remove를 제일 처음거를 없애는거지
		}
	}
	
	private void split_Leaf(List<Integer> list) throws IOException {
		List<Integer> tempList;
		int nodeAddress = History.get(0);
		if(History.size()>1) { //루트노드가 아닌 부모노드에게서 내려온 LeafNode 일 때 -> addToParent
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]);
			tempList.add(count*inputBlock);
			addToCache_split(tempList,nodeAddress);
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock);
			count++;
			
			History.remove(0);
			addToParent(nodeAddress, tempList.get(1), (count-1)*inputBlock);
		}else { //루트노드 바로 아래가 LeafNode 일 때 -> addToRoot
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
	
	//Insert 4, 5번 구현, 점검 필요
	private void split(List<Integer> list) throws IOException {
		List<Integer> tempList;
		int nodeAddress = History.get(0);
		if(History.size()>1) { // 참조할 부모노드가 Root노드가 아닌 경우
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]+1); //+1 추가해줘
			addToCache_split(tempList,nodeAddress); //split한 리스트를 캐시에 저장해주어야 한다.
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock); //나누어진 리스트 중 두번째 리스트는 RAF에 새로 추가 되는 노드이기 때문에 새로운 주솟값을 받아야한다.
			count++; //count 사용했으면 증가시켜주는거 까먹지 마
			
			History.remove(0);
			addToParent(nodeAddress, tempList.get(1), (count-1)*inputBlock); //나누어진 리스트를 나타내주기 위한 값들을 부모 노드에 저장해주어야한다.
		}else{ // 참조할 부모노드가 Root노드인 경우
			tempList = subList(list,0,list.size()/2 + selecter[(list.size()/2)%2]+1);
			addToCache_split(tempList,nodeAddress);
			tempList = subList(list,list.size()/2 + selecter[(list.size()/2)%2],list.size());
			addToCache_split(tempList,count*inputBlock);
			count++;
			
			History.remove(0);
			addToRoot(nodeAddress, tempList.get(1), (count-1)*inputBlock);			
		}
	}
	
	//점검 필요
	private void addToRoot(int LeftChild, int key, int RightChild) throws IOException { //구현해야한다. -> Root가 꽉차는 경우와 안차는 경우 -> 꽉차서 쪼개고나서 height를 증가시킬 때 코드가 다시 들어가는 부분이 없어 무한 루프의 걱정은 없는데. 혹시 모르니 점검해볼것
		int len = Root.size()/2; 
		
		if(Root.size()<blocksize/4) { //Root가 여유 있을 때
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
		}else { //Root가 꽉 찼다면 -> 우선 값을 추가해주고 쪼개기 진행
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
	
	//점검 필요
	private void split_Root() throws IOException {
		List<Integer> tempList;
		tempList = subList(Root, 0, Root.size()/2 + selecter[(Root.size()/2)%2]+1); //+1 추가해줌
//		tempList.add((count+1)*inputBlock); //우측 노드의 첫번째 값 가져오기
		addToCache_split(tempList, count*inputBlock); //루트를 쪼개면 아예 새로운 노드이니까
		count++;
		
		tempList = subList(Root, Root.size()/2 + selecter[(Root.size()/2)%2], Root.size());
		addToCache_split(tempList, count*inputBlock);
		count++;
		
		Root.clear(); //이거 루트 클리어 한다고 tempList 빈 리스트 되는거는 아니겠지?
		Root.add((count-2)*inputBlock); //쪼개진 루트의 좌측 노드를 가리키는 주소값
		Root.add(tempList.get(1)); //쪼개진 우측 노드의 첫번째 key 값
		Root.add((count-1)*inputBlock); //쪼개진 루트의 우측 노드를 가리키는 주소값
		
		height++; //Root를 쪼개주었으니 높이를 증가시켜주자
	}
	
	//점검 필요
	private void addToCache_split(List<Integer> list, int address) throws IOException {
		if(map.size()<nblocks) { //캐시에 충분한 공간이 있다면
			if(map.get(address)==null) {
				LRU.add(0,address);
			}
			map.put(address, list); //기존에 있던 address가 있었다면 이걸로 알아서 덮어씌워진다고 한다.
		}else { //캐시 공간이 꽉 찼다면
			remove(); //map.remove(LRU.get(LRU.size()-1)) 이걸 고쳤다.
			if(map.get(address)==null) {
				LRU.add(0,address);
			}
			map.put(address, list);
		}
	}
	
	
	//점검 필요
	private void addToParent(int LeftChild, int ChildKey, int RightChild) throws IOException { //LeftChild-> 왼쪽에 추가되는 자식 노드의 주소값, ChildKey-> 우측 자식 노드의 첫번째 key값, RightChild->우측에 추가되는 자식 노드의 주소값
		List<Integer> tempList = HitToCache(History.get(0)); //부모노드 혹시라도 캐시에 없을 경우에
		int len = tempList.size()/2;
		
		if(tempList.size()<blocksize/4) { //부모노드에 충분한 여유가 있다면
			for(int i=0; i<len;i++) {
				if(ChildKey<tempList.get(i*2+1)) {
					tempList.add(i*2+1,RightChild); //이 위치에 이 순서로 추가해주는게 맞는지 생각해볼 것
					tempList.add(i*2+1,ChildKey);
					break;
				}
			}
			if(ChildKey>=tempList.get(tempList.size()-2)) {
				tempList.add(ChildKey);
				tempList.add(RightChild);
			}
		}else { //부모노드가 꽉 찼다면
			for(int i=0; i<len;i++) {
				if(ChildKey<tempList.get(i*2+1)) {
					tempList.add(i*2+1,RightChild); //이 위치에 이 순서로 추가해주는게 맞는지 생각해볼 것
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
	
	//점검 필요
	private int findAtParent(int key, int nodeAddress) throws IOException { // Parent노드에서 key가 위치 할 곳 찾기
		num++; //부모에서 위치를 찾는다는 것은 한 층 아래로 내려갈것이라는 뜻이므로 num을 증가시켜준다.
		List<Integer> parentNode = HitToCache(nodeAddress);
		for(int i=0; i<parentNode.size()/2; i++) {
			if(key<parentNode.get(i*2+1)) {
				return parentNode.get(i*2);
			}
		}
		return parentNode.get(parentNode.size()-1); //주소를 리턴하니까 사이즈에서 1을 뺀걸 리턴하는게 맞다.
	}
	
	//점검 필요
	private List<Integer> HitToCache(int nodeAddress) throws IOException { //캐시에 있는 리스트를 갖고 오는 메소드
		if(map.get(nodeAddress)!=null) { //캐시에 주소에 해당하는 리스트가 있다면
			
			return map.get(nodeAddress);
		}else { //캐시에 주소에 해당하는 리스트가 없다면
			addToCache(nodeAddress);
			return map.get(nodeAddress);
		}
	}
	
	//점검 필요
	private void addToCache(int nodeAddress) throws IOException{ //캐시에 해당하는 주소가 없다면 추가해주는 메소드
		if(map.size()<nblocks) { //캐시에 아직 여유 공간이 있다면
			map.put(nodeAddress, getList(nodeAddress));
			LRU.add(0,nodeAddress);
		}else { //캐시에 여유 공간이 없다면
			remove();
			map.put(nodeAddress, getList(nodeAddress));
			LRU.add(0, nodeAddress);
		}
	}
	
	//점검 필요
	private void remove() throws IOException { //캐시가 꽉 차있으면 제일 오래 된걸 지워준다. 지울 때는 꼭 RAF에 저장해준다.
		//지우기 전에 저장해주는 작업
		int removeObject = LRU.get(LRU.size()-1); //제일 오래된 주소의 값이다.
		byte Bytes[] = ListToByte(map.get(removeObject)); // 이거 이렇게 되나? -> 된다
		tree.seek(removeObject);
		tree.write(Bytes);
		
		//다 저장했으면 캐시에서 해당 노드를 삭제해준다.
		LRU.remove((Object)removeObject); //불안해서 Object로 선언해주었다.
		map.remove(removeObject);
	}
	
	private int findAtRoot(int key) { //Root에서 키가 들어가야 할 위치를 찾아주는 함수
		num++; //Root에서 한 층 아래로 내려갈것이므로 값을 증가시켜준다.
		
		for(int i=0; i<Root.size()/2; i++) { //이렇기 때문에  K0 <= A1 < K1 이 성립되는 것이다.
			if(key<Root.get(i*2+1))
				return Root.get(i*2);
		}
		
		return Root.get(Root.size()-1);
	}
	
	private List<Integer> subList(List<Integer> list, int start, int end){ //리스트의 sublist함수를 쓰려니 ConcurrentModificationException이 발생하여 만들어서 쓴다.
		List<Integer> tempList = new LinkedList<>();
		for(int i=start; i<end; i++) {
			tempList.add(list.get(i));
		}
		return tempList;
	}
	
	//점검 필요
	private List<Integer> getList(int nodeAddress) throws IOException{
		byte[] Bytes = new byte[inputBlock];
		tree.seek(nodeAddress);
		tree.read(Bytes);
		List<Integer> tempList = ByteToList(Bytes);
		
		return tempList;
	}
	
	//점검 필요
	private List<Integer> ByteToList(byte[] Bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(Bytes);
		List<Integer> tempList = new LinkedList<>();
		for(int i=0; i<Bytes.length/4; i++) {
			if(buffer.getInt(i*4) != -1) { //-1이 보이면 거기는 리스트의 끝이라는 뜻이다.
				tempList.add(buffer.getInt(i*4));
			}else {
				break;
			}
		}
		return tempList;
	}
	
	//점검 필요
	private byte[] ListToByte(List<Integer> list) { // 쓸 때는 allocate로 해야하니 Insert모드 Search모드 나누어 봐야겠는데??
		ByteBuffer buf = ByteBuffer.allocate(inputBlock); //여기 할당해줄때도 문제네 -> 이거 수정해야됨: Hint 32인데 36까지 입력됨 44입력 후 쪼개어져
		for(int i=0; i<list.size(); i++) { //putInt할 때 다음 위치로 알아서 넘어가는지 확인해볼것
			buf.putInt(list.get(i));
		}
		for(int j=list.size(); j<inputBlock/4; j++) { //근데 이렇게 하면 다시 쓸 때 괜찮나? 고려해보기
			buf.putInt(-1);
		}
		
		return buf.array();
	}

	@Override
	public void open(String metapath, String savepath, int blocksize, int nblocks) throws IOException {
		File treefile = new File(savepath);
		this.metapath = metapath;
		this.savepath = savepath;
		
		if(blocksize%8==0) {
			this.blocksize = blocksize;
			inputBlock = this.blocksize + 12; 
		}else {
			this.blocksize = (blocksize/8)*8;
			inputBlock = this.blocksize + 12; 
		}
		
		if(!treefile.exists()) { //Insert 모드
			treefile.createNewFile();
			tree = new RandomAccessFile(savepath,"rw");
		}else { //Search 모드
			tree = new RandomAccessFile(savepath, "rw"); //캐시의 역할 때문에 쓰는 것도 필요하다
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
		int nodeAddress = searchAtRoot(key); //여기서 num이 1이됨
		
		if(height==0) { //트리에 Root밖에 없는 경우 =>임시 Test만 해당되는데 지워버릴까?
			return nodeAddress;
		}
		while(num<height) { //LeafNode의 바로 윗부분 까지만 가도록 하기
			nodeAddress = findAtParent(key, nodeAddress);
		}
		
		nodeAddress = searchAtLeaf(key,nodeAddress);
		
		return nodeAddress; //찾지 못하면 -1 호출
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

