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

package com.sqleo.environment;

import java.awt.Dimension;
import java.util.Hashtable;

import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.querybuilder.QueryBuilder;


public class Preferences
{
	/* BAD */
	public static void loadDefaults()
	{
		if(Application.session.mount(Application.ENTRY_PREFERENCES).size() == 0)
		{
			Application.session.mount(Application.ENTRY_PREFERENCES).add(new Hashtable());
			
			set("window.height"	,new Integer(600));
			set("window.width"	,new Integer(800));
	        
			set("explorer.navigator.datasources.name.width"		,new Integer(30));
			set("explorer.navigator.datasources.url.width"		,new Integer(250));
			set("explorer.navigator.datasources.status.width"	,new Integer(30));
		}
		
		QueryBuilder.autoJoin = getBoolean("querybuilder.auto-join",true);
		QueryBuilder.loadObjectsAtOnce = getBoolean("querybuilder.load-objects-at-once",true);

		// fix for Ticket #63
		QueryBuilder.autoAlias = getBoolean("querybuilder.auto-alias",false);
		QueryBuilder.useAlwaysQuote	= getBoolean("querybuilder.use-quote",false);
		QueryBuilder.selectAllColumns = getBoolean("querybuilder.select-all-columns",false);
	}
	
	private static Hashtable get()
	{
		return (Hashtable)Application.session.mount(Application.ENTRY_PREFERENCES).get(0);
	}
	
    public static void set(String key,Object value)
    {
		get().put(key,value);
    }
    
	public static boolean getBoolean(String key)
	{
	  try{	
		Boolean value = ((Boolean)get().get(key)).booleanValue();		
		return value;
	  }
	  catch(NullPointerException npe)
	  {
		  // Fix for ticket #50 by Alan Shiers
		  //System.out.println("Preferences.getBoolean() - NullPointerException: returning false");
		  return false;
	  }
	}
	
	public static boolean isAutoCompleteEnabled(){
		return getBoolean(DialogPreferences.AUTO_COMPLETE_KEY, false);
	}
	
	public static boolean isAutoSelectConnectionEnabled(){
		return getBoolean(DialogPreferences.AUTO_SELECT_CON_KEY, false);
	}
	
	public static void setAutoSelectConnectionEnabled(){
		if(!isAutoSelectConnectionEnabled()){
			set(DialogPreferences.AUTO_SELECT_CON_KEY, true);
		}
	}
	
	public static boolean savePosInSQL(){
		return getBoolean(DialogPreferences.QB_SAVE_POS_IN_SQL, false);
	}
	
	public static boolean getBoolean(String key, boolean defaultValue)
	{
		if(!get().containsKey(key))
			set(key,new Boolean(defaultValue));
		
		return getBoolean(key);
	}
	
	public static int getInteger(String key)
	{
		return ((Integer)get().get(key)).intValue();
	}
	
	public static int getInteger(String key, int defaultValue)
	{
		if(!get().containsKey(key))
			set(key,new Integer(defaultValue));
		
		return getInteger(key);
	}
	
	public static String getString(String key)
	{
		return get().get(key).toString();
	}

	public static String getString(String key, String defaultValue)
	{
		if(!get().containsKey(key))
			set(key,defaultValue);
		
		return getString(key);
	}

	public static boolean containsKey(String key) {
		return get().containsKey(key);
	}
	
	public static int getScaledRowHeight(final int oldRowHeight){
		final int fontSizePercentage = getInteger(DialogPreferences.FONT_SIZE_PERCENTAGE, DialogPreferences.DEFAULT_FONT_PERCENT);
	    if (fontSizePercentage != 100) {
	    	final float multiplier = fontSizePercentage / 100.0f;
	    	final int newHeight = Math.round(oldRowHeight * multiplier);
	    	return newHeight;
	    }
	    return oldRowHeight;
	}
	
	public static Dimension getScaledDimension(int w, int h){
		return new Dimension(getScaledRowHeight(w), getScaledRowHeight(h));
	}
	
	
	
}