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
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.TextView;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.SQLHistoryData;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.commands.AliasCommand;
import com.sqleo.environment.ctrl.commands.AliasListCommand;
import com.sqleo.environment.ctrl.commands.ClearCommand;
import com.sqleo.environment.ctrl.commands.Command;
import com.sqleo.environment.ctrl.commands.CommandExecutionResult;
import com.sqleo.environment.ctrl.commands.ConnectCommand;
import com.sqleo.environment.ctrl.commands.FormatCommand;
import com.sqleo.environment.ctrl.commands.HelpCommand;
import com.sqleo.environment.ctrl.commands.InputCommand;
import com.sqleo.environment.ctrl.commands.OutputCommand;
import com.sqleo.environment.ctrl.commands.QuitCommand;
import com.sqleo.environment.ctrl.content.MaskExport;
import com.sqleo.environment.ctrl.content.MaskExport.TxtChoice;
import com.sqleo.environment.ctrl.editor.SQLStyledDocument;
import com.sqleo.environment.ctrl.editor.Task;
import com.sqleo.environment.ctrl.editor._TaskSource;
import com.sqleo.environment.ctrl.editor._TaskTarget;
import com.sqleo.environment.mdi.ClientCommandEditor;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.SQLParser;

public class CommandEditor extends BorderLayoutPanel implements _TaskTarget {
	private boolean stopped;

	private Thread queryThread;
	private Task queryTask;

	private TextView request;
	private TextView response;

	private JSplitPane split;
	private ClientContent gridClient;
	private BorderLayoutPanel gridPanel;
	private int splitPanePosition = -1;
	private JTabbedPane tabs;


	protected MutableAttributeSet errorAttributSet;
	protected MutableAttributeSet keycahAttributSet;

	private OutputCommand outputCmd;
	private FormatCommand formatCmd;
	private boolean autoSelectConnectionInCommandMode = false;

