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

package com.sqleo.querybuilder;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import com.sqleo.querybuilder.syntax.QuerySpecification;


public class DiagramLayout
{
	private QueryBuilder builder;
	
	private HashMap map = new HashMap();
	private QueryModel qmodel = new QueryModel();
	
	public QueryModel getQueryModel()
	{
		return qmodel;
	}	
	
	public void setQueryModel(QueryModel qm)
	{
		qmodel = qm;
	}
	
    public void freeze()
    {
    	DiagramAbstractEntity[] entities = builder.diagram.getEntities();
    	EntityExtra[] extras = new EntityExtra[entities.length];
    	
    	for(int i=0;i<entities.length; i++)
    	{
    		extras[i] = new EntityExtra();
    		extras[i].setReference(entities[i].getHeaderMenu().getText());
    	    extras[i].setLocation(entities[i].getLocation());
    	    extras[i].setPack(entities[i].isPack());
    	}
    	
    	setExtras(builder.browser.getQuerySpecification(),extras);
    }
    
    void resume()
    {
    	EntityExtra[] extras = getExtras(builder.browser.getQuerySpecification());
    	if(extras!=null)
    	{
    		DiagramAbstractEntity[] entities = builder.diagram.getEntities();    		
        	for(int i=0;i<extras.length; i++)
        	{
        		String reference = extras[i].getReference();
        		for(int j=0; j<entities.length; j++)
        		{
    				if(entities[j].getHeaderMenu().getText().equalsIgnoreCase(reference))
    				{
    					entities[j].setLocation(extras[i].getLocation());
    					entities[j].setPack(extras[i].isPack());
    					break;
    				}
        		}
        	}    		
    	}
    }
    
    public EntityExtra[] getExtras(QuerySpecification qs)
    {
    	return map.containsKey(qs) ? (EntityExtra[])map.get(qs) : null;
    }

    public void setExtras(QuerySpecification qs,EntityExtra[] extras)
    {
    	map.put(qs,extras);
    }
    
    public void resetExtras(final HashMap newExtras){
    	map.clear(); map.putAll(newExtras);
    }
    public HashMap getExtras(){
    	return map;
    }

    void setQueryBuilder(QueryBuilder builder)
    {
    	this.builder = builder;
    }
    
    public static class EntityExtra
    {
    	private String reference;
    	private Point location;
    	private boolean pack;
    	
		public Point getLocation()
		{
			return location;
		}
		
		public void setLocation(Point location)
		{
			this.location = location;
		}
		
		public boolean isPack()
		{
			return pack;
		}
		
		public void setPack(boolean pack)
		{
			this.pack = pack;
		}
		
		public String getReference()
		{
			return reference;
		}
		
		public void setReference(String reference)
		{
			this.reference = reference;
		}
    }
}
