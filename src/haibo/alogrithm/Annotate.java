package haibo.alogrithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * 对未标注文件进行标注
 * @author haiboself
 *
 */
public class Annotate {
	
	private Scanner in;	//未标注内容
	private Scanner out; //标注结果
	private File inAnno;
	
	//将训练集直接存入内存，便于提升读写效率
	private ArrayList<HashSet<Integer>> annotationFile;
	private ArrayList<HashMap<String,Integer>> segmentFile;	
	
	public Annotate(Scanner in){
		this.in = in;
	}
	
	public String annotate() throws IOException{
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
		Util.ArffFile(Util.RAWARFF_FIlE,Util.RAWTRANSFER,Util.LABELSNUM);
		//第四步，进行标注
		try {
			mark();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Util.RAWANNOTATION;
	}
	
	private void mark() throws Exception {
		    String arffFilename = Util.EXEARFF_FILE;
	        String xmlFilename = Util.XML_FILE;
	        MultiLabelInstances dataset = new MultiLabelInstances(arffFilename, xmlFilename);

	        RAkEL model = new RAkEL(new LabelPowerset(new J48()));

	        model.build(dataset);


	        String unlabeledFilename = Util.RAWARFF_FIlE;
	        FileReader reader = new FileReader(unlabeledFilename);
	        Instances unlabeledData = new Instances(reader);

	        int numInstances = unlabeledData.numInstances();

	        for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
	            Instance instance = unlabeledData.instance(instanceIndex);
	            MultiLabelOutput output = model.makePrediction(instance);
	            // do necessary operations with provided prediction output, here just print it out
	            System.out.println(output);
	        }
	}
}
