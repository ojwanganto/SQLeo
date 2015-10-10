/*
 * SQLeonardo :: java database frontend
 * Copyright (C) 2004 nickyb@users.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.sqleo.environment.ctrl.content;

import java.io.File;

import javax.swing.JCheckBox;

import com.sqleo.common.gui.AbstractDialogWizard;
import com.sqleo.common.gui.AbstractMaskChooser;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.environment.io.FileHelper;
import com.sqleo.environment.mdi.DefaultMaskChooser;


public class DialogStream extends AbstractDialogWizard
{
	private String tname = null;
	private ContentView view;
	
	private JCheckBox cbxOpen;
	
	private AbstractMaskPerform mkp;
	private AbstractMaskChooser mkc;
	
	private TaskOwner task;
	
	private DialogStream(String title, ContentView view, String tname)
	{
		super(Application.window,title);
		
		this.tname = tname;
		this.view = view;
		
		cbxOpen = new JCheckBox("Open file when finished",Preferences.getBoolean("content.open-file",true));
		cbxOpen.setVisible(false);		
		bar.add(cbxOpen,0);
	}

	public static void showExport(ContentPane content)
	{
		String tname = null;
		new DialogStream("Export", content.getView(), tname).setVisible(true);
	}
	
	public static void showExportExcel(ContentPane content)
	{
		String tname = null;
		new DialogStream("ExportExcel", content.getView(), tname).setVisible(true);
	}
	
	public static void showImport(ContentPane content)
	{
		String tname = null;
		new DialogStream("Import", content.getView(), tname).setVisible(true);
	}
	
	public void dispose()
	{
		Preferences.set("content.open-file",new Boolean(cbxOpen.isSelected()));
		super.dispose();		
	}

	protected boolean onBack()
	{
		cbxOpen.setVisible(false);
		return super.onBack();
	}

	protected boolean onNext()
	{
		cbxOpen.setVisible(true);
		
		if(getStep()==0)
		{
			if(mkc.getSelectedFile()!=null)
			{
				mkp.setType(mkc.getPerformType(),tname,mkc.getSelectedFile().toString(), true);
				mkp.setContent(view);
				
				return true;
			}
		}
		else
		{
			new Thread(task = new TaskOwner()).start();
		}
		
		return false;
	}
	
	protected void onOpen()
	{
		if(this.getTitle().equals("ExportExcel"))
		{
			mkp = new MaskExport();
			mkc = new DefaultMaskChooser(AbstractMaskChooser.SAVE_DIALOG,AbstractMaskChooser.FILES_ONLY,false);
		
			mkc.addChoosableFileFilter(new CSVFilter());
		}else {
			if(this.getTitle().equals("Export"))
			{
				mkp = new MaskExport();
				mkc = new DefaultMaskChooser(AbstractMaskChooser.SAVE_DIALOG,AbstractMaskChooser.FILES_ONLY,false);
			
				mkc.addChoosableFileFilter(new SQLFilter());
				mkc.addChoosableFileFilter(new WebFilter());
				
			}
			else if(this.getTitle().equals("Import"))
			{
				mkp = new MaskImport();
				mkc = new DefaultMaskChooser(AbstractMaskChooser.OPEN_DIALOG,AbstractMaskChooser.FILES_ONLY,false);
			}
			mkc.addChoosableFileFilter(new TXTFilter());
			mkc.addChoosableFileFilter(new CSVFilter()); // ticket #233
		}
		
		addStep(mkc);
		addStep(mkp);
		
		getContentPane().validate();		
	}
	
	private class TaskOwner implements Runnable
	{
		public void run()
		{
			DialogStream.this.setBarEnabled(false);
			DialogStream.this.mkp.setEnabled(false);
			DialogStream.this.mkp.init();
			
			if(DialogStream.this.mkp instanceof MaskExport){
				MaskExport exporter = (MaskExport)DialogStream.this.mkp;
				if(!exporter.isExportFromGrid()){
					exporter.export();
				}else{
					while(DialogStream.this.task != null && !DialogStream.this.mkp.aborted() && !DialogStream.this.mkp.finished())
					{
						DialogStream.this.mkp.next();
					}
				}
				
			}else{
				while(DialogStream.this.task != null && !DialogStream.this.mkp.aborted() && !DialogStream.this.mkp.finished())
				{
					DialogStream.this.mkp.next();
				}
			}
			
			DialogStream.this.mkp.setEnabled(true);
			DialogStream.this.setBarEnabled(true);
			
			if(DialogStream.this.mkp.finished()){
				if(cbxOpen.isSelected()) {
					final String fileNameLbl = DialogStream.this.mkp.lblFile.getText();
					final String fileName = fileNameLbl.substring(6);
				    FileHelper.openFile(new File(fileName));
				}
				DialogStream.this.dispose();
			}
		}
	}
	
	private static class SQLFilter extends AbstractMaskChooser.AbstractFileFilter
	{
		SQLFilter(){super("insert statements",new String[]{".sql"});}
		public short getPerformType(){return MaskExport.SQL;}
	}
	 
	private static class TXTFilter extends AbstractMaskChooser.AbstractFileFilter
	{
		TXTFilter(){super("text files",new String[]{".txt"});}
		public short getPerformType(){return MaskExport.TXT;}
	}
	
	private static class CSVFilter extends AbstractMaskChooser.AbstractFileFilter
	{
		CSVFilter(){super("csv files",new String[]{".csv"});}
		public short getPerformType(){return MaskExport.CSV;}
	}
	
	private static class XLSFilter extends AbstractMaskChooser.AbstractFileFilter
	{
		XLSFilter(){super("excel files",new String[]{".xls"});}
		public short getPerformType(){return MaskExport.XLS;}
	}
	
	private static class WebFilter extends AbstractMaskChooser.AbstractFileFilter
	{
		WebFilter(){super("web pages",new String[]{".htm",".html"});}
		public short getPerformType(){return MaskExport.WEB;}
	}
}