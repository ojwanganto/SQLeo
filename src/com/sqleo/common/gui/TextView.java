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

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import com.sqleo.common.util.Trie;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.editor.SQLStyledDocument;
import com.sqleo.querybuilder.QueryStyledDocument;


public class TextView extends BorderLayoutPanel
{
	private JTextPane editor;
	private CompoundUndoManager undoManager;
	private SuggestionsView suggestions;
	private JScrollPane scroll;
	private static final Color DEFAULT_CURRENT_LINE_HIGHLIGHT_COLOR	= new Color(255,255,170);
	private InternalPopup mousePopup;

	public TextView(StyledDocument doc,final boolean calledFromCommandEditor)
	{
		editor = new JTextPane();
		editor.setDocument(doc);
		
        final int editorFontSize = Preferences.getScaledRowHeight(14);
		editor.setFont(new Font("monospaced", Font.PLAIN, editorFontSize));
		
		TextLineNumber lineNumberView = null;
		if(doc instanceof QueryStyledDocument){
			//request view 
			// add undo manager 
			undoManager = new CompoundUndoManager(editor);
			// add line highlighter 
			new LinePainter(editor,DEFAULT_CURRENT_LINE_HIGHLIGHT_COLOR);
			// add line number view 
			lineNumberView = new TextLineNumber(editor);
			lineNumberView.setCurrentLineForeground(Color.blue); 
			// add suggestions for auto complete
			if (Preferences.isAutoCompleteEnabled()) {
				suggestions = new SuggestionsView(editor, calledFromCommandEditor);
			}
		}
		mousePopup = new InternalPopup();
		editor.addMouseListener(mousePopup);
		editor.addCaretListener(new ParenthesisMatcher());
		
		editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,KeyEvent.SHIFT_MASK),DefaultEditorKit.pasteAction);
		editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,KeyEvent.CTRL_MASK),DefaultEditorKit.copyAction);
		editor.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,KeyEvent.SHIFT_MASK),DefaultEditorKit.cutAction);		
		
		BorderLayoutPanel noWrapPanel = new BorderLayoutPanel();
		noWrapPanel.setComponentCenter(editor);		
		
		scroll = new JScrollPane(noWrapPanel);
		scroll.getVerticalScrollBar().setUnitIncrement(25);
		if(lineNumberView!=null){
			scroll.setRowHeaderView(lineNumberView);
		}
		setComponentCenter(scroll);
		
		this.setTabSize(4);
		
	}
	
	public void setBackgroundColor(Color bg){
		editor.setBackground(bg);
	}
	
	public void addFormatQueryMouseAction(){
		mousePopup.addFormatQueryMouseAction();
	}
	
	public void scrollCenter()
	{
	    Container container = SwingUtilities.getAncestorOfClass(JViewport.class, editor);

	    if (container == null) return;

	    try
	    {
	        Rectangle r = editor.modelToView(editor.getCaretPosition());
	        JViewport viewport = (JViewport)container;

	        int extentWidth = viewport.getExtentSize().width;
	        int viewWidth = viewport.getViewSize().width;

	        int x = Math.max(0, r.x - (extentWidth / 2));
	        x = Math.min(x, viewWidth - extentWidth);

	        int extentHeight = viewport.getExtentSize().height;
	        int viewHeight = viewport.getViewSize().height;

	        int y = Math.max(0, r.y - (extentHeight / 2));
	        y = Math.min(y, viewHeight - extentHeight);
	        
	        //smooth scroll
//	        int oldY = viewport.getViewPosition().y;
//            for(int i = oldY;i<y;i++){
//            	viewport.setViewPosition(new Point(x, i));
//                try {
//					Thread.sleep(2);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//            }
            // use the below comment if we don't need smooth scroll. 
	         viewport.setViewPosition(new Point(x, y));
	    }
	    catch(BadLocationException ble) {}
	}
	
	public void reloadSuggestionsTrie(final Trie prefixTree,final String chKey){
		if(suggestions!=null){
			suggestions.setPrefixTree(prefixTree,chKey);
		}
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
	
	public void replaceSelection(String str)
	{
		final StyledDocument doc = getDocument();
		if (doc != null)
		{
			try
			{
				final int start = editor.getSelectionStart();
				final int end = editor.getSelectionEnd();
				doc.remove(start, end-start);
				doc.insertString(start, str, null);
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
			
			add(createItem("Cut",DefaultEditorKit.cutAction,kit.getActions()));
			add(createItem("Copy",DefaultEditorKit.copyAction,kit.getActions()));
			add(createItem("Paste",DefaultEditorKit.pasteAction,kit.getActions()));
			add(createItem("Delete",DefaultEditorKit.deletePrevCharAction,kit.getActions()));
			addSeparator();
			add(createItem("Select all",DefaultEditorKit.selectAllAction,kit.getActions()));
			add(createItem("Clear all","Clear-all",new AbstractAction(){
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
				add(createItem("Undo","undoMouse",editor.getActionMap().get("Undo")));
				add(createItem("Redo","redoMouse",editor.getActionMap().get("Redo")));
			}
		}
		
		public void addFormatQueryMouseAction(){
			add(createItem("Format","format",editor.getActionMap().get("format-query")));
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
				if(getComponents().length>9){
					getComponent(9).setEnabled(selection);
				}
				show(TextView.this.editor,me.getX(),me.getY());
			}	
		}

		public void mouseClicked(MouseEvent me){}
		public void mouseEntered(MouseEvent me){}
		public void mouseExited(MouseEvent me){}
	}
}