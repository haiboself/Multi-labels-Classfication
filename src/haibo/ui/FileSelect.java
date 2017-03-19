package haibo.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//从磁盘中选择未标记的txt文件.
public final class FileSelect extends JPanel{
	private JLabel name;
	private JTextField content;
	private JButton selectFile;
	
	private JFileChooser chooser;
	private String path;	//文件路径
	
	public FileSelect(String s,String pa){
		
		name = new JLabel(s);
		content = new JTextField(20);
		selectFile = new JButton("File");
		chooser = new JFileChooser();
		
		add(name,BorderLayout.WEST);
		add(content, BorderLayout.CENTER);
		add(selectFile,BorderLayout.EAST);
		
		this.setVisible(true);
		content.setText(pa);
		this.path = pa;
		
		selectFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				 int returnVal = chooser.showOpenDialog(getParent());
				    
				 if(returnVal == JFileChooser.APPROVE_OPTION) {
				       path = chooser.getSelectedFile().getPath();
				       content.setText(path);
				 }
			}
		});
	}
	
	public String getPath(){
		if(path != null) return path;
		else throw new NullPointerException();
	}
}