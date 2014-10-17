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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.StyledDocument;

import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.gui.TextView;
import com.sqleo.environment.Application;
import com.sqleo.querybuilder.QueryStyledDocument;


public class DialogFindReplace extends JDialog implements ActionListener
{
	private DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
	
	private CommandButton btnFind;
	private CommandButton btnFindAll;
	private CommandButton btnReplace;
	private CommandButton btnReplaceAll;
	
	private JCheckBox chxCase;
	
	private JTextField txtFind;
	private JTextField txtReplace;	
	
	private TextView view;
	
	private boolean disableReplace = false;
	
	public DialogFindReplace(TextView view)
	{
		super(Application.window,"Find/replace");
		this.view = view;
		
		init();
		pack();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(Application.window);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				beforeClose();
			}

			
		});
		
		initKeyListener();
	}
	
	private void beforeClose() {
		DialogFindReplace.this.view.getHighlighter().removeAllHighlights();
	}
	
	private void initKeyListener() {
	    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
	        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE , 0), "close");
	    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
	        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "find");
	    getRootPane().getActionMap().put("close", new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	        	beforeClose();
	        	dispose();
	        }
	    });
	    getRootPane().getActionMap().put("find", new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	            btnFind.doClick();
	        }
	    });
	  }
	
	private void init()
	{
		JLabel lbl;
		GridBagConstraints gbc;
		
		getContentPane().setLayout(new GridBagLayout());
		
		lbl = new JLabel("Find:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(8, 5, 3, 0);
        getContentPane().add(lbl,gbc);

        txtFind = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 2, 3, 5);
        add(txtFind,gbc);

        lbl = new JLabel("Replace:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 7, 0);
        add(lbl,gbc);

        txtReplace = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 2, 7, 5);
        add(txtReplace,gbc);

        chxCase = new JCheckBox("Case sensitive");
        chxCase.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chxCase.setMargin(new Insets(0, 0, 0, 0));
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 5, 10, 0);
        add(chxCase,gbc);

		JPanel pnlB = new JPanel();
		pnlB.add(btnFind		= new CommandButton("Find",this));
		pnlB.add(btnFindAll		= new CommandButton("Find all",this));
		pnlB.add(btnReplace		= new CommandButton("Replace",this));
		pnlB.add(btnReplaceAll	= new CommandButton("Replace all",this));
		btnReplace.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(pnlB,gbc);
	}
	
	public void disableReplace(){
		btnReplace.setEnabled(false);
		btnReplaceAll.setEnabled(false);
		txtReplace.setEnabled(false);
		disableReplace = true;
		setTitle("Find");
	}

	private int find(int from)
	{
		int offset = -1;
		
		try
		{
			StyledDocument document = (StyledDocument)view.getDocument();
			String text = document.getText(document.getStartPosition().getOffset(),document.getEndPosition().getOffset());
			String find = txtFind.getText();
			
			if(text.length() == 0 || find.length() == 0) return offset;
			
			if(!chxCase.isSelected())
			{
				find = find.toLowerCase();
				text = text.toLowerCase();
			}
			
			if((offset = text.indexOf(find,from))!=-1)
			{
				int len = offset+txtFind.getText().length();
				Highlighter highlighter = view.getHighlighter();
				highlighter.addHighlight(offset,len,highlightPainter);
			}
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return offset;
	}
	
	private boolean findNext(boolean alertNotFound)
	{
		Highlighter highlighter = view.getHighlighter();
		int from = highlighter.getHighlights().length == 1 ? highlighter.getHighlights()[0].getStartOffset()+1 : 0;
		highlighter.removeAllHighlights();
		find(from);
		boolean found = highlighter.getHighlights().length>0;
		scrollViewToHighlight(highlighter, found, alertNotFound);
		return found;
	}

	private void scrollViewToHighlight(Highlighter highlighter, boolean found, boolean alertNotFound) {
		if(found){
			Highlight firstMatch = highlighter.getHighlights()[0];
			view.setCaretPosition(firstMatch.getStartOffset()>0?firstMatch.getStartOffset():0);
			view.scrollCenter();		}
		else if(alertNotFound)
			Application.alert("Could not find: " + txtFind.getText());
	}
	
	private boolean findAll()
	{
		Highlighter highlighter = view.getHighlighter();
		highlighter.removeAllHighlights();
		for(int from=0; (from=find(from))!=-1; from++);
		boolean found = highlighter.getHighlights().length>0;
		scrollViewToHighlight(highlighter, found,true);
		return found;
	}
	
	private boolean replaceAndNext()
	{
		boolean replaced = replaceOnly();
		if(!replaced){
			return false;
		}else {
			return findNext(false);
		}
	}

	private boolean replaceOnly() {
		try
		{
			Highlighter highlighter = view.getHighlighter();
			if(highlighter.getHighlights().length < 1) return false;
			
			Highlight tag = highlighter.getHighlights()[0];
			replaceHighlight(highlighter, tag, true);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		return true;
	}

	private void replaceHighlight(Highlighter highlighter, Highlight tag, boolean highlightSyntax)
			throws BadLocationException {
		
		highlighter.removeHighlight(tag);
		
		QueryStyledDocument document = (QueryStyledDocument)view.getDocument();
		if(highlightSyntax){
			document.remove(tag.getStartOffset(),tag.getEndOffset()-tag.getStartOffset());
		}else{
			document.removeAndNoSyntaxHighlight(tag.getStartOffset(),tag.getEndOffset()-tag.getStartOffset());
		}
		if(txtReplace.getText().length() > 0) {
			if(highlightSyntax){
				document.insertString(tag.getStartOffset(),txtReplace.getText());
			}else{
				document.insertStringAndNoSyntaxHighlight(tag.getStartOffset(),txtReplace.getText());
			}
		}
	}

	
	private void replaceAll()
	{
		try
		{
			findAll();
			int found = view.getHighlighter().getHighlights().length;
			int idx =0,first = 0,last = 0;
			for(Highlight tag : view.getHighlighter().getHighlights()){
				if(idx == 0){
					first = tag.getStartOffset();
				}
				if(idx == found-1){
					last = tag.getStartOffset() + txtReplace.getText().length();
				}
				
				replaceHighlight(view.getHighlighter(), tag, false);
			    idx++;
			}
			if(found == 0){
				Application.alert("Could not find: " + txtFind.getText());
			}else {
				QueryStyledDocument document = (QueryStyledDocument)view.getDocument();
				document.onChanged(first, last);
				Application.alert(found+ " occurences were replaced");
			}
		}
		catch (BadLocationException e)
		{
				e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnFind)
		{
			boolean findNext = findNext(true);
			if(!disableReplace){
				btnReplace.setEnabled(findNext);
			}
		}
		else if(ae.getSource() == btnFindAll)
		{
			boolean findAll = findAll();
			if(!disableReplace){
				btnReplace.setEnabled(findAll);
			}
		}
		else if(ae.getSource() == btnReplace)
		{
			boolean replaceAndNext = replaceAndNext();
			if(!disableReplace){
				btnReplace.setEnabled(replaceAndNext);
			}
		}
		else if(ae.getSource() == btnReplaceAll)
		{
			btnReplace.setEnabled(false);
			replaceAll();
		}
	}
}
