package haibo.alogrithm;

/**
 * @author haiboself
 *
 */
public class Term {

	public String name;
	public double num;


	public Term(String x, double sU_lx) {
		name = x;
		num = sU_lx;
	}

	public String getName(){
		return name;
	}
	public double getNum(){
		return num;
	}
	
	public boolean equals(Term o){
		return name.equals(o.name);
	}
}
