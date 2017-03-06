package haibo.alogrithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import weka.classifiers.trees.J48;

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
	private File model;			//存储训练的模型

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

	public Train(String content,String annotation,String labels,String modelName){
		try {
			this.content 	= new File(content);
			this.annotation = new File(annotation);
			this.labels	    = new File(labels);
			
			String path = Util.MODEL+modelName+".model";
			this.model 		= new File(path);		//存储数据模型
			if(this.model.exists()) this.model.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		File xmlFile = new File(Util.XML_FILE);

		//创建xml文件
		try {
			if(!xmlFile.exists())
				xmlFile.createNewFile();
			PrintWriter out = new PrintWriter(xmlFile);

			out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			out.write("<labels xmlns=\"http://mulan.sourceforge.net/labels\">\n");
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
		Util.LoadFileInMemory(annotation, segments, annotationFile, segmentFile);//将所需文件读入内存
		selectTerm();
		//第四步。根据锁选择的特征向量将文件内容抽象为向量表示。
		transferTextToVector();
		//释放一些占用的空间
		clearMemory();
		//第五步生成arff文件
		try {
			arffFile = Util.ArffFile(Util.EXEARFF_FILE,Util.EXETRANSFER,IGZ.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		Transfer transfer = new Transfer(termMap, annotationFile, segmentFile, IGZ,Util.EXETRANSFER);
		transfer.transfer(Util.INSANENUM);
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
			HashMap<Integer,String> mapLabel = new HashMap<>();	//存储标签标号和名称
			Scanner inla = new Scanner(labels);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(Util.LABELSTA));
			
			int labelIndex = 1;
			while(inla.hasNextLine()){
				mapLabel.put(labelIndex++,inla.nextLine().trim());
			}
			inla.close();
			
			out.writeObject(mapLabel);
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

	//训练
	private void train() {

		MultiLabelInstances dataset = null;
		try {
			dataset = new MultiLabelInstances(Util.EXEARFF_FILE,Util.XML_FILE);
			RAkEL learner1 = new RAkEL(new LabelPowerset(new J48()));
			//MLkNN learner2 = new MLkNN();

			RAkEL model = new RAkEL(new LabelPowerset(new J48()));
			model.build(dataset);
			
			//存储训练的模型
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.model));
			out.writeObject(model);
			out.close();	
		} catch (InvalidDataFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
