import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetLabels {
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(new File("/home/haiboself/GraduateProject/rawLabels.txt"));
		FileWriter out = new FileWriter(new File("/home/haiboself/GraduateProject/labels.txt"));
		
		String str = "";
		int index = 1;	
		
		Pattern pattern = Pattern.compile(">.*\\(.*\\)<");
		Matcher matcher;
		
		while(in.hasNextLine()){
			str = in.nextLine();
			
			matcher = pattern.matcher(str);
			if(matcher.find()){
				String s = matcher.group(0);
				s = s.substring(s.lastIndexOf('>')+1,s.lastIndexOf('('));
				
				out.write(s+"\n");
				System.out.println(s);
			}
		}
		
		in.close();
		out.close();
	}
}
