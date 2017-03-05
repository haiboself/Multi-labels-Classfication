package haibo.alogrithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;


/**
 * 将文本文件抽象为向量表示
 * @author haiboself
 *
 */
public class Transfer {
	private HashMap<String, Integer> termMap;	//保存特征和包含其的实例数量
	
	private ArrayList<Term> IGZ; //存储最终选择的特征向量
	
	//将训练集直接存入内存，便于提升读写效率
	private ArrayList<HashSet<Integer>> annotationFile;
	private ArrayList<HashMap<String,Integer>> segmentFile;
	
	private File transferResult;	//保存转换的结果
	private PrintWriter out;
	
	public Transfer(HashMap<String, Integer> termMap,ArrayList<HashSet<Integer>> annotationFile,
			ArrayList<HashMap<String,Integer>> segmentFile,ArrayList<Term> IGZ,String saveTransfer){
		
		this.termMap = termMap;
		this.annotationFile = annotationFile;
		this.segmentFile = segmentFile;
		this.IGZ = IGZ;
		
		transferResult = new File(saveTransfer);
		if(!transferResult.exists())
			try {
				transferResult.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void transfer(int insaneNum){
		double TF=0,	//词条频率
				DF=0,	//文档频率
				IDF=0;	//逆文档频率
		
		BloomFilter bloomFilter;
		
		try {
			out = new PrintWriter(transferResult);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//遍历文档
		for(int i=0;i<segmentFile.size();i++){
			HashMap<String,Integer> insane = segmentFile.get(i);
			HashSet<Integer> tagset = annotationFile.get(i);
			
//			//对实例建立布隆过滤器
//			bloomFilter = new BloomFilter();
//			
//			for(String ele : insane.keySet())
//				bloomFilter.add(ele);
			
			//遍历所有选取特征
			double val; StringBuffer rst = new StringBuffer();
			for(int j=0;j<IGZ.size();j++){
				if(insane.containsKey(IGZ.get(j).name)){
					
					//System.out.println(IGZ.get(j).name + i +" "+j);
					TF = insane.get(IGZ.get(j).name).intValue();
					
					IDF = Math.log(insaneNum/termMap.get(IGZ.get(j).name).intValue());
					if(IDF == 0) IDF = 1;
					
					val =  TF * IDF; //特征向量的值
					
					rst.append(val+",");
				}else rst.append(0+",");
			}
			
			//标注信息
			for(int x=1;x<Util.LABELSNUM;x++){
				if(tagset.contains(x))
					rst.append(1+",");
				else rst.append(0+",");
			}
			
			if(tagset.contains(Util.LABELSNUM))
				rst.append(1+"");
			else rst.append(0+"");
			
			out.println(rst);
		}
		
		out.close();
	}
}
