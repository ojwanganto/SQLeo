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
import java.util.regex.PatternSyntaxException;

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
	private String chKey;
	private Connection connection;
	private boolean calledFromCommandEditor = true;
	private String[] previousColumns;

	public SuggestionsView(final JTextPane textPane, final boolean calledFromCommandEditor) {
		this.textPane = textPane;
		this.textPane.addKeyListener(new SuggestionsKeyListener());
		this.calledFromCommandEditor = calledFromCommandEditor;
	}
	
	public void setPrefixTree(final Trie prefixTree,final String chKey){
		this.prefixTree = prefixTree;
		this.chKey = chKey;
		this.connection = ConnectionAssistant.getHandler(chKey).get();
		this.schema = SQLHelper.getSchemaFromUser(chKey);
	}

	private class SuggestionPanel {
		private JList list;
		private final JPopupMenu popupMenu;
		private String subWord;
		private final int insertionPosition;

		private SuggestionPanel(final JTextPane textPane, final int position, final String subWord, final Point location) {
			initPrefixTreeAndConnection();
			this.insertionPosition = position;
			this.subWord = subWord;
			popupMenu = new JPopupMenu();
			popupMenu.removeAll();
			popupMenu.setOpaque(false);
			popupMenu.setBorder(null);
			popupMenu.add(list = createSuggestionList(position, subWord), BorderLayout.CENTER);
			popupMenu.show(textPane, location.x, textPane.getBaseline(0, 0) + location.y);
		}

		private void hide() {
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
				//first try if its alias 
				final String tableNameFromAlias = getTableNameFromAlias(tableOrAliasName);
				if(tableNameFromAlias!=null){
					//means found table name from alias 
					namesStartingWith = SQLHelper.getColumns(chKey, connection, schema, tableNameFromAlias);
				}
				if(null == namesStartingWith || namesStartingWith.length == 0){
					//try as table name 
					namesStartingWith = SQLHelper.getColumns(chKey, connection, schema, tableOrAliasName);
				}
				previousColumns = namesStartingWith;
				this.subWord  = "";
			}else if (previousColumns!=null && subWord.contains(".")) {
				final String colPrefix = subWord.split("\\.")[1];
				namesStartingWith = new String[previousColumns.length];
				int i=0;
				for(final String col : previousColumns){
					if(col.startsWith(colPrefix)){
						namesStartingWith[i]=col; 
						i++;
					}
				}
				this.subWord  = colPrefix;
			}else if (prefixTree != null) {
				final List<String> foundValues = prefixTree.getWordsForPrefix(subWord);
				namesStartingWith = foundValues.toArray(new String[foundValues.size()]);
				previousColumns = null;
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

		private boolean insertSelection() {
			if (list.getSelectedValue() != null) {
				try {
					String selectedSuggestion = ((String) list.getSelectedValue()).substring(subWord.length());
					textPane.getDocument().insertString(insertionPosition, selectedSuggestion, null);
					return true;
				} catch (final BadLocationException e1) {
					e1.printStackTrace();
				}
				hideSuggestion();
			}
			return false;
		}

		private void moveUp() {
			final int index = Math.min(list.getSelectedIndex() - 1, 0);
			selectIndex(index);
		}

		private void moveDown() {
			final int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
			selectIndex(index);
		}

		private void selectIndex(final int index) {
			if(index < 0 ) return;
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
	
	private void showJoinsSuggestionLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				showJoinsSuggestion();
			}

		});
	}

	private String getTableNameFromAlias(final String alias) {
		final String text = textPane.getText();
		final String regex = "(?i)\\s+"+alias+"(\\n+|\\s+|$|,)";
		Matcher matcher = null;
		try{
			matcher = Pattern.compile(regex).matcher(text);
		}catch(PatternSyntaxException e){
			Application.println(e, false);
		}
		if(null == matcher){
			return null;
		}
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
			}else if(_ReservedWords.FROM.equals(word2.toUpperCase())){
				//means given alias is actually table name 
				return alias;
			}else{
				return word2;
			}
		}
		return null;
	}
	private int[] getWordStartEndPositions(final int index, final String text){
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
	
	private void initPrefixTreeAndConnection(){
		if (null == prefixTree) {
			if (calledFromCommandEditor) {
				final MDIClient client = Application.window.getClient(ClientCommandEditor.DEFAULT_TITLE);
				if (client != null) {
					final ClientCommandEditor ce = (ClientCommandEditor) client;
					prefixTree = ce.getPrefixTree();
					chKey = ce.getActiveConnection();
					connection = ConnectionAssistant.getHandler(chKey).get();
					schema = SQLHelper.getSchemaFromUser(chKey);
					removeLafKeyBinding(ce);
				}
			} else {
				final MDIClient[] clients =
						Application.window.getClientsOfConnection(ClientQueryBuilder.DEFAULT_TITLE, null);
				if (clients.length >= 1) {
					final ClientQueryBuilder ce = (ClientQueryBuilder) clients[0];
					prefixTree = ce.getPrefixTree();
					chKey = ce.getQueryBuilder().getConnectionHandlerKey();
					connection =
							ConnectionAssistant.getHandler(chKey).get();
					if (ce.getQueryBuilder().getDiagramLayout().getQueryModel().getSchema()!=null){
						schema = ce.getQueryBuilder().getDiagramLayout().getQueryModel().getSchema();
					}else {
						schema = SQLHelper.getSchemaFromUser(chKey);
					}
					removeLafKeyBinding(ce);
				}
			}
		}
	}
	
	private void removeLafKeyBinding(final MDIClient client){
		//Removes LookAndFeel overriden ctrl+space keybinding to show system menu
		if(client.getActionMap()!=null && client.getActionMap().getParent()!=null)
			client.getActionMap().getParent().remove("showSystemMenu");
	}
	
	private void showJoinsSuggestion() {
		initPrefixTreeAndConnection();
		final String selection = textPane.getSelectedText();
		if(null == selection || selection.length()==0)
			return;
		// format #i, foreignTable fk, primaryTable pk
		// or foreignTable fk, primaryTable pk (default inner join type)
		// or primaryTable pk, foreignTable fk
		final String[] splitSelection = selection.split(",");
		final int length = splitSelection.length; 
		if(length!=2 && length!=3)
			return;
		final String joinType = length == 3 ? splitSelection[0] : "#i";
		final String joinKeyword = getJoinKeyword(joinType);
		if(null == joinKeyword)
			return;
		int startIndex = length-2 ;
		final String fkTableWitAlias = splitSelection[startIndex].trim();
		startIndex++;
		final String fkTable;
		final String fkAlias;
		final String[] fkSplit = fkTableWitAlias.split(" ");
		if(fkSplit.length == 2){
			fkTable = fkSplit[0];
			fkAlias = fkSplit[1];
		}else{
			fkTable = fkTableWitAlias;
			fkAlias = fkTableWitAlias;
		}
		final String pkTableWitAlias = splitSelection[startIndex].trim();
		final String pkTable;
		final String pkAlias;
		final String[] pkSplit = pkTableWitAlias.split(" ");
		if(pkSplit.length == 2){
			pkTable = pkSplit[0];
			pkAlias = pkSplit[1];
		}else {
			pkTable = pkTableWitAlias;
			pkAlias = pkTableWitAlias;
		}
		List<List<String>> joinColumns = SQLHelper.getJoinColumns(chKey, connection, schema, fkTable, pkTable);
		if(joinColumns.isEmpty()){
			//try swapping pk,fk
			joinColumns = SQLHelper.getJoinColumns(chKey, connection, schema, pkTable, fkTable);
			if(joinColumns.isEmpty()){
				return;
			}
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(fkTableWitAlias).append(" ").append(joinKeyword).append(" ");
		builder.append(pkTableWitAlias).append(" ").append("on ");
		boolean first = true;
		for(List<String> temp : joinColumns){
			final String pkCol = temp.get(0);//pkCol
			final String fkCol = temp.get(1);//fkCol
			if(!first){
				builder.append(" and ");
			}else{
				first = false;
			}
			builder.append(fkAlias).append(".").append(fkCol).append("=");
			builder.append(pkAlias).append(".").append(pkCol);
		}
		
		textPane.replaceSelection(builder.toString());
	}
	
	private String getJoinKeyword(final String joinFormat){
		if(joinFormat.equals("#i")){
			return "inner join";
		}else if(joinFormat.equals("#l")){
			return "left outer join";
		}else if(joinFormat.equals("#r")){
			return "right outer join";
		}else{
			return null;
		}
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
			if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE){
				showJoinsSuggestionLater();
			}else if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestion != null) {
				suggestion.moveDown();
			} else if (e.getKeyCode() == KeyEvent.VK_UP && suggestion != null) {
				suggestion.moveUp();
			} else if (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == '.') {
				showSuggestionLater();
			} else if (Character.isWhitespace(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				hideSuggestion();
			}
		}

		@Override
		public void keyPressed(final KeyEvent e) {

		}
	}

//	public static void main(final String[] args) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (final ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (final InstantiationException e) {
//			e.printStackTrace();
//		} catch (final IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (final UnsupportedLookAndFeelException e) {
//			e.printStackTrace();
//		}
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				final JFrame frame = new JFrame();
//				frame.setTitle("Test frame on two screens");
//				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//				final JPanel panel = new JPanel(new BorderLayout());
//				final JTextPane textPanes = new JTextPane();
//				textPanes.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
//				new SuggestionsView(textPanes, true);
//				panel.add(textPanes, BorderLayout.CENTER);
//				frame.add(panel);
//				frame.pack();
//				frame.setVisible(true);
//
//			}
//		});
//	}

}
