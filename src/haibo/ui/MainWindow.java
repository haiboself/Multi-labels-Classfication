package haibo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import haibo.alogrithm.Annotate;
import haibo.alogrithm.Train;
import weka.gui.streams.InstanceJoiner;

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

		JPanel menu = new JPanel();
		menu.setLayout(new GridLayout(0,4));

		menu.add(trainJbt);
		menu.add(annotateJbt);
		menu.add(openRawData);
		menu.add(saveResult);

		add(menu,BorderLayout.NORTH);

		//文本显示区域布局
		rawDataJtx = new JTextArea("这里显示未标记数据");
		resultJtx  = new JTextArea("这里显示标记结果");
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
			annotate();
		}
		if(e.getSource() == saveResult){
			//new MapEditor();
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
	}

	//进行标注
	private void annotate() {
		if(rawDataJtx.getText() != null){
			inRawData = new Scanner(rawDataJtx.getText());
			Annotate anno = new Annotate(inRawData);
			
			try {
				Scanner in = new Scanner(new File(anno.annotate()));
				while(in.hasNextLine())
					resultJtx.append(in.nextLine()+"\n");
				in.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else return;
	}

	//用于获取训练所需要的文件路径
	private void showTrainDialog() {
		//对话框面板内容
		FileSelect content 		= new FileSelect("Content","./data/exercise_content.txt");	//训练文件内容
		FileSelect annotation   = new FileSelect("Annotation","./data/exercise_annotation.txt"); //训练文件内容的标注信息
		FileSelect labels 		= new FileSelect("Labels","./data/labels.txt");	 	//训练文件的标签集信息
		JButton    submit 		= new JButton("Submit");		//确认按钮

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 0));
		p.add(content);
		p.add(annotation);
		p.add(labels);
		p.add(submit);

		submit.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				train = new Train(content.getPath(),annotation.getPath(),labels.getPath());
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


