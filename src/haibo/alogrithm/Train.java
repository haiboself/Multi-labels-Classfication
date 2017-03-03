package haibo.alogrithm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import javax.swing.text.StyleContext.SmallAttributeSet;

import org.omg.CORBA.DataOutputStream;

/**
 * 
 * @author haiboself
 * 训练分类器
 * 采用mulan
 */
public class Train {
	private File content;		//训练集内容
	private File annotation;	//内容对应标注
	private File labels;		//标签集
	private File segments;		//分词结果

	//mulan的输入
	private File xmlFile;
	private File arffFile;
	
	private ArrayList<Term> IGZ; //存储最终选择的特征向量
	
	//每个标签所标记的实例数量(标签编号：标记实例数)
	private HashMap<Integer,Integer> labelMap;
	//每个特征和含有其的实例数目
	private HashMap<String,Integer> termMap;
	
	//将训练集直接存入内存，便于提升读写效率
	private ArrayList<HashSet<Integer>> annotationFile;
	private ArrayList<HashMap<String,Integer>> segmentFile;	

	public Train(String content,String annotation,String labels){
		this.content 	= new File(content);
		this.annotation = new File(annotation);
		this.labels	    = new File(labels);
		
		annotationFile = new ArrayList<HashSet<Integer>>();
		segmentFile	   = new ArrayList<HashMap<String,Integer>>();
		
		//从训练集文件生成arff文件作为mulan的输入
		createArffFile();
		//从labels文件生成xml文件作为mulan的输入
		createXmlFile();
		//训练
		train();
	}

	//生成存储标签信息的xml文件
	private void createXmlFile(){
		File xmlFile = new File("./data/exercise.xml");

		//创建xml文件
		try {
			if(!xmlFile.exists())
				xmlFile.createNewFile();
			PrintWriter out = new PrintWriter(xmlFile);

			out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			out.write("<labels xmlns=\"haiboself\">\n");
			for(int i=1;i<=Util.LABELSNUM;i++)
				out.write("<label name=\"label"+i+"\"></label>\n");
			out.write("</labels>\n");
			
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}

	//生成存储数据集信息的arff文件
	private void createArffFile() {
		//第一步进行分词
		termMap = cutSentence();
		//第二步统计labels和训练集标注结果的相关信息
		collectLabelInfo();
		//第三步特征选择
		loadFileInMemory();//将所需文件读入内存
		selectTerm();
		//第四步。根据锁选择的特征向量将文件内容抽象为向量表示。
		transferTextToVector();
		//释放一些占用的空间
		clearMemory();
		//第五步生成arff文件
		try {
			arffFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void arffFile() throws IOException {
		arffFile = new File("./data/exercise.arff");
		if(!arffFile.exists()) arffFile.createNewFile();
		PrintWriter out = new PrintWriter(arffFile);
		
		Scanner in = new Scanner(new File("./data/transferResult.txt"));
		
		out.println("@relation MultiLabelExample");
		for(int i=1;i<=IGZ.size();i++)
			out.println("@attribute feature"+i+" numeric");
		for(int i=1;i<=Util.LABELSNUM;i++)
			out.println("@attribute label"+i+" {0, 1}");
		
		out.println(" @data");
		while(in.hasNextLine()){
			out.println(in.nextLine());
		}
		
		in.close();
		out.close();
	}

	//释放一些占用的空间
	private void clearMemory() {
		labelMap = null;
		termMap  = null;
		annotationFile = null;
		segmentFile = null;
	}

	//根据锁选择的特征向量将文件内容抽象为向量表示。
	private void transferTextToVector() {
		Transfer transfer = new Transfer(termMap, annotationFile, segmentFile, IGZ);
		transfer.transfer();
	}

	//特征选择
	private void selectTerm() {
		SelectTerms selectTerms = new SelectTerms(termMap,labelMap,annotationFile,segmentFile);
		selectTerms.MLFSIE();
		IGZ = selectTerms.getIGZ();
	}

	//统计labels和训练集标注结果的相关信息
	private void collectLabelInfo() {
		labelMap = new HashMap<>();
		
		try {
			Scanner in = new Scanner(annotation);
			while(in.hasNextInt()){
				
				int labelId = in.nextInt();
				if(labelId != 0){
					if(labelMap.containsKey(labelId))
						labelMap.put(labelId,labelMap.get(labelId).intValue()+1);
					else labelMap.put(labelId, 1);
				}
			}
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try{
			File labelMapFile = new File("./data/labelStatistic.txt");
			if(!labelMapFile.exists()) labelMapFile.createNewFile();
			PrintWriter out = new PrintWriter(labelMapFile);
			
			for(int key : labelMap.keySet())
				out.println(key+"    "+labelMap.get(key).intValue());
			out.close();
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	private HashMap<String, Integer> cutSentence() {
		WordSegmentation wordSeg = new WordSegmentation(content); 
		try {
			wordSeg.execute();
			segments = wordSeg.getTargetFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return wordSeg.getTermMap();
	}

	private void train() {
		// TODO Auto-generated method stub

	}
	
	private void loadFileInMemory() {
		try{
			Scanner inAnno = new Scanner(annotation);
			Scanner inCont = new Scanner(segments);
			
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

}
