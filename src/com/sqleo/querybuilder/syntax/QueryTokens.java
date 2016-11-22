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

import com.sqleo.querybuilder.QueryBuilder;

public abstract class QueryTokens
{
	public interface _Base{}
	public interface _Expression extends _Base{}	
	public interface _TableReference extends _Base{}

	public interface _Alias
	{
		public boolean isAliasSet();
		public String getAlias();
		public void setAlias(String alias);
	}
	
	public static class DefaultExpression implements _Expression, _Alias
	{
		private String alias;
		private String value;
		
		public DefaultExpression()
		{
			this(null);
		}
		
		public DefaultExpression(String value)
		{
			this(value,null);
		}
		
		public DefaultExpression(String value,String alias)
		{
			setValue(value);
			setAlias(alias);
		}
		
		public boolean isAliasSet()
		{
			return alias!=null && !alias.equals("");
		}
		
		public final String getAlias()
		{
			return alias;
		}
		
		public final void setAlias(String alias)
		{
			if(alias!=null)
			{
				alias = alias.replace(' ','_');
				alias = alias.replace('.','_');
			}
			this.alias = alias;
		}
		
		public boolean isEmpty()
		{
			return value == null || value.trim().equals("");
		}
		
		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public String toString()
		{
			return isAliasSet() ? value + SQLFormatter.SPACE + _ReservedWords.AS +  SQLFormatter.SPACE + this.getAlias() : value;
		}

	}
	
	public abstract static class AbstractDatabaseObject implements _Alias
	{
		private String alias;
		private String name;
		
		public AbstractDatabaseObject(String name)
		{
			this.name = name;
		}

		public abstract String getIdentifier();
		
		public String getReference()
		{
			return isAliasSet() ? getAlias() : getIdentifier();
		}
		
		public String getName()
		{
			return name;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
		
		public boolean isAliasSet()
		{
			return alias!=null && !alias.equals("");
		}
		
		public final String getAlias()
		{
			return alias;
		}
		
		public final void setAlias(String alias)
		{
			if(alias!=null)
			{
				alias = alias.replace(' ','_');
				alias = alias.replace('.','_');
			}
			this.alias = alias;
		}
	}

	public static class Column extends AbstractDatabaseObject implements _Expression
	{
		private Table owner;
		
		public Column(Table owner, String name)
		{
			super(name);
			this.owner = owner;
		}

		public Table getTable()
		{
			return owner;
		}
		
		public String getIdentifier()
		{
			String identifier = SQLFormatter.ensureQuotes(this.getName(),!QueryBuilder.useAlwaysQuote);
			if(identifier!=null && identifier.contains("(") && identifier.endsWith(")")){
				//function count(ta.field)
				return identifier;
			}
			if(owner!=null) identifier = owner.getReference() + SQLFormatter.DOT + identifier;
			return identifier;
		}
		
		public String toString()
		{
			String declare = this.getIdentifier();
			if(declare == null) return getAlias();
			
			if(isAliasSet())
				declare = declare  + SQLFormatter.SPACE + _ReservedWords.AS +  SQLFormatter.SPACE + this.getAlias();
			
			return declare;
		}
		
		public String toStringWithoutAlias()
		{
			String declare = this.getIdentifier();
			if(declare == null) return getAlias();
			return declare;
		}

	}
	
	public static class Table extends AbstractDatabaseObject implements _TableReference
	{
		private String schema;
		
		public Table(String schema, String name)
		{
			super(name);
			this.schema = schema;
		}
		
		public String getSchema()
		{
			return schema;
		}

		public void setSchema(String schema)
		{
			this.schema = schema;
		}

		public String getIdentifier()
		{
			String identifier = schema != null ? schema + SQLFormatter.DOT + this.getName() : this.getName();
			return SQLFormatter.ensureQuotes(identifier,!QueryBuilder.useAlwaysQuote);
		}

