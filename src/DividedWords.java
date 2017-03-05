import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class DividedWords {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner in = new Scanner("dfghkjnkml,;.szxdcfgvhbjnmk,");
		
		String text = in.nextLine(); 
		//独立Lucene实现
		StringReader re = new StringReader(text);
		IKSegmenter ik = new IKSegmenter(re,true);
		Lexeme lex = null;
		try {
			while((lex=ik.next())!=null){
				//得到一个切割的词
				String word = lex.getLexemeText();
				//对这个词进行处理，例如对于选择题，过滤掉选项标示如A,B,C,D的信息。
				word = filterWord(word);
				
				System.out.print(word+"|");
			}
		    
		}catch (Exception e) {
		}
	}
	
	private static String filterWord(String str) {
		Pattern pattern1 = Pattern.compile("[a-z | A-Z]\\..*");
		Pattern pattern2 = Pattern.compile("[a-z | A-Z]");
		
		Matcher matcher1 = pattern1.matcher(str);
		Matcher matcher2 = pattern2.matcher(str);
		
		if(matcher1.find())
			return str.substring(2);
		return str;
	}
}
