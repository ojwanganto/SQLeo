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

package nickyb.sqleonardo.environment;

import java.util.Hashtable;

import nickyb.sqleonardo.querybuilder.QueryBuilder;

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
		QueryBuilder.autoAlias = getBoolean("querybuilder.auto-alias",true);
		QueryBuilder.useAlwaysQuote	= getBoolean("querybuilder.use-quote",true);
		QueryBuilder.loadObjectsAtOnce = getBoolean("querybuilder.load-objects-at-once",true);
		QueryBuilder.selectAllColumns = getBoolean("querybuilder.select-all-columns",true);
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
		return ((Boolean)get().get(key)).booleanValue();
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
}