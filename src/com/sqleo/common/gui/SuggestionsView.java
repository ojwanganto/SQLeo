/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2013 anudeepgade@users.sourceforge.net
 *  
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.common.util.Trie;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi.ClientCommandEditor;
import com.sqleo.environment.mdi.ClientQueryBuilder;
import com.sqleo.environment.mdi.MDIClient;
import com.sqleo.querybuilder.syntax._ReservedWords;

public class SuggestionsView {

	private final JTextPane textPane;
	private SuggestionPanel suggestion;
	private Trie prefixTree;
	private String schema;
	private Connection connection;
	private boolean columnMode = false;
	private boolean calledFromCommandEditor = true;

	public SuggestionsView(final JTextPane textPane, final boolean calledFromCommandEditor) {
		this.textPane = textPane;
		this.textPane.addKeyListener(new SuggestionsKeyListener());
		this.calledFromCommandEditor = calledFromCommandEditor;
	}

	private class SuggestionPanel {
		private JList list;
		private final JPopupMenu popupMenu;
		private final String subWord;
		private final int insertionPosition;

		public SuggestionPanel(final JTextPane textPane, final int position, final String subWord, final Point location) {
			if (null == prefixTree) {
				if (calledFromCommandEditor) {
					final MDIClient client = Application.window.getClient(ClientCommandEditor.DEFAULT_TITLE);
					if (client != null) {
						final ClientCommandEditor ce = (ClientCommandEditor) client;
						prefixTree = ce.getPrefixTree();
						connection = ConnectionAssistant.getHandler(ce.getActiveConnection()).get();
						schema = SQLHelper.getSchemaFromUser(ce.getActiveConnection());
					}
				} else {
					final MDIClient[] clients =
							Application.window.getClientsOfConnection(ClientQueryBuilder.DEFAULT_TITLE, null);
					if (clients.length >= 1) {
						final ClientQueryBuilder ce = (ClientQueryBuilder) clients[0];
						prefixTree = ce.getPrefixTree();
						connection =
								ConnectionAssistant.getHandler(ce.getQueryBuilder().getConnectionHandlerKey()).get();
						if (ce.getQueryBuilder().getDiagramLayout().getQueryModel().getSchema()!=null){
							schema = ce.getQueryBuilder().getDiagramLayout().getQueryModel().getSchema();
						}else {
							schema = SQLHelper.getSchemaFromUser(ce.getQueryBuilder().getConnectionHandlerKey());
						}
					}
				}

			}
			this.insertionPosition = position;
			this.subWord = subWord;
			popupMenu = new JPopupMenu();
			popupMenu.removeAll();
			popupMenu.setOpaque(false);
			popupMenu.setBorder(null);
			popupMenu.add(list = createSuggestionList(position, subWord), BorderLayout.CENTER);
			popupMenu.show(textPane, location.x, textPane.getBaseline(0, 0) + location.y);
		}

		public void hide() {
			popupMenu.setVisible(false);
			if (suggestion == this) {
				suggestion = null;
			}
		}

