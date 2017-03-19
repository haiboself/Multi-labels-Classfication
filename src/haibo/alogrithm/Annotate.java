package haibo.alogrithm;

import java.awt.Label;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.RAkEL;
import weka.core.Instance;
import weka.core.Instances;

/**
 * 对未标注文件进行标注
 * @author haiboself
 *
 */
public class Annotate {
	
	private Scanner in;	//未标注内容
	private File inAnno;
	
	private HashMap<Integer,String> labelMap;	//储存标签的编号和名称
	private StringBuffer rsbuffer; //标注结果
	private String modelName;	//使用的数据模型
	
	//将训练集直接存入内存，便于提升读写效率
	private ArrayList<HashSet<Integer>> annotationFile;
	private ArrayList<HashMap<String,Integer>> segmentFile;	
	
	public Annotate(Scanner in,String modleName){
		this.in = in;
		this.modelName = modleName;
		labelMap = new HashMap<>();
		rsbuffer = new StringBuffer();
	}
	
	public String annotate() throws IOException{
		
		//初始化，读入需要的数据
		labelMap = Util.Init();
		//第一步，分词
		WordSegmentation wordSeg = new WordSegmentation(in);
		try {
			wordSeg.execute();
			
			//初始化标记信息为0
			inAnno = new File(Util.RAWANNOTATION);
			if(!inAnno.exists()) inAnno.createNewFile();
			PrintWriter outAnno = new PrintWriter(inAnno);
			
			for(int i=0;i<Util.RAWINSANESUM;i++)
				outAnno.println("0");
			outAnno.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//读入所需数据
		annotationFile = new ArrayList<HashSet<Integer>>();
		segmentFile	   = new ArrayList<HashMap<String,Integer>>();
		Util.LoadFileInMemory(inAnno,new File(Util.RAWDATA_SEGRES),annotationFile, segmentFile);
		//第二步，抽象化数据
		Transfer transfer = new Transfer(wordSeg.getTermMap(),annotationFile,segmentFile,wordSeg.getFeatureSet()
				,Util.RAWTRANSFER);
		transfer.transfer(Util.RAWINSANESUM);
		//第三步，生称arff文件
		Util.ArffFile(Util.RAWARFF_FIlE,Util.RAWTRANSFER,Util.TERMSELECTEDNUM);
		//第四步，进行标注
		try {
			mark();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rsbuffer.toString();	//返回标注结果
	}
	
	private void mark() throws Exception {
	        //读取数据模型
	        ObjectInputStream inModel = new ObjectInputStream(new FileInputStream(Util.MODEL+modelName));
	        RandomKLabelSets model = (RandomKLabelSets) inModel.readObject();
	        inModel.close();

	        String unlabeledFilename = "./data/test/birds-test.arff";
	        FileReader reader = new FileReader(unlabeledFilename);
	        Instances unlabeledData = new Instances(reader);

	        int numInstances = unlabeledData.numInstances();

	        //进行标注	
	        for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
	            Instance instance = unlabeledData.instance(instanceIndex);
	            MultiLabelOutput output = model.makePrediction(instance);
	            
	            boolean[] rs = output.getBipartition();
	            
	            int num = 0;
	            for(int i=1;i<=rs.length;i++){
	            	if(rs[i-1]){
	            		rsbuffer.append(labelMap.get(i)+",");
	            		num++;
	            	}
	            }
	            
	            if(num == 0) rsbuffer.append("None");
	            rsbuffer.append("\n");
	        }
	}
}
