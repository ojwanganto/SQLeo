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

package com.sqleo.querybuilder.syntax;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.mdi.ClientQueryBuilder;
import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.environment.mdi.MDIClient;
import com.sqleo.querybuilder.DiagramLayout.EntityExtra;
import com.sqleo.querybuilder.DiagramAbstractEntity;
import com.sqleo.querybuilder.QueryBuilder;


public class SQLFormatter implements _ReservedWords
{
	public static final char BREAK	= '\n';
	public static final char COMMA	= ',';
	public static final char DOT	= '.';
	public static final char SPACE	= ' ';
	
	//private static final String INDENT	= "     ";
	private static final String INDENT	= String.valueOf('\t');
	
	public static String concatCommaDelimited(Object tokens[], boolean wrap, int offset)
	{
		if(tokens.length == 0) return "<empty>";
		
		String delimiter = String.valueOf(COMMA) + (wrap ? String.valueOf(BREAK) : String.valueOf(SPACE));
		StringBuffer buffer = new StringBuffer();
		
		for(int i=0; i<tokens.length; i++)
		{
			if(wrap) {
				buffer.append(INDENT);
				indent(offset, buffer, INDENT);
			}
			if(tokens[i] instanceof SubQuery){
				if(tokens[i] instanceof DerivedTable){
					final DerivedTable derivedTable = (DerivedTable) tokens[i];
					buffer.append(derivedTable.toString(wrap, offset+1));
				}else{
					final SubQuery subQuery  = (SubQuery) tokens[i];
					buffer.append(subQuery.toString(wrap, offset+1));
				}
				buffer.append(delimiter);
			}else{
				buffer.append(tokens[i] + delimiter);
			}
		}
		
		return buffer.substring(0,buffer.length()-delimiter.length());
	}

	public static String concat(QueryTokens._Expression tokens[], boolean wrap, int offset)
	{
		return concatCommaDelimited(tokens,wrap,offset);
	}
	
	public static String concat(QueryTokens.Group tokens[], boolean wrap, int offset)
	{
		return concatCommaDelimited(tokens,wrap, offset);
	}
	
	public static String concat(QueryTokens.Sort tokens[], boolean wrap, int offset)
	{
		return concatCommaDelimited(tokens,wrap, offset);
	}
	
	public static String concat(QueryTokens.Condition tokens[], boolean wrap, int offset)
	{
		if(tokens.length == 0) return "<empty>";
		
		String delimiter = (wrap ? String.valueOf(BREAK) : String.valueOf(SPACE));
		StringBuffer buffer = new StringBuffer();
		
		for(int i=0; i<tokens.length; i++)
		{
			if(wrap) {				
				buffer.append(INDENT);
				indent(offset, buffer, INDENT);
			}
			buffer.append(tokens[i].toString(wrap, offset));
			buffer.append(delimiter);
		}
		
		return buffer.substring(0,buffer.length()-delimiter.length());
	}