		private JList createSuggestionList(final int position, final String subWord) {
			String[] namesStartingWith = null;
			if (subWord.endsWith(".")) {
				final String tempSubWord = subWord + "xxx";
				final String tableOrAliasName = tempSubWord.split("\\.")[0];
				namesStartingWith = SQLHelper.getColumns(connection, schema, tableOrAliasName.toUpperCase());
				if(null == namesStartingWith ||  namesStartingWith.length ==0){
					// means find the table from alias tableOrAliasName
					final String tableNameFromAlias = getTableNameFromAlias(tableOrAliasName);
					if(tableNameFromAlias!=null){
						namesStartingWith = SQLHelper.getColumns(connection, schema, tableNameFromAlias.toUpperCase());
					}
				}
				columnMode = true;
			} else if (prefixTree != null) {
				final List<String> foundValues = prefixTree.getWordsForPrefix(subWord);
				namesStartingWith = foundValues.toArray(new String[foundValues.size()]);
				columnMode = false;
			}
			final JList list = new JList(namesStartingWith);
			list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectedIndex(0);
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (e.getClickCount() == 2) {
						insertSelection();
					}
				}
			});
			return list;
		}

		public boolean insertSelection() {
			if (list.getSelectedValue() != null) {
				try {
					String selectedSuggestion;
					if (columnMode) {
						selectedSuggestion = (String) list.getSelectedValue();
						columnMode = false;
					} else {
						selectedSuggestion = ((String) list.getSelectedValue()).substring(subWord.length());
					}
					textPane.getDocument().insertString(insertionPosition, selectedSuggestion, null);
					return true;
				} catch (final BadLocationException e1) {
					e1.printStackTrace();
				}
				hideSuggestion();
			}
			return false;
		}

		public void moveUp() {
			final int index = Math.min(list.getSelectedIndex() - 1, 0);
			selectIndex(index);
		}

		public void moveDown() {
			final int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
			selectIndex(index);
		}

		private void selectIndex(final int index) {
			final int position = textPane.getCaretPosition();
			list.setSelectedIndex(index);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					textPane.setCaretPosition(position);
				};
			});
		}
	}

	private void showSuggestionLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				showSuggestion();
			}

		});
	}

	public String getTableNameFromAlias(final String alias) {
		final String text = textPane.getText();
		final String regex = "(?i)\\s+"+alias+"(\\n+|\\s+|$)";
		final Matcher matcher = Pattern.compile(regex).matcher(text);
		//find first matching alias
		if(matcher.find() == true){
			int aliasIndex = matcher.start();
			//Extract last 2 words before alias 
			int[] startEndIndexes2 = getWordStartEndPositions(aliasIndex-1,text);
			String word2 = text.substring(startEndIndexes2[0],startEndIndexes2[1]);
			if(_ReservedWords.AS.equals(word2.toUpperCase())){
				// If AS keyword found then find word before AS.
				int[] startEndIndexes = getWordStartEndPositions(startEndIndexes2[0]-1,text);
				return text.substring(startEndIndexes[0],startEndIndexes[1]);
			}else{
				return word2;
			}
		}
		return null;
	}
	final int[] getWordStartEndPositions(final int index, final String text){
		int end = index;
		//skip blanks
		while(end > 0){
			final char charAt = text.charAt(end);
			if (Character.isWhitespace(charAt)) {
				end--;
			}else{
				break;
			}
		}
		//now stop when blank occurs 
		int start = end;
		while(start > 0){
			final char charAt = text.charAt(start);
			if (!Character.isWhitespace(charAt)) {
				start--;
			}else{
				break;
			}
		}
		return new int[]{start+1, end+1};
	}

	private void showSuggestion() {
		hideSuggestion();
		final int position = textPane.getCaretPosition();
		Point location;
		try {
			location = textPane.modelToView(position).getLocation();
		} catch (final BadLocationException e2) {
			e2.printStackTrace();
			return;
		}
		final String text = textPane.getText();
		int start = Math.max(0, position - 1);
		while (start > 0) {
			final char charAt = text.charAt(start);
			if (!Character.isWhitespace(charAt)) {
				start--;
			} else {
				start++;
				break;
			}
		}
		if (start > position) {
			return;
		}
		final String subWord = text.substring(start, position);
		if (subWord.length() < 1) {
			return;
		}
		suggestion = new SuggestionPanel(textPane, position, subWord, location);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textPane.requestFocusInWindow();
			}
		});
	}

	private void hideSuggestion() {
		if (suggestion != null) {
			suggestion.hide();
		}
	}

	private class SuggestionsKeyListener implements KeyListener {
		@Override
		public void keyTyped(final KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER) {
				if (suggestion != null) {
					if (suggestion.insertSelection()) {
						e.consume();
						final int position = textPane.getCaretPosition();
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								try {
									textPane.getDocument().remove(position - 1, 1);
								} catch (final BadLocationException e) {
									e.printStackTrace();
								}
							}
						});
					}
				}
			}
		}

		@Override
		public void keyReleased(final KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestion != null) {
				suggestion.moveDown();
			} else if (e.getKeyCode() == KeyEvent.VK_UP && suggestion != null) {
				suggestion.moveUp();
			} else if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '.') {
				showSuggestionLater();
			} else if (Character.isWhitespace(e.getKeyChar())) {
				hideSuggestion();
			}
		}

		@Override
		public void keyPressed(final KeyEvent e) {

		}
	}

	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final JFrame frame = new JFrame();
				frame.setTitle("Test frame on two screens");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				final JPanel panel = new JPanel(new BorderLayout());
				final JTextPane textPanes = new JTextPane();
				textPanes.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
				new SuggestionsView(textPanes, true);
				panel.add(textPanes, BorderLayout.CENTER);
				frame.add(panel);
				frame.pack();
				frame.setVisible(true);

			}
		});
	}

}
