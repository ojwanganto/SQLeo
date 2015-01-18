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

package com.sqleo.environment.ctrl;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.TextView;
import com.sqleo.common.util.SQLHistoryData;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.editor.SQLStyledDocument;
import com.sqleo.environment.ctrl.editor.Task;
import com.sqleo.environment.ctrl.editor._TaskSource;
import com.sqleo.environment.ctrl.editor._TaskTarget;
import com.sqleo.environment.mdi.ClientCommandEditor;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.environment.mdi.ClientQueryBuilder;
import com.sqleo.environment.mdi.DialogPreferences;


public class CommandEditor extends BorderLayoutPanel implements _TaskTarget {
	private boolean stopped;

	private Thread queryThread;

	private TextView request;
	private TextView response;
	
	private JSplitPane split;
	private ClientContent gridClient;
	private BorderLayoutPanel gridPanel;

	protected MutableAttributeSet errorAttributSet;
	protected MutableAttributeSet keycahAttributSet;

	public CommandEditor() {
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(request = new TextView(new SQLStyledDocument(), true));
		split.setBottomComponent(response = new TextView(
				new DefaultStyledDocument(), true));
		split.setOneTouchExpandable(true);
		gridPanel = new BorderLayoutPanel();

		response.setTabSize(4);
		response.setEditable(false);

		setComponentCenter(split);

		request.getViewActionMap().put("stop-task", new ActionStopTask());
		request.getViewActionMap().put("start-task", new ActionStartTask());
		request.getViewInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK),
				"start-task");

		getActionMap().setParent(request.getViewActionMap());

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent evt) {
				adjustSplit();
			}
		});

		errorAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(errorAttributSet, Color.red);

		keycahAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(keycahAttributSet, new Color(0, 128, 0));
		StyleConstants.setBold(keycahAttributSet, true);
	}

	public void append(String text) {
		request.append(text);
	}

	public void clearResponse() {
		response.setText(null);
		request.requestFocus();
	}

	public TextView getRequestArea() {
		request.requestFocus();
		return request;
	}

	public TextView getResponseArea() {
		response.requestFocus();
		return response;
	}

	public String getSelectedText() {
		return request.getSelectedText();
	}

	public SQLStyledDocument getDocument() {
		return (SQLStyledDocument) request.getDocument();
	}

	public void setDocument(SQLStyledDocument doc) {
		this.request.setDocument(doc);
		this.request.setCaretPosition(0);
		this.request.requestFocus();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// Actions
	// /////////////////////////////////////////////////////////////////////////////
	private class ActionStartTask extends AbstractAction implements Runnable {
		ActionStartTask() {
			putValue(SMALL_ICON,
					Application.resources.getIcon(Application.ICON_EDITOR_RUN));
			putValue(SHORT_DESCRIPTION, "launch");
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			getActionMap().get("stop-task").setEnabled(true);

			setEnabled(false);
			stopped = false;

			queryThread = new Thread(this);
			queryThread.start();
		}

		@Override
		public void run() {
			String requestString = request.getSelectedText();


			if (requestString == null || requestString.trim().length() == 0) {
				// line
				try {
					int line = request.getLineOfOffset(request
							.getCaretPosition());

					request.setSelectionStart(request.getLineStartOffset(line));
					request.setSelectionEnd(request.getLineEndOffset(line));
					requestString = request.getSelectedText();
				} catch (BadLocationException e) {
					Application.println(e, false);
				}
			}

			if (requestString == null || requestString.trim().length() == 0) {
				// full
				request.setSelectionStart(0);
				request.setSelectionEnd(request.getText().length());
				requestString = request.getSelectedText();
			}

			Boolean PLsql=false;
			String sqlcmd = requestString.length() > 7 ? requestString.toUpperCase().substring(0, 7) : requestString;
			if (sqlcmd.startsWith("DECLARE") || sqlcmd.startsWith("BEGIN") || sqlcmd.startsWith("CREATE") || sqlcmd.startsWith("EXECUTE")) PLsql=true;
			
			if (requestString != null && requestString.trim().length() > 0 ) {
				requestString = requestString.trim();
				if (!PLsql){
					StringTokenizer st = new StringTokenizer(requestString, "\n"); // split sql separated by "\n"
					StringBuilder sqlBuilder = new StringBuilder();
					while (!stopped && st.hasMoreTokens()) {
						final String line = st.nextToken();
						if (line.startsWith("--") || line.startsWith("//") || line.startsWith("#")) {
		                    // Line is a comment	
							continue;
		                } else if (line.endsWith(";")) {
		                	sqlBuilder.append(line.substring(0, line.lastIndexOf(";"))).append("\n");
		                	executeCommandQuery(sqlBuilder.toString());
		                	sqlBuilder = new StringBuilder();
		                }else{
		                	sqlBuilder.append(line).append("\n");
		                }
					}
					if(!stopped && sqlBuilder.toString().length()>0){
						executeCommandQuery(sqlBuilder.toString());
					}
				}else{
					//pl-sql, execute whole selected text
					executeCommandQuery(requestString);
				}
			}
			setEnabled(true);
			transferFocus();

			getActionMap().get("stop-task").setEnabled(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		private void executeCommandQuery(final String sql) {
			_TaskSource source = new TaskSource(sql);
			Application.session.addSQLToHistory(new SQLHistoryData(new Date(), 
					source.getHandlerKey(), "CommandEditor", sql));
			ClientCommandEditor cce = (ClientCommandEditor) Application.window
					.getClient(ClientCommandEditor.DEFAULT_TITLE);

			if(cce.isGridOutput() && sql.toUpperCase().startsWith("SELECT")){
				if(gridClient!=null){
					gridClient.dispose();
				}
				gridClient = new ClientContent(source.getHandlerKey(), sql,true);
				// adds the content and buttons to gridPanel
				gridPanel.removeAll();
				//add content toolbar
				gridPanel.setComponentNorth(gridClient.getContentPane().getComponent(1)); 
				//add content view
				ContentPane content = (ContentPane)gridClient.getContentPane().getComponent(0);
				
				//remove sql status component
				BorderLayoutPanel pnlSouth = (BorderLayoutPanel)content.getComponent(0);
				pnlSouth.remove(pnlSouth.getComponent(2));
				gridPanel.setComponentCenter(content);
				split.setBottomComponent(gridPanel);
				adjustSplit();
			}else{
				adjustSplit();
				split.setBottomComponent(response);
				String keycah = "*** " + source.getHandlerKey() + " ***";
				CommandEditor.this.response.append("\n" + keycah);
				int offset = CommandEditor.this.response.getDocument()
						.getLength() - keycah.length();
				response.getDocument().setCharacterAttributes(offset,
						keycah.length(), keycahAttributSet, true);
				CommandEditor.this.response.append("\n"
						+ source.getSyntax() + "\n");
				new Task(source, CommandEditor.this, cce.getLimitRows())
						.run();
			}
			
		}
		
	}
	
	private void adjustSplit(){
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
				split.setDividerLocation(0.5);
	    		split.validate();
            }
		 });
	}

	private class ActionStopTask extends AbstractAction {
		ActionStopTask() {
			putValue(SMALL_ICON,
					Application.resources.getIcon(Application.ICON_STOP));
			putValue(SHORT_DESCRIPTION, "stop");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			stopped = true;
			setEnabled(false);
			CommandEditor.this.queryThread = null;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// _TaskTarget
	// /////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean continueRun() {
		return queryThread != null;
	}

	@Override
	public void onTaskFinished(String message, boolean error) {
		write(message);
		if (error) {
			int offset = response.getDocument().getLength() - message.length();
			response.getDocument().setCharacterAttributes(offset,
					message.length(), errorAttributSet, true);
		}
		response.append("\n");
	}

	@Override
	public void write(String text) {
		response.append(text);
		try {
			int line = response.getLineCount();
			int off = response.getLineStartOffset(line - 1);

			response.setCaretPosition(off);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////
	// TaskSource
	// /////////////////////////////////////////////////////////////////////////////
	private class TaskSource implements _TaskSource {
		private String query;

		private TaskSource(String query) {
			this.query = query;
		}

		@Override
		public String getHandlerKey() {
			ClientCommandEditor client = (ClientCommandEditor) Application.window
					.getClient(ClientCommandEditor.DEFAULT_TITLE);
			return client.getActiveConnection();
		}

		@Override
		public String getSyntax() {
			return query;
		}
	}

	@Override
	public boolean printSelect() {
		return true;
	}

	@Override
	public void processResult(ResultSet rs) {
		//Nothing to process as printSelect = true 
	}
}
