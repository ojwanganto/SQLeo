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

import java.util.Vector;

public class ContentChanges
{
	static final Integer DELETE = new Integer(0);
	static final Integer INSERT = new Integer(1);
	static final Integer UPDATE = new Integer(2);
		
	private Vector store = new Vector();

	void aborted(Long rid)
	{
		store.removeElement(new Handler(DELETE,rid));
		store.removeElement(new Handler(INSERT,rid));
		store.removeElement(new Handler(UPDATE,rid));
	}
	
	boolean exists(Integer type,Long rid)
	{
		return store.contains(new Handler(type,rid)); 
	}
	
	public boolean existsOnlyInsert()
	{
		final int total = count();
		if(total == 0){
			return false;
		}
		for(int idx=0; idx < total;idx++){
			if(!((Handler) store.elementAt(idx)).type.equals(INSERT)){
				return false;
			}
		}
		return true; 
	}
	
	public int count()
	{
		return store.size();
	}
	
	Handler getHandlerAt(int idx)
	{
		return (Handler)store.elementAt(idx);
	}
	
	Handler getHandlerAt(Long rid)
	{
		int idx = -1;
		for(int i = 0; i<=2; i++){
			idx = store.indexOf(new Handler(i,rid));
			if(idx!=-1){
				break;
			}
		}
		return idx!=-1 ? (Handler)store.elementAt(idx) : null;
	}

	void removeHandlerAt(int idx)
	{
		store.removeElementAt(idx);
	}
		
	void setDeleted(Long rid)
	{
		boolean need = !store.contains(new Handler(INSERT,rid));
		aborted(rid);
			
		if(need) store.addElement(new Handler(DELETE,rid));
	}
		
	void setInserted(Long rid)
	{
		store.addElement(new Handler(INSERT,rid));
	}
		
	void setUpdated(Long rid)
	{
		if(!store.contains(new Handler(INSERT,rid))
		&& !store.contains(new Handler(UPDATE,rid)))
		{
			store.addElement(new Handler(UPDATE,rid));
		}
	}		

	class Handler
	{
		Integer type;
		Long rid;
			
		Handler(Integer type,Long rid)
		{
			this.type = type;
			this.rid = rid;
		}
			
		public boolean equals(Object o)
		{
			return type.equals(((Handler)o).type) && rid.equals(((Handler)o).rid);
		}
			
		public String toString()
		{
			if(type.equals(DELETE))
				return "DELETEed :" + rid;
			else if(type.equals(INSERT))
				return "INSERTed :" + rid;
			else if(type.equals(UPDATE))
				return "UPDATEed :" + rid;
				
			return null;
		}
	}
}