	public static String concat(QueryTokens._TableReference tokens[], boolean wrap, int offset,QuerySpecification qs)
	{
		if(tokens.length == 0) return "<empty>";
		
		String delimiter = (wrap ? String.valueOf(BREAK) + INDENT + indent(offset) : String.valueOf(SPACE));
		StringBuffer buffer = new StringBuffer();
		
		sort(tokens);

		Hashtable<String,DerivedTable> subs = new Hashtable<String,DerivedTable>();
		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i] instanceof DerivedTable)
			{
				DerivedTable dt = (DerivedTable)tokens[i];
				if(!subs.containsKey(dt.getAlias()))
					subs.put(dt.getAlias(),dt);
			}

		}
		
		ArrayList declared = new ArrayList();
		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i] instanceof QueryTokens.Join)
			{
				QueryTokens.Join token = (QueryTokens.Join)tokens[i];
				
				QueryTokens.Column cL = token.getPrimary();
				QueryTokens.Column cR = token.getForeign();
				
				boolean bLeft = declared.indexOf(cL.getTable().toString())!=-1;
				boolean bRight = declared.indexOf(cR.getTable().toString())!=-1;
				
				if(!bLeft) declared.add(cL.getTable().toString());
				if(!bRight) declared.add(cR.getTable().toString());
				
				if(!bLeft && !bRight)
				{
					if(buffer.length() > 0)
						buffer.insert(buffer.toString().lastIndexOf(delimiter),COMMA);
					else if(wrap){
						buffer.append(INDENT);
						indent(offset, buffer, INDENT);
					}

					String left = cL.getTable().toString();
					if(cL.getTable().getName() == null && subs.containsKey(cL.getTable().getAlias())) left = subs.get(cL.getTable().getAlias()).toString(wrap,offset+1);
					
					String right = cR.getTable().toString();
					if(cR.getTable().getName() == null && subs.containsKey(cR.getTable().getAlias())) right = subs.get(cR.getTable().getAlias()).toString(wrap,offset+4);					
					token.getCondition().setAppend(null);
					buffer.append(left + posToAttribute("3",qs, cL.getTable()) + delimiter + token.getTypeName() + SPACE + right + posToAttribute("4", qs,cR.getTable()) + delimiter + SPACE + _ReservedWords.ON + SPACE + token.getCondition() + delimiter);
				}
				else if(bLeft && bRight)
				{
					buffer.append(SPACE + (token.getCondition().getAppend()!=null ? "" : _ReservedWords.AND )+ SPACE + token.getCondition().toString() + delimiter);
				}
				else
				{
					if(!bLeft)
					{
						if(token.getType() == QueryTokens.Join.LEFT_OUTER)
							token.setType(QueryTokens.Join.RIGHT_OUTER);
						else if(token.getType() == QueryTokens.Join.RIGHT_OUTER)
							token.setType(QueryTokens.Join.LEFT_OUTER);
						
						token.getCondition().setLeft(cR);
						token.getCondition().setRight(cL);
						
						cR = cL;
					}
					
					String right = cR.getTable().toString();
					if(cR.getTable().getName() == null && subs.containsKey(cR.getTable().getAlias())) right = subs.get(cR.getTable().getAlias()).toString(wrap,offset+4);

					// ticket #209 "ON AND" after move up or delete table
					token.getCondition().setAppend(null);  
					buffer.append(token.getTypeName() + SPACE + right + posToAttribute("5", qs, cR.getTable()) + delimiter + SPACE + _ReservedWords.ON + SPACE + token.getCondition() + delimiter);
				}
			}
		}

		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i] instanceof DerivedTable && declared.indexOf(((DerivedTable)tokens[i]).getAlias())!=-1) continue;
			
			if(!(tokens[i] instanceof QueryTokens.Join))
			{
				if(buffer.length() > 0)
					buffer.insert(buffer.toString().lastIndexOf(delimiter),COMMA);
				else if(wrap){
					buffer.append(INDENT);
					indent(offset, buffer, INDENT);
				}
				if(tokens[i] instanceof SubQuery){
					if(tokens[i] instanceof DerivedTable){
						final DerivedTable derivedTable = (DerivedTable) tokens[i];
						buffer.append(derivedTable.toString(wrap, offset+1));
					}else{
						final SubQuery subQuery  = (SubQuery) tokens[i];
						buffer.append(subQuery.toString(wrap, offset+1));
					}
					buffer.append(posToAttribute("6", qs, tokens[i]) + delimiter);

				}else{
					buffer.append(tokens[i] + posToAttribute("7", qs, tokens[i]) + delimiter);
				}
			}
		}
		
		return buffer.length() > 0 ? buffer.substring(0,buffer.length()-delimiter.length()) : "<empty>";
	}
	
	public static String indent(int n){
		final StringBuffer buffer = new StringBuffer();
		indent(n, buffer, INDENT);
		return buffer.toString();
	}
	
	private static void indent(int n,final StringBuffer buffer,String indentString){
		for(int i=0;i<n;i++){
			buffer.append(indentString);
		}
	}
	
	private static void sort(QueryTokens._TableReference tokens[])
	{
		for(int i=0; i<tokens.length-1; i++)
		{
			if(tokens[i] instanceof QueryTokens.Join)
			{
				QueryTokens.Join joinI = (QueryTokens.Join)tokens[i];
				for(int j=i+1,k=i; j<tokens.length; j++)
				{
					if(tokens[j] instanceof QueryTokens.Join)
					{
						QueryTokens.Join joinJ = (QueryTokens.Join)tokens[j];
						
						if((joinI.getPrimary().getTable().getReference().equals(joinJ.getPrimary().getTable().getReference())
						&& joinI.getForeign().getTable().getReference().equals(joinJ.getForeign().getTable().getReference()))
						|| (joinI.getPrimary().getTable().getReference().equals(joinJ.getForeign().getTable().getReference())
						&& joinI.getForeign().getTable().getReference().equals(joinJ.getPrimary().getTable().getReference())))
						{
							moveUp(tokens,j,i+1);
							k++;
						}
						else if(joinI.getPrimary().getTable().getReference().equals(joinJ.getPrimary().getTable().getReference())
						|| joinI.getForeign().getTable().getReference().equals(joinJ.getForeign().getTable().getReference())
						|| joinI.getPrimary().getTable().getReference().equals(joinJ.getForeign().getTable().getReference())
						|| joinI.getForeign().getTable().getReference().equals(joinJ.getPrimary().getTable().getReference()))
						{
							moveUp(tokens,j,++k);
						}
					}
				}				
			}
		}
	}

	private static void moveUp(Object tokens[], int idxOld, int idxNew)
	{
		Object token = tokens[idxOld];
		for(int i=idxOld-1; i>=idxNew; i--)
		{
			tokens[i+1] = tokens[i];
		}
		tokens[idxNew] = token;
	}
	
	public static String stripQuote(String s)
	{
		if(s.startsWith(QueryBuilder.identifierQuoteString)) s = s.substring(1);
		if(s.endsWith(QueryBuilder.identifierQuoteString)) s = s.substring(0,s.length()-1);
		
		for(int i=s.indexOf(QueryBuilder.identifierQuoteString); i!=-1; i=s.indexOf(QueryBuilder.identifierQuoteString))
		{
			String l = s.substring(0,i);
			String r = s.substring(i+1);
			
			s = l + r;
		}
		return s;
	}
	
	public static String ensureQuotes(String identifier, boolean asNeeded)
	{
		return ensureQuotes(identifier, QueryBuilder.identifierQuoteString ,asNeeded);
	}
	
	public static String ensureQuotes(String identifier, String quoteString, boolean asNeeded)
	{
		if(identifier == null || quoteString.equals(String.valueOf(SPACE)))
			return identifier;
		
		/* clean quote chars */
		if(identifier.startsWith(quoteString)) identifier = identifier.substring(quoteString.length());
		if(identifier.endsWith(quoteString)) identifier = identifier.substring(0,identifier.length() - quoteString.length());
		for(int i; (i=identifier.indexOf(quoteString)) !=-1;)
		{
			String left = identifier.substring(0,i);
			String right = identifier.substring(i+1);
			
			identifier = left + right;
		}
		
		/* put quote chars */
		StringBuffer quoted = new StringBuffer(identifier);
		if(asNeeded)
		{
			int iQuote = 0;
			int iSpace = 0;
			while( (iSpace = quoted.toString().indexOf(SPACE,iQuote)) != -1 )
			{
				int iDot = quoted.toString().indexOf(DOT,iQuote);
				if(iDot == -1)
				{
					quoted.insert(iQuote,quoteString);
					quoted.append(quoteString);
					break;
				}
				else if(iSpace < iDot)
				{
					quoted.insert(iQuote,quoteString);
					quoted.insert(++iDot,quoteString);
					iQuote = iDot+2;
				}
				else
				{
					iQuote = iDot+1;
				}
			}
		}
		else
		{
			quoted.insert(0,quoteString);
			quoted.append(quoteString);
		
			if(identifier.indexOf(DOT)!=-1)
			{
				int point = identifier.indexOf(DOT);
				quoted.insert(point+1,quoteString);
				quoted.insert(point+3,quoteString);
			}
		}
		
		return quoted.toString();
	}
	
	public static String toJdbcValue(Object value, int sqltype)
	{
		if(value==null) return "null";
		
		switch(sqltype)
		{
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.LONGNVARCHAR:
		case Types.NCHAR:
		case Types.NVARCHAR:
			return "'" + value.toString() + "'";
		case Types.DATE:
			return "{d '" + value.toString() + "'}";
		case Types.TIME:
			return "{t '" + value.toString() + "'}";
		case Types.TIMESTAMP:
			return "{ts '" + value.toString() + "'}";
		default:
			return value.toString();
		}
	}

	private static String posToAttribute(String a,  QuerySpecification qs, QueryTokens._TableReference token)
	{
//		System.out.println(a+"formatter-here");
		if(qs!=null && Preferences.savePosInSQL()){
			final String reference;
			if(token instanceof SubQuery)
			{
				final SubQuery subQuery = (SubQuery)token;
				reference = subQuery.getAlias();
			}else{
				reference = token.toString();
			}
			final MDIClient[] clients =
				Application.window.getClientsOfConnection(ClientQueryBuilder.DEFAULT_TITLE, null);
			if (clients.length >= 1) {
				final ClientQueryBuilder ce = (ClientQueryBuilder) clients[0];
				final HashMap extrasMap = ce.getQueryBuilder().getQueryModel().getExtrasMap();
				if(extrasMap!=null)
				{
					EntityExtra[] extras = (EntityExtra[]) extrasMap.get(qs);
					if(extras!=null && extras.length > 0)
					{
						for(int i=0; i<extras.length; i++)
						{
							if(reference.equals(extras[i].getReference())){
								final Integer x = new Integer(extras[i].getLocation().x);
								final Integer y = new Integer(extras[i].getLocation().y);
								final String s = " /*SQLeo("+x+"_"+y+"_"+new Boolean(extras[i].isPack())+")*/ ";
//								System.out.println(s);
								return s;
							}
						}
					}
				}
			}
		}
		return "";
	}

}