		public String toString()
		{
			String declare = this.getIdentifier();
			if(declare == null) return getAlias();
			
			if(isAliasSet())
				declare = declare + SQLFormatter.SPACE + this.getAlias();
			
			return declare;
		}
	}
	
	public static class Join implements _TableReference
	{
		public static final int INNER		= 0;
		public static final int LEFT_OUTER	= 1;
		public static final int RIGHT_OUTER 	= 2;
		public static final int FULL_OUTER	= 3;

		
		private int type;
		private Condition condition;
		
		public Join(Column primary, String operator, Column foreign)
		{
			this(INNER,primary,operator,foreign);
		}
		
		public Join(int type, Column primary, String operator, Column foreign)
		{
			this.type = type;
			condition = new Condition(primary,operator,foreign);
		}

		public int getType()
		{
			return type;
		}
		
		public String getTypeName()
		{
			return getTypeString(type);
		}

		public static int getTypeInt(String type)
		{
			if(type.toUpperCase().equals(_ReservedWords.LEFT_OUTER_JOIN)||type.toUpperCase().equals(_ReservedWords.LEFT_JOIN)) return LEFT_OUTER;
			if(type.toUpperCase().equals(_ReservedWords.RIGHT_OUTER_JOIN)||type.toUpperCase().equals(_ReservedWords.RIGHT_JOIN)) return RIGHT_OUTER;
			if(type.toUpperCase().equals(_ReservedWords.FULL_OUTER_JOIN)||type.toUpperCase().equals(_ReservedWords.FULL_JOIN)) return FULL_OUTER;
			
			return INNER;
		}

		public static String getTypeString(int type)
		{
			switch(type)
			{
				case LEFT_OUTER:
					return _ReservedWords.LEFT_OUTER_JOIN;
				case RIGHT_OUTER:
					return _ReservedWords.RIGHT_OUTER_JOIN;
				case FULL_OUTER:
					return _ReservedWords.FULL_OUTER_JOIN;
				default:
					return _ReservedWords.INNER_JOIN;
			}
		}
		
		public void setType(int t)
		{
			type = t;
		}
		
		public Condition getCondition()
		{
			return condition;
		}
		
		public Column getPrimary()
		{
			return (Column)condition.getLeft();
		}
		
		public Column getForeign()
		{
			return (Column)condition.getRight();
		}
		
		public String toString()
		{
			return getPrimary().getTable().toString() + SQLFormatter.SPACE + this.getTypeName() + SQLFormatter.SPACE + getForeign().getTable().toString() +
					SQLFormatter.SPACE + _ReservedWords.ON + SQLFormatter.SPACE + this.getCondition();
		}
	}
	
	public static class Condition implements _Base
	{
		private _Expression left;
		private _Expression right;
		
		private String append;
		private String operator;
		
		public Condition()
		{
			this(null,new DefaultExpression(),"=",new DefaultExpression());
		}
		
		public Condition(_Expression left, String operator, _Expression right)
		{
			this(null,left, operator, right);
		}
		
		public Condition(String append, _Expression left, String operator, _Expression right)
		{
			this.left = left;
			this.right = right;
			this.append = append;
			this.operator = operator;
		}
		
		public String getAppend()
		{
			return append;
		}
		
		public void setAppend(String a)
		{
			append = a;
		}
		
		public _Expression getLeft()
		{
			return left;
		}
		
		public void setLeft(_Expression l)
		{
			left = l;
		}
		
		public _Expression getRight()
		{
			return right;
		}
		
		public void setRight(_Expression r)
		{
			right = r;
		}
		
		public String getOperator()
		{
			return operator;
		}
		
		public void setOperator(String o)
		{
			operator = o;
		}
		
