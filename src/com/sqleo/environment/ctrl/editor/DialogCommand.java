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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi.ClientCommandEditor;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class DialogCommand extends AbstractDialogConfirm implements _BuildOwner
{
	private String keycah;
	private QueryTokens.Table querytoken;
	
	private int step = 0;

	private JPanel cardPanel;	
	private CardLayout cardLayout;

	private CommandButton btnBack;
	private MaskBrowser browser;
	private JTabbedPane tabs;
	
	public DialogCommand(String keycah, QueryTokens.Table querytoken)
	{
		super(Application.window,"command",640,480);
		
		this.keycah		= keycah;
		this.querytoken = querytoken;
		
		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		tabs.addTab("delete",new BuildDelete(this));
		tabs.addTab("insert",new BuildInsert(this));
		tabs.addTab("update",new BuildUpdate(this));
		
		cardPanel = new JPanel();
		cardPanel.setLayout(cardLayout = new CardLayout());
		cardPanel.add("first", browser = new MaskBrowser());
		cardPanel.add("last", tabs);
		getContentPane().add(cardPanel);

		btnBack = insertButton(1,"< back");		
	}
	
	protected void setBarEnabled(boolean b)
	{
		super.setBarEnabled(b);
		btnBack.setEnabled(step!=0);
	}
	
	protected void onOpen()
	{
		if(querytoken!=null)
		{
			loadBuilders();
			showLast();
		}
		else
		{
			loadBrowser();
			showFirst();
		}
	}

	protected boolean onConfirm()
	{
		if(step == 0)
		{
			querytoken = browser.getSelectedItem();
			if(querytoken != null)
			{
				loadBuilders();
				showLast();
			}
			else
			{
				Application.alert(Application.PROGRAM,"Nothing selected!");
			}
		}
		else
		{
			int idx = tabs.getSelectedIndex();
			
			ClientCommandEditor client = (ClientCommandEditor)Application.window.getClient(ClientCommandEditor.DEFAULT_TITLE);
			client.getControl().append(((BuildBasePane)tabs.getComponentAt(idx)).getSyntax());
		}
		
		return false;
	}
	
	private void showFirst()
	{
		step = 0;
		btnBack.setEnabled(false);
		btnConfirm.setText("next >");
		cardLayout.show(cardPanel,"first");
	}
	
	private void showLast()
	{
		step = 1;
		btnBack.setEnabled(true);
		btnConfirm.setText("paste");
		cardLayout.show(cardPanel,"last");
	}
	
	public String getIdentifierQuoteString()
	{
		return ConnectionAssistant.getHandler(keycah).getObject("$identifierQuoteString").toString();
	}
	
	public String getTable()
	{
		return SQLFormatter.ensureQuotes(querytoken.getIdentifier(),this.getIdentifierQuoteString(),!QueryBuilder.useAlwaysQuote);
	}

	private void loadBrowser()
	{
		try
		{
			browser.setConnection(ConnectionAssistant.getHandler(keycah));
		}
		catch(SQLException e)
		{
			Application.println(e,false);
		}
	}
	
	private void loadBuilders()
	{
		((BuildBasePane)tabs.getComponentAt(0)).clear();
		((BuildBasePane)tabs.getComponentAt(1)).clear();
		((BuildBasePane)tabs.getComponentAt(2)).clear();
		
		try
		{
			ConnectionHandler ch = ConnectionAssistant.getHandler(keycah);
			DatabaseMetaData dbmetadata = ch.get().getMetaData();
			
			String catalog = querytoken.getSchema() == null ? null : dbmetadata.getConnection().getCatalog();
			ResultSet rsColumns = dbmetadata.getColumns(catalog, querytoken.getSchema(), querytoken.getName(), "%");
			while(rsColumns.next())
			{
				QueryTokens.Column qColumn = new QueryTokens.Column(null,rsColumns.getString(4));
				((BuildBasePane)tabs.getComponentAt(0)).add(qColumn);
				((BuildBasePane)tabs.getComponentAt(1)).add(qColumn);
				((BuildBasePane)tabs.getComponentAt(2)).add(qColumn);
			}
			rsColumns.close();
		}
		catch (SQLException e)
		{
			Application.println(e,false);
		}
		finally
		{
			((BuildBasePane)tabs.getComponentAt(0)).fireQueryChanged();
			((BuildBasePane)tabs.getComponentAt(1)).fireQueryChanged();
			((BuildBasePane)tabs.getComponentAt(2)).fireQueryChanged();
		}
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnBack)
		{
			loadBrowser();
			showFirst();
		}
		else
			super.actionPerformed(ae);
	}
}