	public CommandEditor() {
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(request = new TextView(new SQLStyledDocument(), true));
		gridPanel = new BorderLayoutPanel();

		tabs = new JTabbedPane();
		tabs.add(I18n.getString("commandeditor.console","Console"),response = new TextView(
				new DefaultStyledDocument(), true));
		tabs.add(I18n.getString("commandeditor.resultGrid","Result grid"),gridPanel);
		split.setBottomComponent(tabs);
		
		split.setOneTouchExpandable(true);

		response.setTabSize(4);
		response.setEditable(false);
		initResponse();

		setComponentCenter(split);

		request.getViewActionMap().put("stop-task", new ActionStopTask());
		request.getViewActionMap().put("start-task", new ActionStartTask());
		request.getViewActionMap().put("format-query", new ActionFormatQuery());
		request.getViewInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK),
		"start-task");
		request.getViewInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_MASK),
		"format-query");
		request.addFormatQueryMouseAction();

		getActionMap().setParent(request.getViewActionMap());

		adjustSplitPaneDivider();

		errorAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(errorAttributSet, Color.red);

		keycahAttributSet = new SimpleAttributeSet();
		StyleConstants.setForeground(keycahAttributSet, new Color(0, 128, 0));
		StyleConstants.setBold(keycahAttributSet, true);
	}

	public void append(String text) {
		request.append(text);
	}

	void initResponse() {
		response.setText("Type help to see available commands and their usage\n");
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

	private class ActionFormatQuery extends AbstractAction {
		ActionFormatQuery() {
			putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F7") );
		}
		@Override
		public void actionPerformed(ActionEvent ae) {
			final String requestString = request.getSelectedText();
			if(requestString!=null){
				final String sqlcmd = requestString.length() > 7 ? requestString.toUpperCase().substring(0, 7) : requestString;
				if(sqlcmd.startsWith("SELECT") || sqlcmd.startsWith("UPDATE")){
					try {
						QueryBuilder.useAlwaysQuote	= false;
						final QueryModel model = SQLParser.toQueryModel(requestString);
						final String formattedQuery = model.toString(true);
						request.replaceSelection(requestString.trim().endsWith(";") ? formattedQuery+";" : formattedQuery);
						QueryBuilder.useAlwaysQuote	= Preferences.getBoolean("querybuilder.use-quote",false);
					} catch (IOException e) {
					}
				}
			}
		}
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
				requestString = parseAndSelect(requestString);
			}

			if (requestString == null || requestString.trim().length() == 0) {
				// full
				request.setSelectionStart(0);
				request.setSelectionEnd(request.getText().length());
				requestString = request.getSelectedText();
			}

			if (requestString == null){
				requestString ="";
			}

			executeMultiLineSQL(requestString);
			setEnabled(true);
			final ClientCommandEditor cce = (ClientCommandEditor) Application.window
			.getClient(ClientCommandEditor.DEFAULT_TITLE);
			if(!cce.isGridOutput()){
				transferFocus();
			}
			getActionMap().get("stop-task").setEnabled(!cce.isGridOutput());
			
		}

		private String parseAndSelect(String requestString) {
			try {
				int caretLine = request.getLineOfOffset(request
						.getCaretPosition());
				int totalLines = request.getLineCount();
				int startLine = caretLine, endLine = caretLine;
				int caretStartOffset = request.getLineStartOffset(caretLine);
				int caretEndOffset = request.getLineEndOffset(caretLine);
				String currentLineText = request.getDocument().getText(caretStartOffset,caretEndOffset-caretStartOffset);
				String currentTrimmedLineText = currentLineText!=null ? Text.trimRight(currentLineText):"";
				boolean caretLineEnds = currentTrimmedLineText.endsWith(";");
				boolean caretLineHasText = currentLineText!=null && !currentLineText.trim().isEmpty();
				//find startOffset by searching previous ; in the line or beginning
				int foundStartOffset = -1, foundEndOffset = -1;
				int cntSemicolons = 0 , semiColonIndex = -1, tempStartOffSet = caretStartOffset, tempEndOffset = caretEndOffset;
				boolean oneNonEmptyLineFound = caretLineHasText;
				while(startLine>=0){
					if(startLine<caretLine){
						oneNonEmptyLineFound = oneNonEmptyLineFound || !currentTrimmedLineText.isEmpty();
					}
					tempStartOffSet = request.getLineStartOffset(startLine);
					tempEndOffset = request.getLineEndOffset(startLine);
					currentLineText = request.getDocument().getText(tempStartOffSet,tempEndOffset-tempStartOffSet);
					currentTrimmedLineText = currentLineText!=null ? Text.trimRight(currentLineText):"";
					semiColonIndex = currentTrimmedLineText.lastIndexOf(';');
					if(semiColonIndex >= 0){
						cntSemicolons++;
						if(caretLine == startLine){
							if(currentTrimmedLineText.length()-1 == semiColonIndex){
								final int oneMore = currentTrimmedLineText.lastIndexOf(';', semiColonIndex-1);
								if(oneMore>=0){
									foundStartOffset = tempStartOffSet+oneMore+1;
									break;
								}else{
									startLine--;
									continue;
								}
							}else {
								foundStartOffset = tempStartOffSet+semiColonIndex+1;
								break;
							}
						}
						if(caretLineHasText){
							if(caretLineEnds && cntSemicolons == 2){
								foundStartOffset = tempStartOffSet+semiColonIndex+1;
								break;
							}else if(cntSemicolons == 1){
								foundStartOffset = tempStartOffSet+semiColonIndex+1;
								break;
							}
						}else{
							if(cntSemicolons ==1){
								if(oneNonEmptyLineFound){
									foundStartOffset = tempStartOffSet+semiColonIndex+1;
									break;
								}
								final int oneMore = currentTrimmedLineText.substring(0, semiColonIndex).lastIndexOf(';');
								if(oneMore>=0){
									foundStartOffset = tempStartOffSet+oneMore+1;
									break;
								}
								foundEndOffset = tempStartOffSet+semiColonIndex+1;
							}else if(cntSemicolons == 2){
								foundStartOffset = tempStartOffSet+semiColonIndex+1;
								break;
							}
						}
					}
					startLine--;
				}

				if(caretLineEnds){
					foundEndOffset = caretEndOffset;
				}else if(foundEndOffset<0){
					//find next ; or end  
					endLine++;
					while(endLine < totalLines){
						tempStartOffSet = request.getLineStartOffset(endLine);
						tempEndOffset = request.getLineEndOffset(endLine);
						currentLineText = request.getDocument().getText(tempStartOffSet,tempEndOffset-tempStartOffSet);
						currentTrimmedLineText = currentLineText!=null ? Text.trimRight(currentLineText):"";
						semiColonIndex = currentTrimmedLineText.indexOf(';');
						if(semiColonIndex>=0){
							foundEndOffset = tempStartOffSet+semiColonIndex+1;
							break;
						}
						endLine++;
					}
				}

				request.setSelectionStart(foundStartOffset!=-1?foundStartOffset:0);
				request.setSelectionEnd(foundEndOffset!=-1?foundEndOffset:request.getText().length());
				requestString = request.getSelectedText();
			} catch (BadLocationException e) {
				Application.println(e, false);
			}
			return requestString;
		}

		private void executeMultiLineSQL(String requestString) {
			Boolean PLsql=false;
			String sqlcmd = requestString.length() > 7 ? requestString.toUpperCase().substring(0, 7) : requestString;
			if (sqlcmd.startsWith("DECLARE") || sqlcmd.startsWith("BEGIN") || sqlcmd.startsWith("CREATE") || sqlcmd.startsWith("EXECUTE") || sqlcmd.startsWith("DO $$")) PLsql=true;

			if (requestString != null && requestString.trim().length() > 0 ) {
				requestString = requestString.trim();
				if (!PLsql){
					StringTokenizer st = new StringTokenizer(requestString, "\n"); // split sql separated by "\n"
					StringBuilder sqlBuilder = new StringBuilder();
					while (!stopped && st.hasMoreTokens()) {
						final String line = st.nextToken().trim();
						if (line.startsWith("--") || line.startsWith("//") || line.startsWith("#") || line.startsWith("/* ")) {
							// Line is a comment	
							continue;
						} else if (line.endsWith(";")) {
							sqlBuilder.append(line.substring(0, line.lastIndexOf(";"))).append("\n");
							execute(sqlBuilder.toString());
							sqlBuilder = new StringBuilder();
						}else{
							sqlBuilder.append(line).append("\n");
						}
					}
					if(!stopped && sqlBuilder.toString().length()>0){
						execute(sqlBuilder.toString());
					}
				}else{
					//pl-sql, execute whole selected text
					execute(requestString);
				}
			}
		}

		
		private void execute(final String sql) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						final Command cmd = Application.commandRunner.getCommand(sql.trim());
						if (cmd != null) {
							executeCommand(sql, cmd);
						} else {
							final String sqlAliased = AliasCommand.getAliasedSQL(sql);
							final String sqlToExecute, sqlAlias;
							if(sqlAliased!=null){
								sqlToExecute = sqlAliased;
								sqlAlias = sql;
							}else{
								sqlToExecute = sql;
								sqlAlias = null;
							}
							executeQuery(sqlToExecute, sqlAlias);
						}
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void executeCommand(final String sql,
				final Command cmd) {
			Application.session.addSQLToHistory(new SQLHistoryData(new Date(), "command", "CommandEditor",
					sql));
			if(cmd instanceof ConnectCommand){
				//autoConnectLatestConnection boolean
				setAutoSelectConnectionInCommandMode(true);
			}
			final CommandExecutionResult result = cmd.execute(sql);
			getActionMap().get("stop-task").setEnabled(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			tabs.setSelectedIndex(0);
			if (result.isSuccess()) {
				write( "\n" + sql + "Command executed successfully\n");
				if (cmd instanceof OutputCommand) {
					outputCmd = (OutputCommand) cmd;
					getClient().toggleGridOuptput(outputCmd.gridMode);
				} else if (cmd instanceof FormatCommand) {
					formatCmd = (FormatCommand) cmd;
				} else if (cmd instanceof HelpCommand || cmd instanceof AliasListCommand) {
					write(result.getDetail());
				} else if (cmd instanceof QuitCommand) {
					setAutoSelectConnectionInCommandMode(false);
					outputCmd = null;
					formatCmd = null;
					write(result.getDetail());
				}else if (cmd instanceof ClearCommand) {
					clearResponse();
					gridPanel.removeAll();
				}else if (cmd instanceof InputCommand) {
					final InputCommand inpCmd =(InputCommand)cmd;
					final Thread inp = new Thread(new Runnable(){
						@Override
						public void run() {
							executeMultiLineSQL(inpCmd.inputSql);
						}
					});
					inp.start();
				}
			} else {
				String error = "\n" + sql + "Command "+(result.isFailed()?"failed": "invalid") + "\n";
				if(result.getDetail()!=null){
					error = error + "Reason: "+result.getDetail();
				}
				write(error, true);
			}
		}

		private void executeQuery(final String sql,final String sqlAlias) {
			_TaskSource source = new TaskSource(sql);
			Application.session.addSQLToHistory(new SQLHistoryData(new Date(), source.getHandlerKey(), "CommandEditor",
					sql));
			splitPanePosition = split.getDividerLocation();
			String keycah = "*** " + source.getHandlerKey() + " ***";
			CommandEditor.this.response.append("\n" + keycah);
			int offset = CommandEditor.this.response.getDocument()
			.getLength() - keycah.length();
			response.getDocument().setCharacterAttributes(offset,
					keycah.length(), keycahAttributSet, true);
			if(sqlAlias!=null){
				CommandEditor.this.response.append("\nAlias: "	+ sqlAlias);
			}
			CommandEditor.this.response.append("\n"+ source.getSyntax() + "\n");

			ClientCommandEditor cce = getClient();
			if (cce.isGridOutput() && (sql.toUpperCase().startsWith("SELECT") || sql.toUpperCase().startsWith("SHOW"))) {
				tabs.setSelectedIndex(1);
				CommandEditor.this.response.append("Record(s) sent to data grid\n");
				Vector<Integer> prevColWidths = null;
				if(gridClient!=null){
					gridClient.getControl().getView().cacheColumnWidths();
					if(!gridClient.getControl().getView().getColumnWidths().isEmpty()){
						prevColWidths = new Vector<Integer>(gridClient.getControl().getView().getColumnWidths());
					}
					gridClient.dispose();
				}
				gridClient = new ClientContent(source.getHandlerKey(), sql,true);
				// adds the content and buttons to gridPanel
				gridPanel.removeAll();
				//add content toolbar
				gridPanel.setComponentNorth(gridClient.getContentPane().getComponent(1)); 
				//add content view
				ContentPane content = (ContentPane)gridClient.getContentPane().getComponent(0);
				if(prevColWidths!=null){
					content.getView().setColumnWidths(prevColWidths);
				}
				//remove sql status component
				BorderLayoutPanel pnlSouth = (BorderLayoutPanel)content.getComponent(0);
				pnlSouth.remove(pnlSouth.getComponent(2));
				gridPanel.setComponentCenter(content);

				//append client menu actions inside builder menu actions
				int len1 = cce.getMenuActions().length;
				if(len1 == 6){
					JMenuItem[] allMenuItems = new JMenuItem[len1+1];
					System.arraycopy(cce.getMenuActions(), 0, allMenuItems, 0, len1);
					allMenuItems[len1] = gridClient.getMenuActions()[6];
					cce.setMenuActions(allMenuItems);
				}
				Application.window.menubar.internalFrameActivated(
						new InternalFrameEvent(cce,0));

				adjustSplitPaneDivider();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}else{
				tabs.setSelectedIndex(0);
				adjustSplitPaneDivider();
				queryTask = new Task(source, CommandEditor.this, printSelect() ? cce.getLimitRows() : 0);
				Thread queryThread2 = new Thread(queryTask);
				queryThread2.start();
//				queryTask.run();
			}

		}

	}

	private ClientCommandEditor getClient() {
		return (ClientCommandEditor) Application.window.getClient(ClientCommandEditor.DEFAULT_TITLE);
	}

	private void adjustSplitPaneDivider() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(splitPanePosition != -1) {
					boolean isCollapsed = splitPanePosition > split.getMaximumDividerLocation();
					if(isCollapsed) {
						split.setDividerLocation(0.5d);
					}else {
						split.setDividerLocation(splitPanePosition);
					}
				}else{
					split.setDividerLocation(0.5d);
				}
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
			if(queryTask!=null){
				queryTask.cancel();
			}
			stopped = true;
			setEnabled(false);
			CommandEditor.this.queryThread = null;
			CommandEditor.this.queryTask = null;
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
		getActionMap().get("stop-task").setEnabled(false);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		write(message, error);
	}

	private void write(String message, boolean error) {
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
		return null == outputCmd || null == outputCmd.filename;
	}

	private MaskExport mkp;

	@Override
	public void processResult(ResultSet rs) {
		mkp = mkp != null ? mkp : new MaskExport();
		mkp.rs = rs;
		mkp.setType((short) 3, null, outputCmd.filename, false);
		final MaskExport.TxtChoice txtChoice = (TxtChoice) mkp.eChoice;
		if (formatCmd != null) {
			txtChoice.cbxHeader.setSelected(formatCmd.header);
			txtChoice.cbxCote.setSelected(formatCmd.quote);
			txtChoice.setDefaultDelimiter(formatCmd.delimiter);
		}else{
			txtChoice.cbxHeader.setSelected(true);
			txtChoice.cbxCote.setSelected(false);
			txtChoice.setDefaultDelimiter(";");
		}
		try {
			txtChoice.open(rs.getMetaData(), outputCmd.append);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		mkp.export();
		txtChoice.close();
		write("Record(s): " + NumberFormat.getInstance().format(txtChoice.rowcount) 
				+ " [ bytes: " + NumberFormat.getInstance().format(txtChoice.bytes) + " ]");
	}

	public void setAutoSelectConnectionInCommandMode(
			boolean autoSelectConnectionInCommandMode) {
		this.autoSelectConnectionInCommandMode = autoSelectConnectionInCommandMode;
	}

	public boolean isAutoSelectConnectionInCommandMode() {
		return autoSelectConnectionInCommandMode;
	}
}
