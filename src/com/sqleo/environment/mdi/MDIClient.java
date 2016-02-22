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

package com.sqleo.environment.mdi;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import com.sqleo.common.gui.ClientFrame;
import com.sqleo.common.gui.TextView;
import com.sqleo.common.gui.Toolbar;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.common.util.Trie;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.explorer.UoDatasource;
import com.sqleo.querybuilder.syntax._ReservedWords;

public abstract class MDIClient extends ClientFrame {
	private static int counter = 0;
	private int id = -1;
	protected Trie prefixTree;
	private static ClientMetadataExplorer cme;

	public MDIClient(String title) {
		super(title);
		setFrameIcon(null);
		setMaximizable(true);
		setResizable(true);
		setClosable(true);

		if (getName() == null) {
			setName("MDIClient_" + ++counter);
			setTitle((id = counter) + " - " + title);
		}
	}

	protected int getID() {
		return id;
	}
	
	protected static ClientMetadataExplorer getMetadataExplorer(){
		cme = cme!=null ? cme : 
			(ClientMetadataExplorer)Application.window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
		return cme;
	}
	
	public static Color getConnectionBackgroundColor(final String chKey) {
		if(null == chKey){
			return Color.white;
		}else{
			final UoDatasource uoDs = getMetadataExplorer().getControl().getNavigator().findDatasource(chKey);
			return null == uoDs ? Color.white : uoDs.color;
		}
	}

	public abstract JMenuItem[] getMenuActions();

	public abstract Toolbar getSubToolbar();

	protected abstract void setPreferences();

	public Trie getPrefixTree() {
		return prefixTree;
	}
	
	private String getCacheKey(final String chKey,final String schema){
		return chKey+","+schema+",prefixtree";
	}
	
	protected void loadPrefixTree(final String chKey,final String schema) {
		loadPrefixTreeAndView(chKey, schema, null);
	}
	
	protected void loadPrefixTreeAndView(final String chKey,final String schema,final TextView textView) {
		if(null == chKey){
			return;
		}
		if (Preferences.isAutoCompleteEnabled()) {
			final String cacheKey = getCacheKey(chKey, schema);
			final Object cached = Application.session.getColumnCache(cacheKey);
			if(cached!=null){
				prefixTree = (Trie) cached;
				if(textView!=null){
					textView.reloadSuggestionsTrie(getPrefixTree(),chKey);
				}
			}else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						loadAllTableNames(chKey,schema);
						if(prefixTree!=null){
							Application.session.putColumnCache(cacheKey, prefixTree);
							if(textView!=null){
								textView.reloadSuggestionsTrie(getPrefixTree(),chKey);
							}
						}
					}
				});
			}
		}
	}

	private void loadAllTableNames(final String chKey,final String schema) {
		final ConnectionHandler ch = ConnectionAssistant.getHandler(chKey);
		if (ch != null) {
			try {
				String schemaPrefix;
				if(schema!=null){
					schemaPrefix = schema;
				}else {
					schemaPrefix = SQLHelper.getSchemaFromUser(chKey);
					if (null == schemaPrefix) {
						final ArrayList schemaNames = ch.getArrayList("$schema_names");
						if (schemaNames != null) {
							schemaPrefix = (String) schemaNames.get(0);
						}
					}
				}
				final ResultSet rs =
						ch.get()
								.getMetaData()
								.getTables(null, schemaPrefix != null ? schemaPrefix + "%" : "PUBLIC%", "%",
										new String[] { "TABLE", "SYNONYM", "VIEW" });
				if (rs != null) {
					prefixTree = new Trie();
					for (final String word : _ReservedWords.ALL_RESERVED_WORDS) {
						prefixTree.addWord(word.toLowerCase());
					}
					while (rs.next()) {
						final String name = rs.getString(3);
						if (name != null) {
							prefixTree.addWord(name.toLowerCase());
						}
					}
					rs.close();
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
