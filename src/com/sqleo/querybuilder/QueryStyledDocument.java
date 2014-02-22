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

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.sqleo.common.util.Text;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class QueryStyledDocument extends DefaultStyledDocument implements _ReservedWords
{
	protected static final String DELIMITER_LINE_END = "\n";
		
	protected MutableAttributeSet defaultAttributSet;
	protected MutableAttributeSet keywordAttributSet;
	
	public QueryStyledDocument()
	{
		putProperty(DefaultEditorKit.EndOfLineStringProperty, DELIMITER_LINE_END);
		
		defaultAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(defaultAttributSet, Color.black);

		keywordAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(keywordAttributSet, Color.blue);
		StyleConstants.setBold(keywordAttributSet, true);
	}
	
	public void insertString(int offset, String str) throws BadLocationException
	{
		insertString(offset, str, defaultAttributSet);
	}
	
	public void insertStringAndNoSyntaxHighlight(int offset, String str) throws BadLocationException
	{
		super.insertString(offset, str, defaultAttributSet);
	}
	
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException
	{
		super.insertString(offset,str,a);
    	this.onChanged(offset, str.length());
	}
		
	public void remove(int offset, int length) throws BadLocationException
	{
		super.remove(offset,length);
		this.onChanged(offset,0);
	}
	
	public void removeAndNoSyntaxHighlight(int offset, int length) throws BadLocationException
	{
		super.remove(offset,length);
	}
		
	public void onChanged(int offset, int length) throws BadLocationException
	{
		int contentLength = this.getLength();
		String content = this.getText(0,contentLength);
			
		if(Text.isEmpty(content)) return;
 
		Element root = this.getDefaultRootElement();
		int startLine = root.getElementIndex( offset );
		int endLine = root.getElementIndex( offset + length );
 
		for (int i = startLine; i <= endLine; i++)
		{
			int startOffset = root.getElement( i ).getStartOffset();
			int endOffset = root.getElement( i ).getEndOffset();
				
			if(endOffset > contentLength) endOffset = contentLength;
				
			if(content.substring(startOffset,endOffset).endsWith(DELIMITER_LINE_END))
				doSyntaxHighlight(content, startOffset, endOffset - 1);
			else
				doSyntaxHighlight(content, startOffset, endOffset);
		}
	}
		
	protected void doSyntaxHighlight(String content, int startOffset, int endOffset) throws BadLocationException
	{
		this.setCharacterAttributes(startOffset, (endOffset-startOffset), defaultAttributSet, true);
		
		if(startOffset>endOffset) return;
		checkKeywords(content, startOffset, endOffset);
	}
	
	protected void checkKeywords(String content, int startOffset, int endOffset)
	{
		String line = content.substring(startOffset,endOffset);
		
		for(String keyword : ALL_RESERVED_WORDS){
			checkKeyword(line, keyword , startOffset);
		}
	}

	protected void checkKeyword(String line, String keyword, int offsetgap)
	{
		String upper = line.toUpperCase();
		for(int i=upper.indexOf(keyword); i!=-1; i=upper.indexOf(keyword,++i))
		{
			boolean bKeyword = true;
			
			if(i > 0)
				bKeyword = isKeySeparator(line.charAt(i-1));
			
			if(bKeyword && (i+keyword.length()) < line.length())
				bKeyword = isKeySeparator(line.charAt(i+keyword.length()));
			
			if(bKeyword)
				this.setCharacterAttributes(offsetgap+i, keyword.length(), keywordAttributSet, false);
		}
	}
	
	private boolean isKeySeparator(char c)
	{
		return c == ' ' || c == '\t' || c == '(' || c == ')';
	}
}