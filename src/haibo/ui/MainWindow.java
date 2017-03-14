package haibo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.PKIXRevocationChecker.Option;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import haibo.alogrithm.Annotate;
import haibo.alogrithm.Train;
import haibo.alogrithm.Util;

/**
 * 
 * @author haiboself
 * 2017-03-01
 * 这是程序的主界面
 */

public class MainWindow extends JFrame implements ActionListener{
	private JButton trainJbt;	   //训练分类器
	private JButton annotateJbt;   //进行知识点标注
	private JButton openRawData;   //打开要进行标注的文件
	private JButton saveResult;    //保存标注结果
	private JTextArea rawDataJtx;  //显示未标注的文件内容
	private JTextArea resultJtx;   //显示标注的结果
	private JButton exit;

	private JDialog set;	//用于获取训练所需要的文件路径
	private JFileChooser chooser;  //用于保存和打开文件

	private Train train;
	
	private Scanner inRawData;	//需要进行标注的文件

	private MainWindow(){
		chooser 	= new JFileChooser();
		//按钮布局
		trainJbt    = new JButton("Train Model");
		annotateJbt = new JButton("Annotation");
		openRawData = new JButton("Open File");
		saveResult  = new JButton("Save");
		exit		= new JButton("Exit");

		JPanel menu = new JPanel();
		menu.setLayout(new GridLayout(0,5));

		menu.add(trainJbt);
		menu.add(annotateJbt);
		menu.add(openRawData);
		menu.add(saveResult);
		menu.add(exit);

		add(menu,BorderLayout.NORTH);

		//文本显示区域布局
		rawDataJtx = new JTextArea("show raw data.");
		resultJtx  = new JTextArea("show result after marking.");
		rawDataJtx.setBorder(BorderFactory.createLineBorder(Color.GREEN,3));
		resultJtx.setBorder(BorderFactory.createLineBorder(Color.ORANGE,3));

		JPanel display = new JPanel();
		display.setLayout(new GridLayout(0,2));
		display.add(new JScrollPane(rawDataJtx));
		display.add(new JScrollPane(resultJtx));

		add(display,BorderLayout.CENTER);

		//窗口总体属性设置
		this.setSize(800,600);
		this.setTitle("Mutil-Label Classfication");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

		//添加事件监听
		trainJbt.addActionListener(this);
		annotateJbt.addActionListener(this);
		openRawData.addActionListener(this);
		saveResult.addActionListener(this);
		exit.addActionListener(this);

	}

	public static void main(String[] args) {
		MainWindow display = new MainWindow();
	}

	//事件监听
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == trainJbt){
			showTrainDialog();
		}
		if(e.getSource() == annotateJbt) {
			try {
				annotate();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(e.getSource() == saveResult){
			try {
				saveRes();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	//保存标注结果
		}
		if(e.getSource() == openRawData ){
			int returnVal = chooser.showOpenDialog(getParent());

			if(returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					inRawData = new Scanner(chooser.getSelectedFile());
					while(inRawData.hasNextLine()){
						rawDataJtx.append(inRawData.nextLine()+"\n");
					}
					
					inRawData.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
		if(e.getSource() == exit){
			System.exit(0);
		}
	}

	private void saveRes() throws IOException {
		if(resultJtx.getText().equals("show result after marking.") || resultJtx.getText() == null){
			JOptionPane.showConfirmDialog(this,"Please click button Annotation first.");
			return;
		}
		
		int flag = chooser.showSaveDialog(this);
		if(flag==JFileChooser.APPROVE_OPTION)   
        {   
            //获得你输入要保存的文件   
              File fc = chooser.getSelectedFile();
              if(!fc.exists())	fc.createNewFile();
            //保存标注结果   
              PrintWriter out = new PrintWriter(fc);
              out.print(resultJtx.getText());
              out.close();
        }   
	}

	//进行标注
	private void annotate() throws IOException {
		//选择一个数据模型
		File modelFileDir = new File(Util.MODEL);
		String[] models = modelFileDir.list();
		
		//没有模型
		if(models.length == 0){
			JOptionPane.showConfirmDialog(this,"There has no DataModel,please click train button fisrt.");
			return;
		}
		else{
			int m = JOptionPane.showOptionDialog(this,"Please select a model:","Select Model",JOptionPane.CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE, null, models,0);
			if(rawDataJtx.getText() .equals("show raw data.") || rawDataJtx.getText() == null)
				inRawData = new Scanner(rawDataJtx.getText());
			Annotate anno = new Annotate(inRawData,models[m]);
	
			resultJtx.setText(anno.annotate());
		}
	}

	//用于获取训练所需要的文件路径
	private void showTrainDialog() {
		
		//对话框面板内容
		JLabel	   tip			= new JLabel("Project Name : ");
		JTextField proName 		= new JTextField("exercise",15);	//项目名称
		FileSelect content 		= new FileSelect("Content","./data/exercise_content.txt");	//训练文件内容
		FileSelect annotation   = new FileSelect("Annotation","./data/exercise_annotation.txt"); //训练文件内容的标注信息
		FileSelect labels 		= new FileSelect("Labels","./data/labels.txt");	 	//训练文件的标签集信息
		JButton    submit 		= new JButton("Submit");		//确认按钮

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(6, 0));
		p.add(tip);
		p.add(proName);
		p.add(content);
		p.add(annotation);
		p.add(labels);
		p.add(submit);

		submit.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				train = new Train(content.getPath(),annotation.getPath(),labels.getPath(),proName.getText());
				set.setVisible(false);
			}
		});

		//对话框属性
		set = new JDialog(this,true);
		set.setTitle("Train");
		set.setBounds(this.getX(),this.getY(), 500, 300);	
		set.add(p);
		set.setVisible(true);
	}
}


