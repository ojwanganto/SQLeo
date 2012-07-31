/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 *  
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

package com.sqleo.common.gui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import com.sqleo.environment.ctrl.editor.SQLStyledDocument;
import com.sqleo.querybuilder.QueryStyledDocument;


public class TextView extends BorderLayoutPanel
{
	private JTextPane editor;
	private CompoundUndoManager undoManager;
	
	public TextView(StyledDocument doc)
	{
		editor = new JTextPane();
		editor.setDocument(doc);
		if(doc instanceof QueryStyledDocument){
			//request view 
			undoManager = new CompoundUndoManager(editor);
		}
		editor.addMouseListener(new InternalPopup());
		editor.setFont(new Font("monospaced", Font.PLAIN, 12));
		
		
		editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,KeyEvent.SHIFT_MASK),DefaultEditorKit.pasteAction);
		editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,KeyEvent.CTRL_MASK),DefaultEditorKit.copyAction);
		editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,KeyEvent.SHIFT_MASK),DefaultEditorKit.cutAction);		
		
		BorderLayoutPanel noWrapPanel = new BorderLayoutPanel();
		noWrapPanel.setComponentCenter(editor);		
		
		JScrollPane scroll = new JScrollPane(noWrapPanel);
		scroll.getVerticalScrollBar().setUnitIncrement(25);
		setComponentCenter(scroll);
		
		this.setTabSize(4);
		
	}
	
	public void setEditable(boolean b)
	{
		editor.setEditable(b);
	}

	public void setTabSize(int size)
	{
		FontMetrics fm = editor.getFontMetrics(editor.getFont());
		
		int charWidth = fm.charWidth('w');
		int tabWidth = charWidth * size;
 
		TabStop[] tabs = new TabStop[10];
		for (int j = 0; j < tabs.length; j++)
		{
			int tab = j + 1;
			tabs[j] = new TabStop( tab * tabWidth );
		}
 
		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);
		
		int length = editor.getDocument().getLength();
		editor.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
	}

	public void append(String str)
	{
		StyledDocument doc = getDocument();
		if (doc != null)
		{
			try
			{
				doc.insertString(doc.getLength(), str, null);
			}
			catch (BadLocationException e)
			{
			}
		}		
	}
	
	public String getText()
	{
		return editor.getText();
	}
	
	public void setText(String s)
	{
		editor.setText(s);
	}
	
	public int getCaretPosition()
	{
		return editor.getCaretPosition();
	}
	
	public void setCaretPosition(int position)
	{
		editor.setCaretPosition(position);
	}
	
	public void setSelectionStart(int start)
	{
		editor.setSelectionStart(start);
	}
	
	public void setSelectionEnd(int end)
	{
		editor.setSelectionEnd(end);
	}
	
	public String getSelectedText()
	{
		return editor.getSelectedText();
	}
	
	public StyledDocument getDocument()
	{
		return (StyledDocument)editor.getDocument();
	}
	
	public void setDocument(StyledDocument doc)
	{
		editor.setDocument(doc);
		if(doc instanceof QueryStyledDocument){
		    undoManager.registerListener(editor.getDocument());
		}
	}
	
	public int getLineCount()
	{
		Element map = getDocument().getDefaultRootElement();
		return map.getElementCount();
	}
	
	public int getLineOfOffset(int offset) throws BadLocationException
	{
		StyledDocument doc = this.getDocument();
		if (offset < 0)
		{
			throw new BadLocationException("Can't translate offset to line", -1);
		}
		else if (offset > doc.getLength())
		{
			throw new BadLocationException("Can't translate offset to line", doc.getLength()+1);
		}
		else
		{
			Element map = getDocument().getDefaultRootElement();
			return map.getElementIndex(offset);
		}
	}

	public int getLineStartOffset(int line) throws BadLocationException
	{
		int lineCount = getLineCount();
		if(line < 0)
		{
			throw new BadLocationException("Negative line", -1);
		}
		else if (line >= lineCount)
		{
			throw new BadLocationException("No such line", getDocument().getLength()+1);
		}
		else
		{
			Element map = getDocument().getDefaultRootElement();
			Element lineElem = map.getElement(line);
			
			return lineElem.getStartOffset();
		}
	}

	public int getLineEndOffset(int line) throws BadLocationException
	{
		int lineCount = getLineCount();
		if (line < 0)
		{
			throw new BadLocationException("Negative line", -1);
		}
		else if (line >= lineCount)
		{
			throw new BadLocationException("No such line", getDocument().getLength()+1);
		}
		else
		{
			Element map = getDocument().getDefaultRootElement();
			Element lineElem = map.getElement(line);
			
			int endOffset = lineElem.getEndOffset();
			return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
		}
	}
	
	public Highlighter getHighlighter()
	{
		return editor.getHighlighter();
	}
	
	public ActionMap getViewActionMap()
	{
		return editor.getActionMap();
	}
	
	public InputMap getViewInputMap()
	{
		return editor.getInputMap();
	}
	
	private class InternalPopup extends JPopupMenu implements MouseListener
	{
		InternalPopup()
		{
			DefaultEditorKit kit = new DefaultEditorKit();
			
			add(createItem("cut",DefaultEditorKit.cutAction,kit.getActions()));
			add(createItem("copy",DefaultEditorKit.copyAction,kit.getActions()));
			add(createItem("paste",DefaultEditorKit.pasteAction,kit.getActions()));
			add(createItem("delete",DefaultEditorKit.deletePrevCharAction,kit.getActions()));
			addSeparator();
			add(createItem("select all",DefaultEditorKit.selectAllAction,kit.getActions()));
			add(createItem("clear all","clear-all",new AbstractAction(){
				private static final long serialVersionUID = 1L;
				@Override
				public void actionPerformed(ActionEvent e) {
					if(editor.getDocument() instanceof QueryStyledDocument){
						//request Text view
						if(editor.getDocument() instanceof SQLStyledDocument){
							setDocument(new SQLStyledDocument());
						}else if(editor.getDocument() instanceof QueryStyledDocument){
							setDocument(new QueryStyledDocument());
						}	
						setCaretPosition(0);
						requestFocus();
					}else{//response text view
						setText(null);
						requestFocus();
					}
				}
			}));
			if(editor.getDocument() instanceof QueryStyledDocument){
				add(createItem("undo","undoMouse",editor.getActionMap().get("Undo")));
				add(createItem("redo","redoMouse",editor.getActionMap().get("Redo")));
			}
		}

		private JMenuItem createItem(String text, String key, Action[] actions)
		{
			JMenuItem item = new JMenuItem();
			
			for(int i=0; i<actions.length; i++)
			{
				if(actions[i].getValue(Action.NAME).toString().equals(key))
				{
					item.setAction(actions[i]);
					item.setText(text);
				}
			}

			return item;
		}
		private JMenuItem createItem(String text, String key, Action action)
		{
			JMenuItem item = new JMenuItem();
			item.setAction(action);
			item.setText(text);
			return item;
		}
		public void mousePressed(MouseEvent me)
		{
			TextView.this.editor.requestFocus();
		}

		public void mouseReleased(MouseEvent me)
		{
			if(SwingUtilities.isRightMouseButton(me))
			{
				boolean selection = TextView.this.editor.getSelectionStart() != TextView.this.editor.getSelectionEnd();
				
				getComponent(0).setEnabled(selection && TextView.this.editor.isEditable());
				getComponent(1).setEnabled(selection);
				getComponent(2).setEnabled(TextView.this.editor.isEditable());
				getComponent(3).setEnabled(selection && TextView.this.editor.isEditable());
				getComponent(6).setEnabled(TextView.this.editor.getText().length()>0);
				
				show(TextView.this.editor,me.getX(),me.getY());
			}	
		}

		public void mouseClicked(MouseEvent me){}
		public void mouseEntered(MouseEvent me){}
		public void mouseExited(MouseEvent me){}
	}
}