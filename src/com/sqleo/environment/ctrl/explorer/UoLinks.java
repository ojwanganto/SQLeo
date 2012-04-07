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

package com.sqleo.environment.ctrl.explorer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import com.sqleo.common.util.I18n;


public class UoLinks
{
	private static String DEFAULT_LABEL = I18n.getString("application.objetctype.links", "Linked objects");
	private Hashtable groups = new Hashtable();

	public void add(String group,String schema,String name,String type)
	{
		ArrayList links = getLinks(group);
		links.add(new String[]{schema,name,type});
	}

	public ArrayList getLinks(String group)
	{
		ArrayList links = new ArrayList();
		
		if(groups.containsKey(group))
			links = (ArrayList)groups.get(group);
		else
			groups.put(group,links);
		
		return links;
	}	

	public Set getGroups()
	{
		return groups.keySet();
	}

	public void removeGroup(String group)
	{
		groups.remove(group);
	}

	public String toString()
	{
		return DEFAULT_LABEL;
	}
}