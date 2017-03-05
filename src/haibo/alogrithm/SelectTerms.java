package haibo.alogrithm;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Array;
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

	private HashMap<String, Integer> termMap;	//保存特征和包含其的实例数量
	private HashMap<Integer, Integer> labelMap;	//每个标签所标记的实例数量(标签编号：标记实例数)
	
	//算法需要的数据结构
	private ArrayList<Term> IGS;
	private ArrayList<Double> S;
	private ArrayList<Term> IGZ; //存储最终选择的特征向量
	
	//将训练集直接存入内存，便于提升读写效率
	private ArrayList<HashSet<Integer>> annotationFile;
	private ArrayList<HashMap<String,Integer>> segmentFile;	
	
	public SelectTerms(HashMap<String, Integer> termMap, HashMap<Integer, Integer> labelMap,
			ArrayList<HashSet<Integer>> annotationFile,ArrayList<HashMap<String,Integer>> segmentFile) {
		
		this.termMap = termMap;
		this.labelMap = labelMap;
		
		this.annotationFile = annotationFile;
		this.segmentFile	= segmentFile;
		
	}

	//特征选择算法
	public void MLFSIE(){
		IGS = new ArrayList<>();
		
		for(String x : termMap.keySet()){
			S = new ArrayList<>();
			for(int l : labelMap.keySet()){
				//计算IG(li|xi) 公式：IG (l|x) = H(l) + H(x) - H(xl)
				double IG_lx = H(x) + H(l) - H(x,l);
				//进行归一化
				double SU_lx = (IG_lx/(H(l)+H(x)))*2;
				if(SU_lx != 0) S.add(SU_lx);
			}
			
			//计算IGSi
			double IGSi = 0;
			for(double ele : S)
				IGSi += ele;
			IGS.add(new Term(x,IGSi));
		}
		
		double u = average(IGS);	//平均值
		double c = getStarC(IGS,u);//标准差
		
		for(Term ele : IGS){
			ele.num = (ele.num-u)/c;
		}
		IGZ = new ArrayList<>();
		
		//计算阈值
		double doorValue = caculate(IGS);
		
		//筛选特征
		try{
			File saveTermsSelect = new File(Util.EXESELETERMS);
			if(!saveTermsSelect.exists()) saveTermsSelect.createNewFile();
			PrintWriter out = new PrintWriter(saveTermsSelect);
			
			for(Term ele : IGS)
				if(Math.abs(ele.num)>= doorValue){
					IGZ.add(ele);
					out.println(ele.name+"   "+ele.num);
					//System.out.println(ele.name);
				}
			
			out.close();
			Util.TERMSELECTEDNUM = IGZ.size();
			
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//计算阈值
	private double caculate(ArrayList<Term> set) {
		double value = 0.0;
		for(Term ele : set)
			value += Math.abs(ele.num);
		
		return value/set.size();
	}

	private double getStarC(ArrayList<Term> set,double aver) {
		double result = 0.0;
		for(Term ele : set)
			result += (ele.num-aver)*(ele.num-aver);
		
		result = Math.sqrt(result/set.size());
		
		return result;
	}

	private double average(ArrayList<Term> set) {
		double result = 0.0;
		for(Term ele : set)
			result += ele.num;
		
		return result/set.size();
	}

	//计算熵值
	public double H(String x){
		double K = termMap.get(x).intValue();
		double M = Util.INSANENUM;
		
		double rs = -1* K/M * (Math.log(K/M)/Math.log(2.0)) - ((M-K)/M * (Math.log((M-K)/M)/Math.log(2.0)));
		return rs;
	}
	public double H(int l){
		double K = labelMap.get(l).intValue();
		double M = Util.INSANENUM;
		
		double rs = -1* K/M * (Math.log(K/M)/Math.log(2.0)) - ((M-K)/M * (Math.log((M-K)/M)/Math.log(2.0)));
		return rs;
	}
	//计算联合熵 H(xl) = H(x)+H(l | x);
	public double H(String x,int l){
		int M = Util.INSANENUM;
		double bi_x = 0,bi_nox=0,bi_xl=0,bi_xnol=0,bi_noxl=0,bi_noxnol=0;
		
		for(int i=0;i<Util.INSANENUM;i++){
			if(segmentFile.get(i).containsKey(x)){
				bi_x++;
				if(annotationFile.get(i).contains(l)) bi_xl++;
				else bi_xnol++;
				
			}else{
				bi_nox++;
				if(annotationFile.get(i).contains(l)) bi_noxl++;
				else bi_noxnol++;
			}
		}
		
		double lx=0,lnox=0,nolx=0,nolnox=0;
		//计算条件熵
		if(bi_xl > 0) lx =  (bi_xl/bi_x)*(Math.log(bi_xl/bi_x)/Math.log(2.0));
		if(bi_xnol > 0) nolx = (bi_xnol/bi_x)*(Math.log(bi_xnol/bi_x)/Math.log(2.0));
		
		if(bi_noxl > 0) lnox = (bi_noxl/bi_nox)*(Math.log(bi_noxl/bi_nox)/Math.log(2.0));
		if(bi_noxnol > 0) nolnox = (bi_noxnol/bi_nox)*(Math.log(bi_noxnol/bi_nox)/Math.log(2.0));
		
		double rs = -1.0 * (bi_x/M)*(lx+nolx) + -1.0 * (bi_nox/M)*(lnox+nolnox);
		
		return rs;
	}
	
	public ArrayList<Term> getIGZ(){
		return IGZ;
	}
}
