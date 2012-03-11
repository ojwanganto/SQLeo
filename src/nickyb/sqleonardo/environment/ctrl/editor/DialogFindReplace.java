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

package nickyb.sqleonardo.environment.ctrl.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;

import nickyb.sqleonardo.common.gui.CommandButton;
import nickyb.sqleonardo.common.gui.TextView;
import nickyb.sqleonardo.environment.Application;

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
	
	public DialogFindReplace(TextView view)
	{
		super(Application.window,"find/replace");
		this.view = view;
		
		init();
		pack();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(Application.window);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				DialogFindReplace.this.view.getHighlighter().removeAllHighlights();
			}
		});
	}
	
	private void init()
	{
		JLabel lbl;
		GridBagConstraints gbc;
		
		getContentPane().setLayout(new GridBagLayout());
		
		lbl = new JLabel("find:");
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

        lbl = new JLabel("replace:");
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

        chxCase = new JCheckBox("case sensitive");
        chxCase.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chxCase.setMargin(new Insets(0, 0, 0, 0));
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 5, 10, 0);
        add(chxCase,gbc);

		JPanel pnlB = new JPanel();
		pnlB.add(btnFind		= new CommandButton("find",this));
		pnlB.add(btnFindAll		= new CommandButton("find all",this));
		pnlB.add(btnReplace		= new CommandButton("replace",this));
		pnlB.add(btnReplaceAll	= new CommandButton("replace all",this));
		btnReplace.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(pnlB,gbc);
	}

	private int find(int from)
	{
		int offset = -1;
		
		try
		{
			SQLStyledDocument document = (SQLStyledDocument)view.getDocument();
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
	
	private boolean findNext()
	{
		Highlighter highlighter = view.getHighlighter();
		int from = highlighter.getHighlights().length == 1 ? highlighter.getHighlights()[0].getEndOffset() : 0;
		highlighter.removeAllHighlights();
		find(from);
		return highlighter.getHighlights().length>0;
	}
	
	private boolean findAll()
	{
		Highlighter highlighter = view.getHighlighter();
		highlighter.removeAllHighlights();
		for(int from=0; (from=find(from))!=-1; from++);
		return highlighter.getHighlights().length>0;
	}
	
	private boolean replace()
	{
		try
		{
			Highlighter highlighter = view.getHighlighter();
			if(highlighter.getHighlights().length < 1) return false;
			
			Highlight tag = highlighter.getHighlights()[0];
			highlighter.removeHighlight(tag);
			
			SQLStyledDocument document = (SQLStyledDocument)view.getDocument();
			document.remove(tag.getStartOffset(),tag.getEndOffset()-tag.getStartOffset());
			if(txtReplace.getText().length() > 0) document.insertString(tag.getStartOffset(),txtReplace.getText());
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		
		return findNext();
	}

	private boolean replaceNext()
	{
		Highlighter highlighter = view.getHighlighter();
		if(highlighter.getHighlights().length < 1) find(0);
		return replace();
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnFind)
		{
			btnReplace.setEnabled(findNext());
		}
		else if(ae.getSource() == btnFindAll)
		{
			btnReplace.setEnabled(findAll());
		}
		else if(ae.getSource() == btnReplace)
		{
			btnReplace.setEnabled(replace());			
		}
		else if(ae.getSource() == btnReplaceAll)
		{
			btnReplace.setEnabled(false);
			while(replaceNext());
		}
	}
}
