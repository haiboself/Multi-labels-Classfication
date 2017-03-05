package haibo.alogrithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.Scanner;

import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import weka.classifiers.trees.J48;

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
		
		 MultiLabelInstances dataset = null;
			try {
				dataset = new MultiLabelInstances("./data/exercise.arff", "./data/exercise.xml");
			} catch (InvalidDataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        RAkEL learner1 = new RAkEL(new LabelPowerset(new J48()));
	        //MLkNN learner2 = new MLkNN();

	        Evaluator eval = new Evaluator();
	        MultipleEvaluation results;

	        int numFolds = 2;
	        results = eval.crossValidate(learner1, dataset, numFolds);
	        System.out.println(results);
	        // results = eval.crossValidate(learner2, dataset, numFolds);
	        //System.out.println(results);
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