import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class DividedWords {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner in = new Scanner(new File("/home/haiboself/GraduateProject/dataset/exercise_content.txt"));
		
		String text = in.nextLine(); 
		//独立Lucene实现
		StringReader re = new StringReader(text);
		IKSegmenter ik = new IKSegmenter(re,false);
		Lexeme lex = null;
		try {
		    while((lex=ik.next())!=null)
		    System.out.print(lex.getLexemeText()+"|");
		}catch (Exception e) {
		}
	}
}
