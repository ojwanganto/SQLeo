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

package com.sqleo.environment.ctrl.editor;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.sqleo.querybuilder.QueryStyledDocument;


public class SQLStyledDocument extends QueryStyledDocument
{
	private static final String DELIMITER_COMMENT_MULTI_LINE_START = "/*";
	private static final String DELIMITER_COMMENT_MULTI_LINE_END = "*/";		
	private static final List<String> DELIMITER_COMMENT_SINGLE_LINE = Arrays.asList("//","--","#");

	private MutableAttributeSet commentAttributSet;
	
	public SQLStyledDocument()
	{
		super();
		
		commentAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(commentAttributSet, new Color(63,127,95));
	}
	
	protected void doSyntaxHighlight(String content, int startOffset, int endOffset) throws BadLocationException
	{
		super.doSyntaxHighlight(content, startOffset, endOffset);
		checkSingleLineComments(content, startOffset, endOffset);
		checkMultiLineComments(content, startOffset, endOffset);
	}
	
	private void checkSingleLineComments(String content, int startOffset, int endOffset){
		for(final String delimCommentSingleLine : DELIMITER_COMMENT_SINGLE_LINE){
			int index = content.indexOf( delimCommentSingleLine, startOffset );
			if ( (index > -1) && (index < endOffset) )
			{
				this.setCharacterAttributes(index, endOffset - index + 1, commentAttributSet, false);
				endOffset = index - 1;
				break;
			}
		}
		
		if(startOffset>endOffset) return;
	}
	
	private void checkMultiLineComments(String content, int startOffset, int endOffset)
	{
		if(startOffset>endOffset) return;
		while(true){
			int index = content.indexOf( DELIMITER_COMMENT_MULTI_LINE_START, startOffset );
			if (index > -1)
			{
				int index2 = content.indexOf( DELIMITER_COMMENT_MULTI_LINE_END, index );
	 
				if ( (index2 == -1) || (index2 > endOffset) )
				{
					this.setCharacterAttributes(index, index2 - index + 1, commentAttributSet, false);
					if (index2 == -1) return;
					else startOffset = index2;
				}
				else if (index2 >= startOffset)
				{
					this.setCharacterAttributes(index, index2 + 2 - index, commentAttributSet, false);
					startOffset = index2;
				}
			}else{
				return;
			}
			if(startOffset>endOffset) return;
		}
		
	}
	
}
