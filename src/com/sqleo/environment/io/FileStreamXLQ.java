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

package com.sqleo.environment.io;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.querybuilder.DiagramLayout;
import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QuerySpecification;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SubQuery;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class FileStreamXLQ
{
//	---------------------------------------------------------------
//	entry points
//	---------------------------------------------------------------
	public static DiagramLayout read(String filename)
		throws IOException, ClassNotFoundException
	{
		Reader r = new Reader(filename);
		return r.layout;
	}

	public static void write(String filename, DiagramLayout layout)
		throws IOException
	{
		new Writer(filename,layout);
	}

//	---------------------------------------------------------------
//	reader class
//	---------------------------------------------------------------
	private static class Reader
	{
		private DiagramLayout layout;
		private BufferedReader in; 
		
		Reader(String filename)
			throws IOException, ClassNotFoundException
		{
			layout = new DiagramLayout();
			in = new BufferedReader(new FileReader(filename));
			
			in.readLine(); // TAG:SQLEONARDO:OPEN
			String tag = in.readLine(); // TAG:MODEL:OPEN
			String schema = getAttribute(tag,"schema");
			layout.getQueryModel().setSchema(schema);
			
			in.readLine(); // TAG:QUERY:OPEN
			read(layout.getQueryModel().getQueryExpression());
			
			tag = in.readLine();  // TAG:ORDER_BY:OPEN
			if(!tag.endsWith("/>"))
			{
				/* leggi tokens */
				while(true)
				{
					tag = in.readLine();
					if(tag.equals("</ORDER_BY>") ) break;
					
					short t = Short.valueOf(getAttribute(tag,"type")).shortValue();
					layout.getQueryModel().addOrderByClause(new QueryTokens.Sort((QueryTokens._Expression)getToken(in.readLine()),t));
					
					in.readLine(); // TAG:SORT:CLOSE
				}
			}
			
			in.readLine(); // TAG:MODEL:CLOSE
			in.readLine();// TAG:SQLEONARDO:CLOSE
			in.close();
		}

		private void read(QueryExpression qe)
			throws IOException
		{
			String tag = in.readLine();
			if(tag.equals("<LAYOUT>"))
			{
				tag = in.readLine();
				
				ArrayList al = new ArrayList();
				do
				{	
					DiagramLayout.EntityExtra extra = new DiagramLayout.EntityExtra();
					al.add(extra);
					
					int x = Integer.valueOf(getAttribute(tag,"x")).intValue();
					int y = Integer.valueOf(getAttribute(tag,"y")).intValue();
					
					extra.setLocation(new Point(x,y));
					extra.setReference(getAttribute(tag,"reference"));
					extra.setPack(Boolean.valueOf(getAttribute(tag,"pack")).booleanValue());					
					
					tag = in.readLine();
				}
				while (!tag.equals("</LAYOUT>"));
				
				DiagramLayout.EntityExtra[] extras = new DiagramLayout.EntityExtra[al.size()];
				layout.setExtras(qe.getQuerySpecification(),(DiagramLayout.EntityExtra[])al.toArray(extras));
				
				tag = in.readLine(); // TAG:SELECT:OPEN
			}
			else if(tag.equals("<LAYOUT/>"))
			{
				tag = in.readLine();  // TAG:SELECT:OPEN
			}
			
			for(int i=0; i<5; i++)
			{
				if(!tag.endsWith("/>"))
				{
					switch(i)
					{
						case 0:
							read(qe.getQuerySpecification(),_ReservedWords.SELECT);
							break;
						case 1:
							read(qe.getQuerySpecification(),_ReservedWords.FROM);
							break;
						case 2:
							read(qe.getQuerySpecification(),_ReservedWords.WHERE);
							break;
						case 3:
							read(qe.getQuerySpecification(),_ReservedWords.GROUP_BY.replace(' ','_'));
							break;
						case 4:
							read(qe.getQuerySpecification(),_ReservedWords.HAVING);
							break;
					}
				}
				tag = in.readLine();
			}
			
/*			tag = in.readLine(); // TAG:QUERY:CLOSE || TAG:QUERY:OPEN -> union */
			
			
			if(tag.equals("<QUERY>"))
			{
				QueryExpression union = new QueryExpression();
				qe.setUnion(union);
				
				read(union);
				in.readLine(); // TAG:QUERY:CLOSE
			}
		}
	
		private void read(QuerySpecification qs, String clause)
			throws IOException
		{
			while(true)
			{
				String tag = in.readLine();
				if(tag.equals("</" + clause + ">") ) break;
				
				QueryTokens._Base token = getToken(tag);
				if(clause.equals(_ReservedWords.SELECT))
					qs.addSelectList((QueryTokens._Expression)token);
				else if(clause.equals(_ReservedWords.FROM))
					qs.addFromClause((QueryTokens._TableReference)token);
				else if(clause.equals(_ReservedWords.WHERE))
					qs.addWhereClause((QueryTokens.Condition)token);
				else if(clause.equals(_ReservedWords.GROUP_BY.replace(' ','_')))
					qs.addGroupByClause(new QueryTokens.Group((QueryTokens._Expression)token));
				else if(clause.equals(_ReservedWords.HAVING))
					qs.addHavingClause((QueryTokens.Condition)token);
			}
		}
		
		private QueryTokens._Base getToken(String tag)
			throws IOException
		{
			if(tag.startsWith("<QUERY"))
			{
				SubQuery sq = new SubQuery();
				read(sq);
				return sq;
			}
			
			if(tag.startsWith("<EXPRESSION"))
				return getExpression(tag);
			if(tag.startsWith("<COLUMN"))
				return getColumn(tag);
			if(tag.startsWith("<TABLE"))
				return getTable(tag);
			if(tag.startsWith("<CONDITION"))
				return getCondition(tag);
			if(tag.startsWith("<JOIN"))
				return getJoin(tag);
			
			return null;
		}

		private QueryTokens.DefaultExpression getExpression(String tag)
		{
			if(tag.equals("<EXPRESSION/>")) return null;
			return new QueryTokens.DefaultExpression(getAttribute(tag,"value"));
		}
	
		private QueryTokens.Column getColumn(String tag)
			throws IOException
		{
			QueryTokens.Column token = new QueryTokens.Column(getTable(in.readLine()),getAttribute(tag,"name"));
			token.setAlias(getAttribute(tag,"alias"));
			
			in.readLine(); // TAG:COLUMN:CLOSE
			
			return token;
		}
		
		private QueryTokens.Table getTable(String tag)
		{
			QueryTokens.Table token = new QueryTokens.Table(getAttribute(tag,"schema"),getAttribute(tag,"name"));
			token.setAlias(getAttribute(tag,"alias"));
			
			return token;
		}
		
		private QueryTokens.Condition getCondition(String tag)
			throws IOException
		{
			in.readLine(); // TAG:LEFT:OPEN
			QueryTokens._Expression left = (QueryTokens._Expression)getToken(in.readLine());
			in.readLine(); // TAG:LEFT:CLOSE
			
			in.readLine(); // TAG:RIGHT:OPEN
			QueryTokens._Expression right = (QueryTokens._Expression)getToken(in.readLine());
			in.readLine(); // TAG:RIGHT:CLOSE
			
			QueryTokens.Condition token = new QueryTokens.Condition(getAttribute(tag,"append"),left,getAttribute(tag,"operator"),right);
			in.readLine(); // TAG:CONDITION:CLOSE
			
			return token;
		}
		
		private QueryTokens.Join getJoin(String tag)
			throws IOException
		{
			in.readLine(); // TAG:CONDITION:OPEN
			QueryTokens.Condition condition = getCondition(tag);
			
			int t = Integer.valueOf(getAttribute(tag,"type")).intValue();
			QueryTokens.Join token = new QueryTokens.Join(t,(QueryTokens.Column)condition.getLeft(), condition.getOperator(), (QueryTokens.Column)condition.getRight());
			in.readLine(); // TAG:JOIN:CLOSE
			
			return token;
		}
	}

//	---------------------------------------------------------------	
//	writer class
//	---------------------------------------------------------------
	private static class Writer
	{
		private DiagramLayout layout;
		private BufferedWriter out;
				
		private Writer(String filename, DiagramLayout layout)
			throws IOException
		{
			this.layout = layout;
			out = new BufferedWriter(new FileWriter(filename));		
			
			writeln("<" + Application.PROGRAM.toUpperCase() + toAttribute("version",Application.getVersion()) + ">");
			writeln("<MODEL" + toAttribute("schema",layout.getQueryModel().getSchema()) + ">");
			
			write(layout.getQueryModel().getQueryExpression());
			write(layout.getQueryModel().getOrderByClause(), _ReservedWords.ORDER_BY.replace(' ','_'));
			
			writeln("</MODEL>");
			writeln("</" + Application.PROGRAM.toUpperCase() + ">");
			
			out.flush();
			out.close();
		}
		
		private void write(QueryExpression qe)
			throws IOException
		{
			if(qe==null) return;
			
			writeln("<QUERY>");
			
			DiagramLayout.EntityExtra[] extras = layout.getExtras(qe.getQuerySpecification());
			if(extras!=null && extras.length > 0)
			{
				writeln("<LAYOUT>");
				for(int i=0; i<extras.length; i++)
				{
					Integer x = new Integer(extras[i].getLocation().x);
					Integer y = new Integer(extras[i].getLocation().y);
					writeln("<ENTITY" + toAttribute("reference",extras[i].getReference()) + toAttribute("x",x)  + toAttribute("y",y) + toAttribute("pack",new Boolean(extras[i].isPack())) + "/>");
				}
				writeln("</LAYOUT>");
			}
			else
			{
				writeln("<LAYOUT/>");
			}
			
			write(qe.getQuerySpecification().getSelectList()	, _ReservedWords.SELECT);
			write(qe.getQuerySpecification().getFromClause()	, _ReservedWords.FROM);
			write(qe.getQuerySpecification().getWhereClause()	, _ReservedWords.WHERE);
			write(qe.getQuerySpecification().getGroupByClause()	, _ReservedWords.GROUP_BY.replace(' ','_'));
			write(qe.getQuerySpecification().getHavingClause()	, _ReservedWords.HAVING);
			write(qe.getUnion());
			
			writeln("</QUERY>");
		}
		
		private void write(Object[] tokens, String tag)
			throws IOException
		{
			if(tokens.length == 0)
			{
				writeln("<" + tag + "/>");
				return;
			}
			
			writeln("<" + tag + ">");
			for(int i=0; i<tokens.length; i++)
			{
				if(tokens[i] instanceof QueryTokens._Expression)
					write((QueryTokens._Expression)tokens[i]);
				else if(tokens[i] instanceof QueryTokens.Join)
					write((QueryTokens.Join)tokens[i]);
				else if(tokens[i] instanceof QueryTokens.Sort)
					write((QueryTokens.Sort)tokens[i]);
				else if(tokens[i] instanceof QueryTokens.Group)
					write((QueryTokens.Group)tokens[i]);
				else if(tokens[i] instanceof QueryTokens.Table)
					write((QueryTokens.Table)tokens[i]);
				else if(tokens[i] instanceof QueryTokens.Condition)
					write((QueryTokens.Condition)tokens[i]);
			}
			writeln("</" + tag + ">");
		}
		
		private void write(QueryTokens._Expression token)
			throws IOException
		{
			if(token instanceof SubQuery)
				write((QueryExpression)token);
			else if(token instanceof QueryTokens.Column)
				write((QueryTokens.Column)token);
			else if(token!=null)
				writeln("<EXPRESSION" + toAttribute("value",token.toString()) + "/>");
			else
				writeln("<EXPRESSION/>");
		}
		
		private void write(QueryTokens.Condition token)
			throws IOException
		{
			writeln("<CONDITION" + toAttribute("append",token.getAppend()) + toAttribute("operator",token.getOperator()) + ">");
			writeln("<LEFT>");
			write(token.getLeft());
			writeln("</LEFT>");
			writeln("<RIGHT>");
			write(token.getRight());
			writeln("</RIGHT>");
			writeln("</CONDITION>");
		}
		
		private void write(QueryTokens.Join token)
			throws IOException
		{
			writeln("<JOIN" + toAttribute("type",new Integer(token.getType())) + ">");
			write(token.getCondition());
			writeln("</JOIN>");
		}
	
		private void write(QueryTokens.Column token)
			throws IOException
		{
			writeln("<COLUMN" + toAttribute("name",token.getName()) + toAttribute("alias",token.getAlias()) + ">");
			write(token.getTable());
			writeln("</COLUMN>");
		}
		
		private void write(QueryTokens.Table token)
			throws IOException
		{
			writeln("<TABLE" + toAttribute("schema",token.getSchema()) + toAttribute("name",token.getName()) + toAttribute("alias",token.getAlias()) + "/>");
		}
	
		private void write(QueryTokens.Group token)
			throws IOException
		{
			write(token.getExpression());
		}
		
		private void write(QueryTokens.Sort token)
			throws IOException
		{
			writeln("<SORT" + toAttribute("type",new Short(token.isAscending() ? QueryTokens.Sort.ASCENDING : QueryTokens.Sort.DESCENDING)) + ">");
			write(token.getExpression());
			writeln("</SORT>");
		}
		
		private void writeln(String s)
			throws IOException
		{
			out.write(s + '\n');
		}
	}
	
//	---------------------------------------------------------------	
//	utils
//	---------------------------------------------------------------
	private static String getAttribute(String t,String a)
	{
		a = " " + a + "=\"";
			
		int start = t.indexOf(a) + a.length();
		int end = t.indexOf("\"",start);
		if (start!=-1 && end!=-1 && start < end){	
			t = t.substring(start,end);
		}else{
			return null;
		}
		return t.equals("null") ? null : Text.replaceText(t,"&apos;","'");
	}

	private static String toAttribute(String a, Object o)
	{
		return " " + a + "=\"" + (o==null ? null : Text.replaceText(o.toString(),"'","&apos;")) + "\"";
	}
}