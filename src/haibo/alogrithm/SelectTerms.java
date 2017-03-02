package haibo.alogrithm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * 采用MLFSIE 算法（一种基于信息熵的算法）进行特征的选择
 * @author haiboself
 *
 */
public class SelectTerms {
	
	private File segMents;		//分词结果
	private File annotation;	//内容对应标注
	private File labels;		//标签集

	private HashMap<String, Integer> termMap;	//保存特征和包含其的实例数量
	private HashMap<Integer, Integer> labelMap;	//每个标签所标记的实例数量(标签编号：标记实例数)
	
	//算法需要的数据结构
	private HashSet<Term> IGS;
	private HashSet<Term> S;
	private HashSet<Term> IGZ;
	
	//将训练集直接存入内存，便于提升读写效率
	private ArrayList<HashSet<Integer>> annotationFile;
	private ArrayList<HashSet<String>> segmentFIle;	
	
	public SelectTerms(HashMap<String, Integer> termMap, HashMap<Integer, Integer> labelMap,
			File segments,File annotation,File labels) {
		
		this.termMap = termMap;
		this.labelMap = labelMap;
		this.segMents = segments;
		this.annotation = annotation;
		this.labels = labels;
		
		annotationFile = new ArrayList<HashSet<Integer>>();
		segmentFIle	   = new ArrayList<HashSet<String>>();
		
		loadFileInMemory();
	}

	private void loadFileInMemory() {
		try{
			Scanner inAnno = new Scanner(annotation);
			Scanner inCont = new Scanner(segMents);
			
			while(inAnno.hasNextLine()){
				HashSet<Integer> annoSet = new HashSet<>();
				String[] tags = inAnno.nextLine().split(" | *");
				for(String tag : tags)
					annoSet.add(Integer.parseInt(tag));
				annotationFile.add(annoSet);
			}
			inAnno.close();
			
			while(inCont.hasNextLine()){
				HashSet<String> segSet = new HashSet<>();
				String[] words = inCont.nextLine().split(" | *");
				for(String word : words)
					segSet.add(word);
				segmentFIle.add(segSet);
			}
			
			inCont.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void MLFSIE(){
		IGS = new HashSet<>();
		
		for(String x : termMap.keySet()){
			S = new HashSet<>();
			for(int l : labelMap.keySet()){
				//计算IG(li|xi) 公式：IG (B|A) = H(A) + H(B) - H(AB)
				double IG_lx = H(x) + H(l) - H(x,l);
				//进行归一化
				double SU_lx = (IG_lx/(H(l)+H(x)))*2;
				S.add(new Term(x,SU_lx));
			}
			
			//计算IGSi
			double IGSi = 0;
			for(Term ele : S)
				IGSi += ele.num;
			IGS.add(new Term(x,IGSi));
		}
		
		IGZ = new HashSet<>();
		for(Term ele : IGS){
			ele.num = (ele.num-u)/c;
		}
		
		//计算阈值
		int doorValue = caculate();
		
		//筛选特征
	}
	
	//计算熵值
	public double H(String x){
		double K = termMap.get(x).intValue();
		double M = Util.INSANENUM;
		
		return -1* K/M * (Math.log(K/M)/Math.log(2.0)) - ((M-K)/M * (Math.log((M-K)/M)/Math.log(2.0)));
	}
	public double H(int l){
		double K = labelMap.get(l).intValue();
		double M = Util.INSANENUM;
		
		return -1* K/M * (Math.log(K/M)/Math.log(2.0)) - ((M-K)/M * (Math.log((M-K)/M)/Math.log(2.0)));
	}
	//计算联合熵
	public double H(String x,int l){
		double ll=0,oo=0,lo=0,ol=0;
		int M = Util.INSANENUM;
		
		for(int j=0;j<Util.INSANENUM;j++){
			if(annotationFile.get(j).contains(l) && segmentFIle.get(j).contains(x))
				ll++;
			else if(annotationFile.get(j).contains(l) && !segmentFIle.get(j).contains(x))
				lo++;
			else if(!annotationFile.get(j).contains(l) && segmentFIle.get(j).contains(x))
				ol++;
			else oo++;
		}
		
		return  -1*ll/M * (Math.log(ll/M)/Math.log(2.0)) - lo/M * (Math.log(lo/M)/Math.log(2.0)) -
				 ol/M * (Math.log(ol/M)/Math.log(2.0)) -  oo/M * (Math.log(oo/M)/Math.log(2.0));
	}
	
	class Term{
		String name;
		double num;
		
		public Term(String s,double n){
			name = s;
			num = n;
		}
	}
}
