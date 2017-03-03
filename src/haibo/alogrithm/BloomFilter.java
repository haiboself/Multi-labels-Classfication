package haibo.alogrithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Scanner;

//布隆过滤器
public class BloomFilter {
	private static final int DEFAULT_SIZE = 2 << 24;//布隆过滤器的比特长度
	private static final int[] seeds = {3,5,7,11,13,31,37,38};
	private BitSet bits = new BitSet(DEFAULT_SIZE);
	private SimpleHash[] func = new SimpleHash[seeds.length];
	
	public BloomFilter(){
		for(int i=0; i< seeds.length; i++){
			func[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);
		}
	}
	
	public void addValue(String value){
		for(SimpleHash f : func){////将字符串value哈希为8个或多个整数，然后在这些整数的bit上变为1  
			bits.set(f.hash(value),true);
		}
	}
	
	public void add(String value){
		if(value != null) addValue(value);
	}
	
	public boolean contains(String value){
		if(value == null) return false;
		boolean ret = true;
		for(SimpleHash f : func)
			ret = ret && bits.get(f.hash(value));
		return ret;
	}
	
	public static void main(String[] args){
		String value = "设 一 整型 变量 占用 2个 字节 则 下述 共同体 变量 所 占用 内存 字节数 为 14个 7个 8个 随机 而定";
		BloomFilter bloomFilter = new BloomFilter();
		
		String strs[] = value.split(" |\t|\r");
		for(String s : strs)
			bloomFilter.add(s);
		
		if(bloomFilter.contains("号"))
			System.out.println("dsfasfasdf");
	}
}

class SimpleHash{
	
	private int cap;
	private int seed;
	
	public SimpleHash(int cap,int seed){
		this.cap = cap;
		this.seed = seed;
	}
	
	public int hash(String value){//字符串哈希函数
		int result = 0;
		int len = value.length();
		
		for(int i=0; i<len; i++){
			result = seed * result + value.charAt(i);
		}
		return (cap-1) & result;
		
	}
}