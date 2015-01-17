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

package com.sqleo.common.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.io.FileHelper;
import com.sqleo.environment.mdi.ClientSQLHistoryViewer;
import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.environment.mdi.MDIClient;

public class Store
{
	protected static final int INDEX_DATA = 0;
	protected static final int INDEX_SUBS = 1;
	protected static final int INDEX_JUMP = 2;
	
	/* mount-points */
	private Hashtable mountpoints;
	
	/* Current mount-point */
	private Object[] cmp;
	
	//Cache tables prefixTree, Cache table columns, Cache joinColums (All needed for autocomplete feature)
	private Hashtable columnsCache;
	
	private LinkedList<SQLHistoryData> sqlHistoryData;
	
	public Store()
	{
		mountpoints = new Hashtable();
		columnsCache = new Hashtable();
		sqlHistoryData = new LinkedList<SQLHistoryData>();
		
		cmp = new Object[3];
		cmp[INDEX_DATA] = new ArrayList();
		cmp[INDEX_SUBS] = new Hashtable();
	}
	
	public void addSQLToHistory(final SQLHistoryData historyData){
		final Integer max =  Preferences.getInteger(DialogPreferences.MAX_QUERIES_IN_HISTORY, 
				DialogPreferences.DEFAULT_MAX_QUERIES_IN_HISTORY);
		if(max>0){
			//notify sqlhistory viewer
			final MDIClient historyView = Application.window.getClient(ClientSQLHistoryViewer.DEFAULT_TITLE);
			if(sqlHistoryData.size() == max){
				// if we reach max, remove last one 
				sqlHistoryData.removeLast();
				if(historyView!=null){
					final ClientSQLHistoryViewer historyViewer = (ClientSQLHistoryViewer)historyView;
					historyViewer.removeLastRow();
				}
			}
			//add to first, as we need to store last N queries by timestamp
			sqlHistoryData.addFirst(historyData);
			if(historyView!=null){
				final ClientSQLHistoryViewer historyViewer = (ClientSQLHistoryViewer)historyView;
				historyViewer.addRowAtFirst(historyData);
			}
		}
	}
	
	public void removeSQLFromHistory(final String timestamp){
		for(int i = 0; i<sqlHistoryData.size();i++){
			if(sqlHistoryData.get(i).getTimestamp().equals(timestamp)){
				sqlHistoryData.remove(i);
				break;
			}
		}
	}
	
	public LinkedList<SQLHistoryData> getSQLHistoryData(){
		return  sqlHistoryData;
	}
	
	protected Object[] get(String entry)
	{
		return (Object[])mountpoints.get(entry);
	}

	protected void put(String entry, Object[] content)
	{
		mountpoints.put(entry,content);
	}
	
	public Object getColumnCache(String entry)
	{
		return columnsCache.get(entry);
	}
	
	public void putColumnCache(String entry, Object content)
	{
		columnsCache.put(entry,content);
	}
	
	public void home()
	{
		cmp[INDEX_JUMP] = null;
	}

	public boolean canMount(String entry)
	{
		return mountpoints.containsKey(entry);
	}

	public ArrayList mount()
	{
		return (ArrayList)cmp[INDEX_DATA];
	}
	
	public ArrayList mount(String entry)
	{
		if(mountpoints.containsKey(entry))
		{
			cmp = (Object[])mountpoints.get(entry);
		}
		else
		{
			cmp = new Object[3];
			cmp[INDEX_DATA] = new ArrayList();
			cmp[INDEX_SUBS] = new Hashtable();
			
			mountpoints.put(entry,cmp);
		}
		
		return mount();
	}
	
	public Enumeration mounts()
	{
		return mountpoints.keys();
	}
	
	public void umount(String entry)
	{
		cmp = new Object[3];
		cmp[INDEX_DATA] = new ArrayList();
		cmp[INDEX_SUBS] = new Hashtable();
		
		mountpoints.remove(entry);
	}

	public boolean canJump(String sub)
	{
		Hashtable subs = (Hashtable)cmp[INDEX_SUBS];
		return subs.containsKey(sub);
	}
	
	public ArrayList jump()
	{
		return (ArrayList)((Object[])cmp[INDEX_JUMP])[INDEX_DATA];
	}
	
	public ArrayList jump(String sub)
	{
		Hashtable subs = (Hashtable)cmp[INDEX_SUBS];
		if(cmp[INDEX_JUMP] != null)
		    subs = (Hashtable)((Object[])cmp[INDEX_JUMP])[INDEX_SUBS];
		
		if(subs.containsKey(sub))
		{
			cmp[INDEX_JUMP] = (Object[])subs.get(sub);
		}
		else
		{
			/* jump-point */
			Object[] jp = new Object[2];
			jp[INDEX_DATA] = new ArrayList();
			jp[INDEX_SUBS] = new Hashtable();
			
			subs.put(sub,jp);
			cmp[INDEX_JUMP] = jp;
		}
		
		return jump();
	}
	
	public ArrayList jump(String[] subs)
	{
		for(int i=0; i<subs.length; i++) jump(subs[i]);
		return jump();
	}

	public Enumeration jumps()
	{
		Hashtable subs = (Hashtable)cmp[INDEX_SUBS];
		if(cmp[INDEX_JUMP] != null)
		    subs = (Hashtable)((Object[])cmp[INDEX_JUMP])[INDEX_SUBS];
		
	    return subs.keys();
	}
	
	public void ujump(String sub)
	{
		Hashtable subs = (Hashtable)cmp[INDEX_SUBS];
		subs.remove(sub);
		
		home();
	}
	
	public void rename(String oldentry, String newentry)
	{
		Object[] obj = (Object[])mountpoints.get(oldentry);		
		mountpoints.put(newentry,obj);
		mountpoints.remove(oldentry);
	}

