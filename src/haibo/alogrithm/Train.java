package haibo.alogrithm;

import java.io.File;

/**
 * 
 * @author haiboself
 * 训练分类器
 */
public class Train {
	private File content;		//训练集内容
	private File annotation;	//内容对应标注
	private File labels;		//标签集
	
	public Train(String content,String annotation,String labels){
		this.content 	= new File(content);
		this.annotation = new File(annotation);
		this.labels	    = new File(labels);
	}
}
