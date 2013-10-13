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

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;

import com.sqleo.environment.Application;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.QueryModel;


public class SQLParser
{
	public static Hashtable cte;

	public static QueryModel toQueryModel(String sql)
		throws IOException
	{
		// ticket #83 remove -- comment
		sql = sql.replace("--","//");

		// ticket #73 oracle (+) join support
		sql = sql.replace("(+)"," ORACLE_OUTER_JOIN");


		return toQueryModel(new StringReader(sql));
	}

	private static QueryModel toQueryModel(Reader r)
		throws IOException
	{
		QueryModel qm = new QueryModel();
		
		ArrayList al = doTokenize(r);
		doAdjustSequence(al);
		
		ListIterator li = al.listIterator();
		doParseQuery(li,qm.getQueryExpression());
		
		if(li.hasNext() && li.next().toString().toUpperCase().equalsIgnoreCase(_ReservedWords.ORDER_BY))
			doParseOrderBy(li,qm);
		
		return qm;
	}

	private static void doParseQuery(ListIterator li, QueryExpression qe)
		throws IOException
	{

		while(li.hasNext())
		{
			Object next = li.next();

			if(next.toString().equalsIgnoreCase(_ReservedWords.WITH))
			{
				doParseCTE(li,qe.getQuerySpecification());
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.SELECT))
			{
				doParseSelect(li,qe.getQuerySpecification());
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.FROM))
			{
				doParseFrom(li,qe.getQuerySpecification());
				doConvertColumns(qe.getQuerySpecification());
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.WHERE))
			{
				QueryTokens.Condition[] tokens = doParseConditions(li);
				for(int i=0; i<tokens.length; i++)
					qe.getQuerySpecification().addWhereClause(tokens[i]);
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.GROUP_BY))
			{
				doParseGroupBy(li,qe.getQuerySpecification());
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.HAVING))
			{
				QueryTokens.Condition[] tokens = doParseConditions(li);
				for(int i=0; i<tokens.length; i++)
					qe.getQuerySpecification().addHavingClause(tokens[i]);
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.UNION))
			{
				QueryExpression union = new QueryExpression();
				doParseQuery(li,union);
				qe.setUnion(union);
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.ORDER_BY))
			{
				li.previous();
				break;
			}
			else if(next.toString().equals(")"))
			{
				li.previous(); // ticket #80
				break;
			}
		}
	}
	

	private static void doParseCTE(ListIterator li, QuerySpecification qs)
		throws IOException
	{
		String alias = null;
		DerivedTable dt = null;
		cte = new Hashtable(); 

		while(li.hasNext())
		{
			Object next = li.next();
			
			if(next.toString().equalsIgnoreCase(_ReservedWords.SELECT))
			{
				li.previous();
				if(alias!=null)
				{
					dt = new DerivedTable();
					doParseQuery(li,dt);
					dt.setAlias(alias);
					cte.put(SQLFormatter.stripQuote(dt.getAlias()),dt);
					alias=null;
				}
				else
				{
					// let doParseQuery do the work
					break;
				}
			}
			else if(next.toString().equalsIgnoreCase("(") || next.toString().equalsIgnoreCase(")") || next.toString().equalsIgnoreCase(",") || next.toString().equalsIgnoreCase("AS"))
			{
				// do nothing
			}
			else
			{
				alias=next.toString();
			}
		}
				
	}

	private static void doParseSelect(ListIterator li, QuerySpecification qs)
		throws IOException
	{
		int surrounds = 0;
		String alias = null;
		String value = new String();
		SubQuery sub=null;
		DerivedTable dt = null;
		boolean seenSubquery = false;
		
		while(li.hasNext())
		{
			Object next = li.next();
			
			if(next.toString().equalsIgnoreCase(_ReservedWords.DISTINCT) && surrounds == 0 )
			{
				qs.setQuantifier(QuerySpecification.DISTINCT);
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.EXTRACT))
			{
				Object n = li.next();
                if(n.toString().equals("("))
                {
                	Extract e = new Extract();
                	qs.addSelectList(e);
                	doParseExtract(li,e);
                }
                else { value=next.toString(); li.previous(); }
			}
			else if(next.toString().equals(",") && surrounds == 0 )
			{
				if(!value.trim().equals("")) qs.addSelectList(new QueryTokens.DefaultExpression(value.trim(),alias));
				value = new String();
				alias = null; // added to fix bug #13
				seenSubquery = false;
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.ORDER_BY) )
			{
				// ticket #102: Specific for Oracle syntax 
				// ROW_NUMBER() OVER (PARTITION BY ... ORDER BY ... ASC) as Y
				value = value + " Order by";
			}
			else if(isClauseWord(next.toString()))
			{
				li.previous();
				// for scalar subqueries
				if(next.toString().equalsIgnoreCase(_ReservedWords.WITH))
				{
					//to to ticket #138
					//sub = new SubQuery();
					//doParseQuery(li,sub);
					//qs.addSelectList(sub);

					//value = new String();

				}
				if(next.toString().equalsIgnoreCase(_ReservedWords.SELECT))
				{
					sub = new SubQuery();
					doParseQuery(li,sub);
					qs.addSelectList(sub);
					value = new String();

				}
				else 
				{
					if(!value.trim().equals("")) qs.addSelectList(new QueryTokens.DefaultExpression(value.trim(),alias));
					break;
				}
			}
			else if(next.toString().equals(")") && value.trim().equals("")) // for last ")" after subquery
			{
				surrounds--;
				seenSubquery = true;
			}
			// ticket #87 for cast (x as ...) 
			// else if(next.toString().equalsIgnoreCase("AS") && li.hasNext())
			else if(next.toString().equalsIgnoreCase("AS") && li.hasNext() && surrounds == 0)
			{
				alias = li.next().toString().trim();
				if (seenSubquery && sub!=null) {sub.setAlias(alias); sub.setAs(true);}
			}
			else
			{
				if(next.toString().equals(")")) surrounds--;
				if(next.toString().equals("(")) surrounds++;

				
				if(value.length()>0)
					if(next instanceof String || next instanceof Character || next instanceof Integer)
				{
					// ticket #54
					// char last = value.charAt(value.length()-1);
					// if(Character.isLetter(last) || String.valueOf(last).equals(QueryBuilder.identifierQuoteString))
					value = value + SQLFormatter.SPACE;
				}
				value = value + next.toString().trim();
				
				 // [MSE] Sometimes the token stream returns not comma in a single token, it returns
	            // a value with an appended comma. This is the fix for this case.
	            if (value.endsWith(",") && surrounds == 0)
	            {
	            	qs.addSelectList(new QueryTokens.DefaultExpression(value.substring(0,value.length()-1)));
	            	value = new String();
	            }
	            if (seenSubquery && !value.equals(""))
	            {
	            	sub.setAlias(value);
	            	sub.setAs(false);
	            	value = new String();
	            }
			}
		}
	}

	private static void doParseGroupBy(ListIterator li, QuerySpecification qs)
		throws IOException
	{
		int surrounds = 0;
		String value = new String();
		
		while(li.hasNext())
		{
			Object next = li.next();
			
			if(next.toString().equals(",") && surrounds == 0 || next.toString().equals(";"))
			{
				qs.addGroupByClause(new QueryTokens.Group(value.trim()));
				value = new String();
			}
			else if(isClauseWord(next.toString()))
			{
				li.previous();
				if(!value.trim().equals("")) qs.addGroupByClause(new QueryTokens.Group(value.trim()));
				break;
			}
			// added for ticket #80
			else if(next.toString().equals(")") && surrounds == 0)
			{
				li.previous();
				if(!value.trim().equals("")) qs.addGroupByClause(new QueryTokens.Group(value.trim()));
				break;
			}
			// ticket #80 end
			else if(next.toString().equalsIgnoreCase(_ReservedWords.EXTRACT))
			{
				Object n = li.next();
				if(n.toString().equals("("))
				{
					Extract e = new Extract();
					qs.addGroupByClause(new QueryTokens.Group(e));
					doParseExtract(li,e);
				}
				else { value = value + next.toString().trim(); li.previous(); }
			}
			else
			{
				if(next.toString().equals("(")) surrounds++;
				if(next.toString().equals(")")) surrounds--;
				
				if(value.length()>0 && next instanceof String)
				{
					char last = value.charAt(value.length()-1);
					if(Character.isLetter(last) || String.valueOf(last).equals(QueryBuilder.identifierQuoteString))
						value = value + SQLFormatter.SPACE;
				}
				value = value + next.toString().trim();
			}
		}
	}


	private static void doParseOrderBy(ListIterator li, QueryModel qm)
	throws IOException
	{
		int surrounds = 0;
		boolean isDescendingPrevious = false;
		String value = new String();

		while(li.hasNext())
		{
			Object next = li.next();

			if(next.toString().equals(",") && surrounds == 0 || next.toString().equals(";"))
			{
				QueryTokens.Sort token = new QueryTokens.Sort(new QueryTokens.DefaultExpression(value.trim()));
				if(isDescendingPrevious){
					token.setType(QueryTokens.Sort.DESCENDING);
					isDescendingPrevious = false;
				}
				qm.addOrderByClause(token);
				value = new String();

			}
			else if(isClauseWord(next.toString()))
			{
				li.previous();
				if(!value.trim().equals("")) qm.addOrderByClause(new QueryTokens.Sort(new QueryTokens.DefaultExpression(value.trim())));
				break;
			}
			else if(next.toString().equalsIgnoreCase("DESC"))
			{
				isDescendingPrevious = true;
			}
			else if(next.toString().equalsIgnoreCase("ASC"))
			{
				// nothing to do
			}
			else
			{
				if(next.toString().equals("(")) surrounds++;
				if(next.toString().equals(")")) surrounds--;


				if(value.length()>0 && next instanceof String)
				{
					char last = value.charAt(value.length()-1);
					if(Character.isLetter(last) || String.valueOf(last).equals(QueryBuilder.identifierQuoteString))
						value = value + SQLFormatter.SPACE;
				}
				value = value + next.toString().trim();

			}
		}
	}


	private static void doParseFrom(ListIterator li, QuerySpecification qs)
		throws IOException
	{
		int joinType = -1;
		QueryTokens.Table t = null;
		DerivedTable dt = null;
		Hashtable tables = new Hashtable();
		
		for(int surrounds = 0; li.hasNext();)
		{
			String next = li.next().toString();

			// ticket #80 Derived tables 
			// ticket #135 add CTE support in derived tables
			if(next.toString().equalsIgnoreCase(_ReservedWords.SELECT)||next.toString().equalsIgnoreCase(_ReservedWords.WITH))
			{
//				System.out.println("Derived Table");
				dt = new DerivedTable();
				li.previous();
				doParseQuery(li,dt);
				qs.addFromClause(dt);		
			}
			// end #80
			else if(isClauseWord(next) || next.equals(";") )
			{
//				System.out.println("end.");
				
				if(t!=null) qs.addFromClause(t);
				
				li.previous();
				break;
			}
			else if(next.equals(","))
			{
//				System.out.println("cross");
				
				if(t!=null) qs.addFromClause(t);
				t=null;
				dt=null;
			}
			else if(isJoinWord(next))
			{
//				System.out.println("join");

				if(t!=null) 
				{
					tables.put(SQLFormatter.stripQuote(t.getReference()),t);
					t=null;
				}

				if(dt!=null) // #80
				{
					tables.put(SQLFormatter.stripQuote(dt.getAlias()),dt);
					dt=null;
				}
				
				joinType = QueryTokens.Join.getTypeInt(next);
			}
			else if(next.toString().equalsIgnoreCase(_ReservedWords.ON)
			|| next.toString().equalsIgnoreCase(_ReservedWords.AND)
			|| next.toString().equalsIgnoreCase(_ReservedWords.OR))
			{
//				System.out.println("condition");

				if(t!=null) tables.put(SQLFormatter.stripQuote(t.getReference()),t);
				t=null;
				
				/* is AND/OR, then use previous/last type */
				if(joinType == -1)
				{
					QueryTokens._TableReference[] ref = qs.getFromClause();
					if(ref.length>0 && ref[ref.length-1] instanceof QueryTokens.Join)
						joinType = ((QueryTokens.Join)ref[ref.length-1]).getType();
				}
				
				String left = li.next().toString();
				while(left.equals("("))
				{
					surrounds++;
					left = li.next().toString();
				}
				String op = li.next().toString();
				String right = li.next().toString();
				
				QueryTokens.Column tcl = null;
				QueryTokens.Column tcr = null;
				
				for(int side=0; side<2; side++)
				{
					String e = side==0 ? left : right;
					e = SQLFormatter.stripQuote(e);

					int dot = e.lastIndexOf(SQLFormatter.DOT);
					String ref = dot==-1 ? new String() : e.substring(0,dot);

					// to do fix #80 for derived tables
					QueryTokens.Table tr = new QueryTokens.Table(null,ref);

					if(tables.containsKey(ref))
					{
						if (tables.get(ref) instanceof QueryTokens.Table)
						{
							tr = (QueryTokens.Table)tables.get(ref);
						}
						else
						{
							// to do Derived Table
							Application.alert("!!! join with derived table for: " + ref + " !!!");
						}
					}
					// fix #92 
					else
					{	
						// let contaisKey case sensitive but raise exception if Alias or Table not fount
						// to do raise exception
						Application.alert("!!! condition table or alias not found: " + ref + " !!!");

					} // end #92

					if(side==0)
						tcl = new QueryTokens.Column(tr,e.substring(dot+1));

					else
						tcr = new QueryTokens.Column(tr,e.substring(dot+1));
				}
		 				
				qs.addFromClause(new QueryTokens.Join(joinType,tcl,op,tcr));
				joinType = -1;
			}
			else if(next.toString().equals("("))
			{
				surrounds++;
			}
			else if(next.toString().equals(")"))
			{
				// <bug=1914170>
				if(t!=null) qs.addFromClause(t);
				t=null;				
				// </bug>
				
				if(--surrounds < 0)
				{
					li.previous();
					break;
				}

			}
			else if(!next.toString().equalsIgnoreCase("AS"))
			{
//				System.out.println("table or alias");

				String schema = null;
				String name = SQLFormatter.stripQuote(next.toString());
					
				int i = name.lastIndexOf(SQLFormatter.DOT);
				if(i>0) {schema = name.substring(0,i); name = name.substring(i+1); }

				if(dt==null) // added for #80
				{
					if(t==null) 
						// CTE fix #99, make cte visible from there
						 if (cte !=null && cte.containsKey(next.toString()))
							qs.addFromClause((DerivedTable)cte.get(next.toString()));
						 else
							t = new QueryTokens.Table(schema,name);
					else
							t.setAlias(next.toString()); 
				}
				else
					dt.setAlias(next.toString()); 
				
			}
		}
	}

	private static QueryTokens.Condition[] doParseConditions(ListIterator li)
		throws IOException
	{
		boolean between = false;
		ArrayList tokens = new ArrayList();
		QueryTokens.Condition token = null;
		QueryTokens._Expression expr = null;
		String value;
		
		for(int surrounds = 0; li.hasNext();)
		{
			Object next = li.next();
			
			if(next.toString().equals("(")) surrounds++;
			if(next.toString().equals(")")) surrounds--;
			
			if(next.toString().equalsIgnoreCase("EXISTS") || next.toString().equalsIgnoreCase("NOT EXISTS"))
			{
				if(token == null) token = new QueryTokens.Condition(); // Added for ticket #93

				SubQuery sub = new SubQuery();
				doParseQuery(li,sub);
				
				token.setLeft(null);
				token.setOperator(next.toString().toUpperCase());
				token.setRight(sub);
				
				tokens.add(token);
				
				token = null;
				expr = null;

				surrounds++; // Added for ticket #93


			}
			if(next.toString().equalsIgnoreCase(_ReservedWords.EXTRACT))
			{
				Object n = li.next();
				if(n.toString().equalsIgnoreCase("("))
				{
					Extract e = new Extract();
					doParseExtract(li,e);
					expr = e;
				}
				else { expr = new QueryTokens.DefaultExpression(next.toString()); li.previous(); };
			}
			else if(isClauseWord(next.toString()))
			{
				li.previous();
				if(next.toString().equalsIgnoreCase(_ReservedWords.SELECT))
				{
					expr = new SubQuery();
					doParseQuery(li,(SubQuery)expr);
					
					// bug reverse IN (subquery)
					token.setRight(expr);
					tokens.add(token);
					token = null;
					expr = null;
					// end bug reverse IN (subquery)
				}
				else
				{
					if(token!=null)
					{
						token.setRight(expr);
						tokens.add(token);
					}
					break;
				}
			}
			else if(isOperator(next.toString()))
			{
				if(token == null) token = new QueryTokens.Condition();
				
				token.setLeft(expr);
				token.setOperator(next.toString().toUpperCase());
				
				between = token.getOperator().indexOf(_ReservedWords.BETWEEN) != -1;
				expr = null;
			}
			else if((next.toString().equalsIgnoreCase(_ReservedWords.AND) && !between)
			|| next.toString().equalsIgnoreCase(_ReservedWords.OR)
			|| next.toString().equals(";") || surrounds < 0)
			{
				if(token!=null)
				{
					token.setRight(expr);
					tokens.add(token);
				}
				
				token = new QueryTokens.Condition();
				token.setAppend(next.toString());
				
				expr = null;
			}
			// ticket #73 oracle (+) join support
			else if(next.toString().equals("ORACLE_OUTER_JOIN"))
			{
				// to do Change JOIN TYPE !
				// raise exception 
				Application.alert("!!!" + expr + "(+) changed to INNER join !!!");
			}
			// end #73
			else
			{
				value = expr == null ? new String() : expr.toString();
				
				if(value.length()>0 && (next instanceof String || next instanceof Number))
				{
					char last = value.charAt(value.length()-1);
					if(Character.isLetterOrDigit(last)) value = value + SQLFormatter.SPACE;
				}
				value = value + next.toString();
				expr = new QueryTokens.DefaultExpression(value);
				
				if(between && next.toString().equalsIgnoreCase(_ReservedWords.AND)) between = false;
			}
			
			if(surrounds < 0)
			{
				li.previous();
				break;
			}
		}
		
		return (QueryTokens.Condition[])tokens.toArray(new QueryTokens.Condition[tokens.size()]);
	}

	private static void doParseExtract(ListIterator li, Extract e)
		throws IOException
	{
		String value = new String();
		while(li.hasNext())
		{
			Object next = li.next();
			if(next.toString().equalsIgnoreCase(_ReservedWords.FROM))
			{
				char surround = 1;
				String expr = new String();
				while(li.hasNext())
				{
					next = li.next();
					if(next.toString().equals(")") )
						surround--;
					if(next.toString().equals("(") ) 
						surround++;
					if(surround==0)
					{
						Object n = li.next();
						if(n.toString().equals(",") || n.toString().equals(";"))
						{
							e.setExtract(value,expr,null);
						}
						else if(isClauseWord(n.toString()) || isOperatorSimbol(n.toString())) 
						{
							e.setExtract(value,expr,null);
							li.previous();
						}
						else 
						{
							e.setExtract(value,expr,n.toString());
						}
						break;
					}
					expr = expr + (expr.length()>0?SQLFormatter.SPACE:expr) + next;
				}
				break;
			}
			value = value + next;
		}
	}
	
	public static void doConvertColumns(QuerySpecification qs)
		throws IOException
	{
		Hashtable tables = new Hashtable();
		
		QueryTokens._TableReference[] fromClause = qs.getFromClause();
		for(int i=0; i<fromClause.length; i++)
		{
			QueryTokens._TableReference token = fromClause[i];
			if(token instanceof QueryTokens.Join)
			{
				tables.put(SQLFormatter.stripQuote(((QueryTokens.Join)token).getPrimary().getTable().getReference()),((QueryTokens.Join)token).getPrimary().getTable());
				tables.put(SQLFormatter.stripQuote(((QueryTokens.Join)token).getForeign().getTable().getReference()),((QueryTokens.Join)token).getForeign().getTable());
			}
			else if(token instanceof DerivedTable) // added for #80 derived table
			{
				tables.put(SQLFormatter.stripQuote(((DerivedTable)token).getAlias()),token);			}
			else
			{
				tables.put(SQLFormatter.stripQuote(((QueryTokens.Table)token).getReference()),token);
			}
		}

		QueryTokens._Expression[] selectList = qs.getSelectList();
		for(int i=0; i<selectList.length; i++)
		{
			QueryTokens.Column c = doConvertColumn(tables,selectList[i]);
			if(c!=null)
			{
				qs.removeSelectList(selectList[i]);
				qs.addSelectList(c);				
			}
			else // added for ticket #49
			{
				qs.removeSelectList(selectList[i]);
				qs.addSelectList(selectList[i]);				
			} 
		}
	}
	
	public static QueryTokens.Column doConvertColumn(Hashtable tables, QueryTokens._Expression e)
		throws IOException
	{
		QueryTokens.Column c = null;
		
		StreamTokenizer stream = createTokenizer(new StringReader(e.toString()));
		for(ArrayList al = new ArrayList(); true;)
		{
			stream.nextToken();
			if(stream.ttype == StreamTokenizer.TT_EOF)
			{
				ListIterator li = al.listIterator();
				
				String ref = li.next().toString();
				String alias = null;
				
				while(li.hasNext())
				{
					String next = li.next().toString();
					
					if(next.toString().equals(String.valueOf(SQLFormatter.DOT))
					|| ref.endsWith(String.valueOf(SQLFormatter.DOT)))
						ref = ref + next;
					else
						alias = next;
				}
				
				ref = SQLFormatter.stripQuote(ref);
				int dot = ref.lastIndexOf(SQLFormatter.DOT);
				if(dot!=-1)
				{
					String owner = ref.substring(0,dot);
					String cname = ref.substring(dot+1);
					
					// to do fix #80 for derived tables
					if(tables.containsKey(owner))
					{
						if (tables.get(owner) instanceof QueryTokens.Table)
						{
							c = new QueryTokens.Column((QueryTokens.Table)tables.get(owner),cname);
							if(alias!=null) c.setAlias(alias);
						}
						else
						{
							// to do 
							// c = new QueryTokens.Column((DerivedTable)tables.get(owner),cname);
							// if(alias!=null) c.setAlias(alias);
							Application.alert("!!! Column belongs to derived table: " + owner + "." + cname + " !!!");
						}

					}
					// fix #92
					else
					{
						// to do raise and display error message
						Application.alert("Table or alias not found: " + owner);
					}
					// end fix #92
				}
				break;
			}
			else
			{
				if(stream.sval == null && (char)stream.ttype != SQLFormatter.DOT) break;
				al.add(stream.sval == null ? String.valueOf(SQLFormatter.DOT) : stream.sval);
			}
		}
		
		return c;
	}
	
	public static boolean isOperator(String s)
	{
		return isOperatorSimbol(s)
			|| s.equalsIgnoreCase("IS") || s.equalsIgnoreCase("IS NOT")
			|| s.equalsIgnoreCase("IN") || s.equalsIgnoreCase("NOT IN")
			|| s.equalsIgnoreCase("LIKE") || s.equalsIgnoreCase("NOT LIKE")
			|| s.equalsIgnoreCase("EXISTS") || s.equalsIgnoreCase("NOT EXISTS")
			|| s.equalsIgnoreCase("BETWEEN") || s.equalsIgnoreCase("NOT BETWEEN");
	}
	
	public static boolean isOperatorSimbol(String s)
	{
		return s.equals("<") || s.equals(">") || s.equals("=") || s.equals("<=") || s.equals("=>") || s.equals("<=") || s.equals(">=") 
			|| s.equals("<>")  || s.equals("!=");
	}
	
	public static boolean isReservedWord(String s)
	{
		return isClauseWord(s) || isJoinWord(s) || s.equals(_ReservedWords.ON) || s.equals(_ReservedWords.AND) || s.equals(_ReservedWords.OR);
	}

	public static boolean isJoinWord(String s)
	{
		return s.equalsIgnoreCase(_ReservedWords.INNER_JOIN) || s.equalsIgnoreCase(_ReservedWords.FULL_OUTER_JOIN)
			|| s.equalsIgnoreCase(_ReservedWords.LEFT_OUTER_JOIN) || s.equalsIgnoreCase(_ReservedWords.RIGHT_OUTER_JOIN)
			|| s.equalsIgnoreCase(_ReservedWords.JOIN) || s.equalsIgnoreCase(_ReservedWords.FULL_JOIN)
			|| s.equalsIgnoreCase(_ReservedWords.LEFT_JOIN) || s.equalsIgnoreCase(_ReservedWords.RIGHT_JOIN);
	}
	
	public static boolean isClauseWord(String s)
	{
		return s.equalsIgnoreCase(_ReservedWords.SELECT) || s.equalsIgnoreCase(_ReservedWords.FROM)
			|| s.equalsIgnoreCase(_ReservedWords.WHERE) || s.equalsIgnoreCase(_ReservedWords.GROUP_BY)
			|| s.equalsIgnoreCase(_ReservedWords.HAVING) || s.equalsIgnoreCase(_ReservedWords.UNION)
			|| s.equalsIgnoreCase(_ReservedWords.ORDER_BY);
	}
	
	private static void doAdjustSequence(ArrayList al)
	{
		for(int i=0; i<al.size(); i++)
		{
			if(al.get(i).toString().equalsIgnoreCase(_ReservedWords.SELECT)
			|| al.get(i).toString().equalsIgnoreCase(_ReservedWords.FROM)
			|| al.get(i).toString().equalsIgnoreCase(_ReservedWords.HAVING))
			{
				al.set(i, al.get(i).toString().toUpperCase());
			}
			else if(al.get(i).toString().equalsIgnoreCase("BY"))
			{
				al.set(i-1, al.get(i-1).toString().toUpperCase() + SQLFormatter.SPACE + "BY");
				al.remove(i--);
			}
			else if(al.get(i).toString().equalsIgnoreCase("JOIN"))
			{
				if (al.get(i-1).toString().equalsIgnoreCase("CROSS"))
				{	
					al.set(i-1, ",");
					al.remove(i--);
				}
				else if(al.get(i-1).toString().equalsIgnoreCase("INNER")
					||al.get(i-1).toString().equalsIgnoreCase("LEFT")
					||al.get(i-1).toString().equalsIgnoreCase("RIGHT")
					||al.get(i-1).toString().equalsIgnoreCase("FULL"))
				{	
					al.set(i-1, al.get(i-1).toString().toUpperCase() + SQLFormatter.SPACE + "JOIN");
					al.remove(i--);
				}
				else if(al.get(i-1).toString().equalsIgnoreCase("OUTER"))
				{
					al.set(i-2, al.get(i-2).toString().toUpperCase() + SQLFormatter.SPACE + "OUTER" + SQLFormatter.SPACE + "JOIN");
					al.remove(i--);
					al.remove(i--);
				}
			}
			else if(al.get(i).toString().equalsIgnoreCase("NOT"))
			{
				if(al.get(i-1).toString().equalsIgnoreCase("IS"))
				{
					al.set(i-1, "IS NOT");
					al.remove(i--);
				}
				else if(al.get(i+1).toString().equalsIgnoreCase("IN")
				|| al.get(i+1).toString().equalsIgnoreCase("LIKE")
				|| al.get(i+1).toString().equalsIgnoreCase("EXISTS")
				|| al.get(i+1).toString().equalsIgnoreCase("BETWEEN"))
				{
					al.set(i, "NOT" + SQLFormatter.SPACE + al.get(i+1).toString().toUpperCase());
					al.remove(i+1);
				}
			}
			else if(al.get(i).toString().equals("="))
			{
				if(al.get(i-1).toString().equals("<") || al.get(i-1).toString().equals(">") || al.get(i-1).toString().equals("!"))
				{
					al.set(i-1, al.get(i-1).toString() + "=");
					al.remove(i--);
				}
			}
			else if(al.get(i).toString().equals(">") && al.get(i-1).toString().equals("="))
			{
				al.set(i-1, ">=");
				al.remove(i--);
			}
			else if(al.get(i).toString().equals(">") && al.get(i-1).toString().equals("<"))
			{
				al.set(i-1,"<>");
				al.remove(i--);
			}
			else if(al.get(i).toString().equals("<") && al.get(i-1).toString().equals("="))
			{
				al.set(i-1, "<=");
				al.remove(i--);
			}
			else if(al.get(i).toString().equals("."))
			{
				al.set(i, al.get(i-1).toString() + SQLFormatter.DOT + al.get(i+1).toString());
				al.remove(i-1);
				al.remove(i--);
			}
		}
	}
	
	private static ArrayList doTokenize(Reader r)
		throws IOException
	{
		ArrayList al = new ArrayList();
		StreamTokenizer stream = createTokenizer(r);
		
		while(stream.ttype != StreamTokenizer.TT_EOF)
		{
			stream.nextToken();
			if(stream.ttype == StreamTokenizer.TT_WORD)
			{
				al.add(stream.sval);
			}
			else if(stream.ttype == StreamTokenizer.TT_NUMBER)
			{
				Double dval = new Double(stream.nval);
				al.add(dval.doubleValue() == dval.intValue() ? new Integer((int)stream.nval) : (Number)dval);
			}
			else if(stream.ttype != StreamTokenizer.TT_EOF)
			{
				if(stream.sval == null)
				{
					al.add(new Character((char)stream.ttype));
				}
				else
				{
					al.add((char)stream.ttype + stream.sval + (char)stream.ttype);
				}
			}
		}
		
		if(al.size() > 0 && !al.get(al.size()-1).toString().equals(";")) al.add(new Character(';'));
		return al;
	}
	
	private static StreamTokenizer createTokenizer(Reader r)
	{

		StreamTokenizer stream = new StreamTokenizer(r);

		stream.ordinaryChar('.');
		stream.ordinaryChar('/'); // fix for ticket #48
		stream.wordChars('$','$'); // fix for ticket #48
		stream.wordChars('#','#'); // fix for ticket #86
		stream.wordChars('_','_');

	
		if(!QueryBuilder.identifierQuoteString.equals("\""))
		{
			if ((int)QueryBuilder.identifierQuoteString.charAt(0) != 32)	
				// QuoteString=" " with SQLite ...
				stream.quoteChar(QueryBuilder.identifierQuoteString.charAt(0));
			
//			for(int i=0; i<QueryBuilder.identifierQuoteString.length(); i++)
//			{
//				char wc = QueryBuilder.identifierQuoteString.charAt(i);
//				stream.wordChars(wc,wc);
//			}
		}
		
		stream.slashSlashComments(true);
		stream.slashStarComments(true);
		
		return stream;
	}

