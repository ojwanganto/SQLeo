package com.sqleo.environment.io;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.sqleo.environment.Application;

public class FileHelper {

	public static boolean writeTextToFile(String text,File file,boolean append, boolean openFile){
		PrintStream out = null;
		boolean saveSuccess = false;
		try {
			// Ticket #45 this use default encoding, can be modified when launching app by
			// java -Dfile.encoding=UTF-8 -jar SQLeoVQB.jar 
			out = new PrintStream(new FileOutputStream(file,append));
			out.print(text);
			saveSuccess = true;
		} catch (FileNotFoundException e) {
			saveSuccess = false;
			Application.println(e, true);
			e.printStackTrace();
		}finally{
			if(out!=null){
				out.close();
			}
		}
		if(saveSuccess && openFile){
			openFile(file);
		}
		return saveSuccess;
	}
	
	public static void openFile(final File file){
		if (Desktop.isDesktopSupported() && file!=null) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Please check whether any editor is configured to open the file type",
						"Cannot open file "+file.getName(),JOptionPane.WARNING_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"Java is unable to open the files on your computer.",
					"Cannot open file "+file.getName(),JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public static <T> void saveAsXml(final String filename,final T object){
		saveAsXml(new File(filename), object);
	}
	
	public static <T> void saveAsXml(final File file,final T object){
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(object,file);
		} catch (JAXBException e) {
			Application.println(e, false);
			e.printStackTrace();
		}
	}
	
	public static <T> T loadXml(final String filename,final Class<T> klass){
		return loadXml(new File(filename), klass);
	}
	
	public static <T> T loadXml(final File file,final Class<T> klass){
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(klass);
			final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			@SuppressWarnings("unchecked")
			final T result = (T) jaxbUnmarshaller.unmarshal(file); 
			return result ;
		} catch (JAXBException e) {
			Application.println(e, false);
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getFileExtension(final String fileName){
	    int lastIndexOf = fileName.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return fileName.substring(lastIndexOf+1);
	}
		
}
