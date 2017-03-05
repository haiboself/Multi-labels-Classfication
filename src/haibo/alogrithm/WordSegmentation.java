package haibo.alogrithm;

import java.awt.datatransfer.StringSelection;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * 分词模块，采用IKAnalyzer进行分词
 * 并统计相关信息
 * @author haiboself
 *
 */
public class WordSegmentation {
	private File sourceFile;	//需要进行分词的txt文件
	private File targetFile;	//分词结果
	private File termsFile;		//保存terms
	
	private Scanner in;			//需要进行分词的数据
	private HashMap<String,Integer> termMap;	//保存特征和包含其的实例数量
	
	private int insaneNum = 0;
	private boolean isExercise;	//训练数据or未标注数据
	
	private HashSet<String> featureSet;	//存储最终选择的特征向量
	
	//进行标注
	public WordSegmentation(Scanner in){
		this.in	    = in;
		targetFile 	= new File(Util.RAWDATA_SEGRES);
		termsFile 		= new File(Util.RAWTERMS_FILE);
		isExercise = false;
		
		Scanner featureIn;
		try {
			featureSet = new HashSet<>();
			featureIn = new Scanner(new File(Util.EXESELETERMS));
			
			while(featureIn.hasNextLine()){
				featureSet.add(featureIn.nextLine().split(" |\t|\r")[0]);
			}
			featureIn.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//进行训练
	public WordSegmentation(File sourceFile){
		this.sourceFile = sourceFile;
		targetFile 		= new File(Util.EXEDATA_SEGRES);
		termsFile 		= new File(Util.EXETERMS_FILE);
		isExercise = true;

		try{
			in = new Scanner(this.sourceFile);
			
			if(!targetFile.exists())
				targetFile.createNewFile();
			if(!targetFile.exists())
				targetFile.createNewFile();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//进行分词;保存分词结果.
	//统计所有特征并保存;
	public void execute() throws FileNotFoundException {
		PrintWriter out = new PrintWriter(targetFile);
		termMap = new HashMap<>();

		while(in.hasNextLine()){
			String text = in.nextLine(); 	//读取一条实例的内容
			insaneNum++;
			
			//独立Lucene实现
			StringBuffer strbuf = new StringBuffer();
			StringReader re = new StringReader(text);
			IKSegmenter ik = new IKSegmenter(re,true);
			Lexeme lex = null;
			
			try {
				while((lex=ik.next())!=null){
					//得到一个切割的词
					String word = lex.getLexemeText();
					//对这个词进行处理，例如对于选择题，过滤掉选项标示如A,B,C,D的信息。
					word = filterWord(word);
					if(word == null) continue;
					
					//更新特征和包含其的实例数量
					if(!termMap.containsKey(word)){
						if(isExercise) termMap.put(word, 1);
						else if(!isExercise && featureSet.contains(word))	termMap.put(word, 1);
						
					}
					else{
						if(strbuf.indexOf(word)==-1){//确保不是同一个实例中的相同单词
							if(isExercise) termMap.put(word,termMap.get(word).intValue()+1);
							else if(!isExercise && featureSet.contains(word))
								termMap.put(word,termMap.get(word).intValue()+1);
						}
					}
					
					strbuf.append(word+" ");//分词结果
				}
				
				out.println(strbuf);
				//System.out.println(strbuf);
			}catch (Exception e) {
			}
		}	
		
		if(isExercise){
			Util.INSANENUM = insaneNum-1;
			Util.TERMSNUM = termMap.size();
		}else Util.RAWINSANESUM = insaneNum;
		
		saveMap();
		in.close();
		out.close();	
		
	}

	//将map中的信息存入文件
	private void saveMap() {
		try {
			PrintWriter out = new PrintWriter(termsFile);
			for(String s : termMap.keySet())
				out.printf("%s %12d\n",s,termMap.get(s).intValue());
			
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//对这个词进行处理，例如对于选择题，过滤掉选项标示如A,B,C,D的信息。
	private String filterWord(String str) {
		Pattern pattern1 = Pattern.compile("[a-z | A-Z]\\..*");
		Pattern pattern2 = Pattern.compile("[a-z | A-Z]");
		
		Matcher matcher1 = pattern1.matcher(str);
		Matcher matcher2 = pattern2.matcher(str);
		
		if(matcher1.find())
			return str.substring(2);
		if(matcher2.find()) return null;
		
		return str;
	}
	
	public HashMap<String,Integer> getTermMap(){
		//if(termMap == null) throw new NullPointerException();
		return termMap; 
	}
	public File getTargetFile(){
		//if(termMap == null) throw new NullPointerException();
		return targetFile;
	}

	public ArrayList<Term> getFeatureSet() {
		ArrayList<Term> list = new ArrayList<>();
		for(String s : termMap.keySet())
			list.add(new Term(s,0));
		return list;
	}
}
