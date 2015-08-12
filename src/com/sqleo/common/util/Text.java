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

public class Text
{
	public static final String UNDEFiNED = "Undefined";
	
	public static boolean isEmpty(String s)
	{
		return s == null || s.length() == 0;
	}
	
	/**
	 * Es:
	 *	String source = "bla1 bla3";
	 *	
	 *	insertText(source, "bla2 ", 5);
	 *  
	 *	return -> "bla1 bla2 bla3"
	 */
	public static String insertText(String source, String text, int index)
	{
		return left(source, index) + text + source.substring(index);
	}
	
	public static String left(String source, int len)
	{
		return source.substring(0, len);
	}
	
	/**
	 * Es:
	 *	String source = "bla1 bla2 bla3";
	 *	
	 *	right(source, 5);
	 *  
	 *	return -> " bla3"
	 */
	public static String right(String source, int len)
	{
		return source.substring(source.length()-len);
	}
	
	/**
	 * Es:
	 *	String source = "bla1 bla2 bla3";
	 *	
	 *	replaceText(source, "bl", "cr");
	 *  
	 *	return -> "cra1 cra2 cra3"
	 */
	public static String replaceText(String source, String find, String replace)
	{
		for(int index = source.indexOf(find); index != -1; index = source.indexOf(find,index))
		{
			source = replaceText(source, replace, index, find.length());
			index = index + replace.length();
		}
		
		return source;
	}
	
	/**
	 * Es:
	 *	String source = "bla1 bla2 bla3";
	 *	
	 *	replaceText(source, "cra", 5);
	 *  
	 *	return -> "bla1 cra"
	 */
	public static String replaceText(String source, String text, int start)
	{
		return left(source, start) + text;
	}
	
	/**
	 * Es:
	 *	String source = "bla1 bla2 bla3";
	 *	
	 *	replaceText(source, "cr", 5, 7);
	 *  
	 *	return -> "bla1 cra3"
	 */
	public static String replaceText(String source, String text, int start, int len)
	{
		return left(source, start) + text + source.substring(start+len);
	}
	
	public static String trimBoth(String source){
		final String trimLeft = trimLeft(source);
		return trimRight(trimLeft);
	}
	
	public static String trimLeft(String source)
	{
		int len = source.length();
		for(int i=0;i<len;i++)
		{
		  if(!Character.isWhitespace(source.charAt(i))){
			 return source.substring(i);
		  }
		}
		return source;
	}
	
	public static String trimRight(String source)
	{
		int len = source.length();
		for(;len>0;len--)
		{
		  if(!Character.isWhitespace(source.charAt(len-1)))
		     break;
		}
		return source.substring( 0, len);
	}
	
	public static String wrap(String s, int len)
	{
		StringBuffer sb = new StringBuffer();
		while(s.length() > len)
		{
			sb.append(s.substring(0,len) + "\n");
			s = s.substring(len);
		}
		sb.append(s);
		
		return sb.toString();
	}
	
	public static String escapeHTML(final String s) {
		final int slen = s.length();
	    final StringBuilder out = new StringBuilder(Math.max(16, slen));
	    char c;
	    for (int i = 0; i < slen; i++) {
	        c = s.charAt(i);
	        if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
	            out.append("&#");
	            out.append((int) c);
	            out.append(';');
	        } else {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
}