	public void reset()
	{
		mountpoints = new Hashtable();
		
		cmp = new Object[3];
		cmp[INDEX_DATA] = new ArrayList();
		cmp[INDEX_SUBS] = new Hashtable();
	}
	
	public void load(String filename) throws IOException, ClassNotFoundException
	{
		ZipInputStream zin = new ZipInputStream(new FileInputStream(filename));
		
		for(ZipEntry entry = null; (entry = zin.getNextEntry())!=null;)
		{
			Object[] content = new Object[3];
			content[INDEX_DATA] = new ObjectInputStream(zin).readObject();
			content[INDEX_SUBS] = new ObjectInputStream(zin).readObject();
			this.put(entry.getName(),content);
		}
			
		zin.close();
	}
	
	public void save(String filename) throws IOException
	{
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(filename));
		
		for(Enumeration e = mounts(); e.hasMoreElements();)
		{
			String name = e.nextElement().toString();
			zout.putNextEntry(new ZipEntry(name));
			
			Object[] content = this.get(name);
			new ObjectOutputStream(zout).writeObject(content[INDEX_DATA]);
			new ObjectOutputStream(zout).writeObject(content[INDEX_SUBS]);
			
			zout.closeEntry();
		}
		zout.close();
	}
	public void loadXMLAndMetaviews(String filename,String metaviewFileName) throws IOException, ClassNotFoundException
	{
		XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(
                    new FileInputStream(filename)));
		ZipInputStream zin = new ZipInputStream(new FileInputStream(metaviewFileName));
		Object zipContent = null; 
		for(ZipEntry entry = null; (entry = zin.getNextEntry())!=null;)
		{
			zipContent = new ObjectInputStream(zin).readObject();
		}
		
		try{
			while(true)
			{
				Object name = decoder.readObject();
				String nameStr = (String)name;
				Object[] content = new Object[3];
				if(Application.ENTRY_PREFERENCES.equals(nameStr)){
					content[INDEX_DATA] = decoder.readObject();
					content[INDEX_SUBS] = zipContent;
				}else {
					content[INDEX_DATA] = decoder.readObject();
					content[INDEX_SUBS] = decoder.readObject();
				}
				this.put(nameStr,content);
			}
		}catch(ArrayIndexOutOfBoundsException e){
			//REACHED END OF FILE
		}finally{
			decoder.close();
			zin.close();
		}
	}
	
	public void saveSQLHistoryAsXml(final String filename){
		final Integer maxQueriesToSave = Preferences.getInteger(DialogPreferences.MAX_QUERIES_IN_HISTORY,
				DialogPreferences.DEFAULT_MAX_QUERIES_IN_HISTORY);
		final Integer subListMax = sqlHistoryData.size() > maxQueriesToSave ? maxQueriesToSave : sqlHistoryData.size();

		final SQLHistory history = new SQLHistory();
		history.setSqlHistoryLines(new LinkedList<SQLHistoryData>(sqlHistoryData.subList(0, subListMax)));
		
		FileHelper.saveAsXml(filename, history);
	}
	
	public void loadSQLHistoryAsXml(final String filename){
		final SQLHistory history = FileHelper.loadXml(filename, SQLHistory.class); 
		if(history!=null && history.getSqlHistoryLines()!=null){
			sqlHistoryData = history.getSqlHistoryLines();	
		}
	}

	public void saveXMLAndMetaviews(String filename,String metaviewFileName) throws IOException
	{
	   XMLEncoder encoder = new XMLEncoder(
                   new BufferedOutputStream(
                       new FileOutputStream(filename)));
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(metaviewFileName));

		for(Enumeration e = mounts(); e.hasMoreElements();)
		{
			String name = e.nextElement().toString();
			encoder.writeObject(name);
			Object[] content = this.get(name);
			if(Application.ENTRY_PREFERENCES.equals(name)){
				encoder.writeObject(content[INDEX_DATA]);
				zout.putNextEntry(new ZipEntry(name));
				new ObjectOutputStream(zout).writeObject(content[INDEX_SUBS]);
				zout.closeEntry();
			}else {
				encoder.writeObject(content[INDEX_DATA]);
				encoder.writeObject(content[INDEX_SUBS]);
			}
			
		}
		encoder.close();
		zout.close();
		
	}

	private void print()
	{
		Enumeration e = mountpoints.keys();
		while(e.hasMoreElements())
		{
			String entry = e.nextElement().toString();
			System.out.println("*** "+entry+" ***");
			
			print("",(Object[])mountpoints.get(entry));
		}
	}
	
	private void print(String indent,Object[] obj)
	{
		ArrayList al = (ArrayList)obj[INDEX_DATA];
//		System.out.println(indent + al);
		System.out.print(indent + al.size() + "{");
		for(int i=0; i<al.size(); i++)
			System.out.print(al.get(i) + ",");
		System.out.println("}");
		
		Hashtable h = (Hashtable)obj[INDEX_SUBS];
		Enumeration e = h.keys();
		while(e.hasMoreElements())
		{
			String entry = e.nextElement().toString();
			System.out.println(indent + "\t" + entry);
			
			print(indent + "\t",(Object[])h.get(entry));
		}
	}
	
//	public static void main(String[] args)
//	{
//		Store s = new Store();
//		try
//		{
//			String filename = System.getProperty("user.home") + "/.sqleo";
//			System.out.println("### " + filename + " ###");
//			s.load(filename);
//			s.print();
//			s.reset();
//			
//			filename = "D:/Temp/w2k/test.2006.05.rc1.lqy";
//			System.out.println("### " + filename + " ###");
//			s.load(filename);			
//			s.print();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		catch (ClassNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//	}
}