		public String toString()
		{
			String exprL = null;
			String exprR = null;
			
			if(left!=null)
			{
				if(left instanceof Column)
					exprL = ((Column)left).getIdentifier();
				else
					exprL = left.toString();
			}
			
			if(right!=null)
			{
				if(right instanceof Column)
					exprR = ((Column)right).getIdentifier();
				else
					exprR = right.toString();
			}
			
			
			if(operator.equals("EXISTS") || operator.equals("NOT EXISTS"))
			{
				return (append!=null ? append + " " : "") + operator + " " + exprR;
			}
			
			return (append!=null ? append + " " : "") + exprL + " " + operator + " " + exprR;
		}
		
		public String toString(boolean wrap, int offset)
		{
			String exprL = null;
			String exprR = null;
			
			if(left!=null)
			{
				if(left instanceof Column){
					exprL = ((Column)left).getIdentifier();
				}else if(left instanceof SubQuery){
					if(left instanceof DerivedTable){
						final DerivedTable derivedTable = (DerivedTable)left;
						exprL = derivedTable.toString(wrap, offset+2);
					}else{
						final SubQuery sub = (SubQuery)left;
						exprL = sub.toString(wrap, offset+2);
					}
				}else{
					exprL = left.toString();
				}
			}
			if(right!=null)
			{
				if(right instanceof Column){
					exprR = ((Column)right).getIdentifier();
				}else if(right instanceof SubQuery){
					int leftLength = operator.length();
					if(append!=null){
						leftLength +=append.length();
					}
					if(exprL!=null){
						leftLength +=exprL.length();
					}
					final int offset2 = leftLength>0 ?  offset + 2 + leftLength/4 : offset;
					if(right instanceof DerivedTable){
						final DerivedTable derivedTable = (DerivedTable)right;
						exprR = derivedTable.toString(wrap, offset2);
					}else{
						final SubQuery sub = (SubQuery)right;
						exprR = sub.toString(wrap, offset2);
					}
				}else{
					exprR = right.toString();
				}
			}
			
			final String delimiter =  String.valueOf(SQLFormatter.SPACE);

			if(operator.equals("EXISTS") || operator.equals("NOT EXISTS"))
			{
				return (append!=null ? append + " " : "") +  operator +" "+exprR;
			}
			
			return (append!=null ? append + " " : "") + exprL + delimiter +operator + " " +exprR;
		}
	}

	public static class Group implements _Base
	{
		private _Expression expr;
		
		public Group(String value)
		{
			this(new DefaultExpression(value));
		}
		
		public Group(_Expression expr)
		{
			this.expr = expr;
		}
		
		public _Expression getExpression()
		{
			return expr;
		}		
		
		public String toString()
		{
// fix for ticket #12   return expr instanceof Column ? ((Column)expr).getReference() : expr.toString();
// fix for ticket #47	return expr instanceof Column ? ((Column)expr).getIdentifier() : expr.toString();
			return expr instanceof Column ? ((Column)expr).getIdentifier() : ((DefaultExpression)expr).getValue();
		}		
	}
	
	public static class Sort implements _Base
	{
		public static final short ASCENDING = 0;
		public static final short DESCENDING = 1;
		
		private short type;
		private _Expression expr;
		
		public Sort(_Expression expr)
		{
			this(expr,ASCENDING);
		}
		
		public Sort(_Expression expr, short type)
		{
			this.expr = expr;
			this.type = type;
		}
		
		public Sort(_Expression expr, boolean ascending)
		{
			this(expr, ascending ? ASCENDING : DESCENDING);
		}
		
		public boolean isAscending()
		{
			return type == ASCENDING;
		}
		
		public void setType(short t)
		{
			type = t;
		}
		
		public _Expression getExpression()
		{
			return expr;
		}
		
		public String toString()
		{
// fix for ticket #47	return (expr instanceof Column ? ((Column)expr).getIdentifier() : expr.toString() ) + (isAscending() ? " ASC" : " DESC");
			return (expr instanceof Column ? ((Column)expr).getIdentifier() : ((DefaultExpression)expr).getValue() ) + (isAscending() ? " ASC" : " DESC");
		}
	}
}