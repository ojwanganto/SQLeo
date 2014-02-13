package com.sqleo.environment.io;

import java.awt.Desktop;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.sqleo.environment.Application;

public class FileHelper {

	public static boolean writeTextToFile(String text,String fileName,boolean append, boolean openFile){
		PrintStream out = null;
		boolean saveSuccess = false;
		try {
			// Ticket #45 this use default encoding, can be modified when launching app by
			// java -Dfile.encoding=UTF-8 -jar SQLeoVQB.jar 
			out = new PrintStream(new FileOutputStream(fileName));
			out.print(text);
			saveSuccess = true;
		} catch (FileNotFoundException e) {
			saveSuccess = false;
			Application.println(e, true);
			e.printStackTrace();
		} catch (IOException e) {
			saveSuccess = false;
			Application.println(e, true);
			e.printStackTrace();
		}finally{
			if(out!=null){
				out.close();
			}
		}
		if(saveSuccess && openFile){
			openFile(fileName);
		}
		return saveSuccess;
	}
	
	public static void openFile(final String fileName){
		if (Desktop.isDesktopSupported() && fileName!=null) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(new File(fileName));
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Please check whether any editor is configured to open the file type",
						"Cannot open file "+fileName,JOptionPane.WARNING_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"Java is unable to open the files on your computer.",
					"Cannot open file "+fileName,JOptionPane.WARNING_MESSAGE);
		}
	}
		
}