/*
	private static void print(ArrayList al)
	{
		print(al.toArray());
	}

	private static void print(Object[] o)
	{
		for(int i=0; i<o.length; i++)
			System.out.println(o[i].getClass().getName() + "[ " + o[i].toString() + " ]");
		
		System.out.println("----------------------------------------------------------------------");
	}
 */

	public static void main(String[] args)
	{
		QueryBuilder.useAlwaysQuote = false;
		
		try
		{
			String fname = "E:/SQLeonardo/tmp/test.sql";
			QueryModel qm = toQueryModel(new java.io.FileReader(fname));
			
//			QueryBuilder.identifierQuoteString = new String("`");
//			String sql = "SELECT `nome tabella`.`primo campo`, `nome tabella`.`secondo campo` FROM `nome tabella` WHERE `nome tabella`.`primo campo` BETWEEN 1 AND 10";
//			String sql = "SELECT \"nome tabella\".\"primo campo\", \"nome tabella\".\"secondo campo\" FROM \"nome tabella\"";
//			String sql = "SELECT aa.idSoggetto AS aa_idSoggetto,bb.idCliente,bb.cCognome,bb.cNome FROM aa aa INNER JOIN bb bb ON aa.idSoggetto = bb.idSoggetto WHERE (aa.cPartitaIva = '' OR aa.cPartitaIva IS null) AND (aa.cCodiceFiscale = '' OR aa.cCodiceFiscale IS null) AND bb.idCliente NOT IN (select idcliente from cc)";		
//			QueryModel qm = toQueryModel(sql);
			
			System.out.println(qm.toString(true));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}