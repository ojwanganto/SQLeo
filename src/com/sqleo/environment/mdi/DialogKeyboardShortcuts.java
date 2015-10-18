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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.sqleo.common.gui.AbstractDialogModal;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;


public class DialogKeyboardShortcuts extends AbstractDialogModal
{
	private static class Key{
		public Key(String category, String binding, String usage) {
			super();
			this.category = category;
			this.binding = binding;
			this.usage = usage;
		}
		private String category;
		private String binding;
		private String usage;
		private static String SEP = "";
		
		private static Key SEPARATOR() {
			return new Key(SEP,SEP,SEP);
		}
	}

	private class KeyTableModel extends AbstractTableModel {

		private List<Key> keys;

		public KeyTableModel(List<Key> keys) {
			this.keys = new ArrayList<Key>(keys);
		}

		@Override
		public int getRowCount() {
			return keys.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int column) {
			String name = "??";
			switch (column) {
			case 0:
				name = "category";
				break;
			case 1:
				name = "binding";
				break;
			case 2:
				name = "usage";
				break;
			}
			return name;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Key key = keys.get(rowIndex);
			Object value = null;
			switch (columnIndex) {
			case 0:
				value = key.category;
				break;
			case 1:
				value = key.binding;
				break;
			case 2:
				value = key.usage;
				break;
			}
			return value;
		}            
	}  

	public DialogKeyboardShortcuts()
	{
		super(Application.window, 
				I18n.getString("application.menu.help.keyboardShortcuts","Keyboard shortcuts..."),
				610,600		
		);

		final List<Key> keys = new ArrayList<Key>();
		keys.add(new Key("Menu", "ALT-F", "File"));
		keys.add(new Key("Menu", "ALT-A", "Actions"));
		keys.add(new Key("Menu", "ALT-T", "Tools"));
		keys.add(new Key("Menu", "ALT-W", "Window"));
		keys.add(new Key("Menu", "ALT-H", "Help"));

		keys.add(new Key("Menu", "CTRL-1", "Show metadata explorer"));
		keys.add(new Key("Menu", "CTRL-2", "Show command editor"));
		keys.add(new Key("Menu", "CTRL-3", "Show schema comparer"));
		keys.add(new Key("Menu", "CTRL-4", "Show SQL history"));
		keys.add(new Key("Menu", "CTRL-5", "Show data comparer"));
		keys.add(Key.SEPARATOR());

		keys.add(new Key("Syntax", "CTRL-A", "Select all"));
		keys.add(new Key("Syntax", "CTRL-F", "Find"));
		keys.add(new Key("Syntax", "CTRL-C", "Copy"));
		keys.add(new Key("Syntax", "CTRL-V", "Paste"));
		keys.add(new Key("Syntax", "CTRL-Z", "Undo change"));
		keys.add(new Key("Syntax", "CTRL-R", "Redo change"));
		keys.add(Key.SEPARATOR());

		keys.add(new Key("Command editor", "CTRL-ENTER", "Execute current or previous query"));
		keys.add(new Key("Command editor", "CTRL-T", "Clear request area"));
		keys.add(new Key("Command editor", "CTRL-B", "Clear response area"));
		keys.add(new Key("Command editor", "CTRL-F7", "Format selected text"));
		keys.add(new Key("Command editor", "CTRL-SPACE", "Auto-join completion"));
		keys.add(Key.SEPARATOR());
		
		keys.add(new Key("Querybuilder", "CTRL-N", "Open a new query builder window"));
		keys.add(new Key("Content window", "F5", "Refresh"));
		keys.add(Key.SEPARATOR());
		
		
		final KeyTableModel model = new KeyTableModel(keys);
		final JTable table = new JTable(model);
		getContentPane().add(new JScrollPane(table));
	}

	protected void onOpen()
	{
	}
}
