package haibo.alogrithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * 工具类，存储常量，全局变量和一些静态方法
 * @author haiboself
 *
 */

public class Util {
	public static int LABELSNUM = 237;	//标签数量 
	public static int TERMSELECTEDNUM; //最终选择的特征向量数量
	
	public static int INSANENUM;	//训练集实例数量
	public static int TERMSNUM;		//特征向量数量

	public static int RAWINSANESUM = 0;	//标注数据实例数量
	
	public static final String XML_FILE 	  = "./data/exercise/exercise.xml";	//保存存储label信息的xml文件
	public static final String LABELSTA 	  = "./data/exercise/labelStatistic.txt";	//存储标签数量
	public static final String MODEL 		  = "./data/model/";	//存储训练的数据模型

	public static final String RAWDATA_SEGRES = "./data/rawData/rawSegREs.txt";	//保存未标注文件的分词结果
	public static final String RAWTERMS_FILE  = "./data/rawData/rawTerms.txt";	//保存未标注文件中被选中特征和其文档频率
	public static final String RAWANNOTATION  = "./data/rawData/rawAnnotation.txt";	//保存未标注文件的标注结果
	public static final String RAWARFF_FIlE   = "./data/rawData/raw.arff";
	public static final String RAWTRANSFER    = "./data/rawData/rawTransferResult.txt";

	public static final String EXEDATA_SEGRES = "./data/exercise/exeWordSegRes.txt";//保存训练数据的分词结果
	public static final String EXETERMS_FILE  = "./data/exercise/exeOriginTerms.txt";	//保存训练护具所有特征和其文档频率
	public static final String EXESELETERMS   = "./data/exercise/exeTermsSelected.txt";	//保存最终选择的特征
	public static final String EXEARFF_FILE   = "./data/exercise/exercise.arff";
	public static final String EXETRANSFER	  = "./data/exercise/exeTransferResult.txt";

	//读入训练集到内存
	public static void LoadFileInMemory(File annotataion,File segmetns,ArrayList<HashSet<Integer>> annotationFile,
			ArrayList<HashMap<String,Integer>> segmentFile) {
		try{
			Scanner inAnno = new Scanner(annotataion);
			Scanner inCont = new Scanner(segmetns);

			while(inAnno.hasNextLine()){
				HashSet<Integer> annoSet = new HashSet<>();
				String[] tags = inAnno.nextLine().split(" |\t|\n|\r");
				for(String tag : tags)
					annoSet.add(Integer.parseInt(tag.trim()));
				annotationFile.add(annoSet);

				HashMap<String,Integer> segMap = new HashMap<>();
				String[] words = inCont.nextLine().split(" |\t|\n|\r");
				for(String word : words){
					if(segMap.containsKey(word))
						segMap.put(word,segMap.get(word).intValue()+1);
					else segMap.put(word, 1);
				}
				segmentFile.add(segMap);
			}

			inAnno.close();
			inCont.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	
	//创建arff格式文件
	public static File ArffFile(String arff,String traRes,int size) throws IOException {
		File arffFile = new File(arff);
		if(!arffFile.exists()) arffFile.createNewFile();
		PrintWriter out = new PrintWriter(arffFile);
		
		Scanner in = new Scanner(new File(traRes));
		
		out.println("@relation MultiLabelExample");
		for(int i=1;i<= size;i++)
			out.println("@attribute feature"+i+" numeric");
		for(int i=1;i<=Util.LABELSNUM;i++)
			out.println("@attribute label"+i+" {0,1}");
		
		out.println("@data");
		while(in.hasNextLine()){
			out.println(in.nextLine().trim());
		}
		
		in.close();
		out.close();
		
		return arffFile;
	}

	//获取标签信息
	@SuppressWarnings("unchecked")
	public static HashMap<Integer, String> Init() {
		try{
			ObjectInputStream inLa = new ObjectInputStream(new FileInputStream(Util.LABELSTA));
			HashMap<Integer,String> mapla = (HashMap<Integer, String>) inLa.readObject();
			Util.LABELSNUM = mapla.size();
			inLa.close();
			
			return mapla;
		}catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

}
