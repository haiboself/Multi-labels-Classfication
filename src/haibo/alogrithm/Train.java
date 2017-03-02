package haibo.alogrithm;

import java.io.File;
import java.io.FileNotFoundException;

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
	
	//mulan的输入
	private File xmlFile;
	private File arffFile;
	
	public Train(String content,String annotation,String labels){
		this.content 	= new File(content);
		this.annotation = new File(annotation);
		this.labels	    = new File(labels);
		
		//从labels文件生成xml文件作为mulan的输入
		createXmlFile();
		//从训练集文件生成arff文件作为mulan的输入
		createArffFile();
		//训练
		train();
	}

	//生成存储标签信息的xml文件
	private void createXmlFile(){}
	
	//生成存储数据集信息的arff文件
	private void createArffFile() {
		//第一步进行分词
		cutSentence();
	}
	
	private void cutSentence() {
		WordSegmentation wordSeg = new WordSegmentation(content); 
		try {
			wordSeg.execute();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void train() {
		// TODO Auto-generated method stub
		
	}

}
