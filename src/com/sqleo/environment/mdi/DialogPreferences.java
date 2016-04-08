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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.text.DecimalFormat;

import java.util.Locale;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.Toolbar;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.LocaleAdapter;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.querybuilder.QueryBuilder;


public class DialogPreferences extends AbstractDialogConfirm {
	private JCheckBox optQbAutoJoin;
	private JCheckBox optQbAutoAlias;
	private JCheckBox optQbUseQuote;
	private JCheckBox optQbUseSchema;
	private JCheckBox optQbLoadAtOnce;
	private JCheckBox optQbSelectAll;
	private JRadioButton optQbRelrenderArc;
	private JRadioButton optQbRelrenderLinear; 
	private JCheckBox optQbSavePosInSQL;

	private JLabel jLabelLanguage = new JLabel();
	private JTextArea jLabelAutoCommitInfo = new JTextArea();
	private JComboBox jComboBoxLanguage = new JComboBox();
	private JCheckBox jCheckBoxTrace = new JCheckBox();

	DecimalFormat decimalFormat = new DecimalFormat("######0");
	private NumberFormatter numberFormatter = new NumberFormatter(decimalFormat);
	private JTextField jTextFieldFontSize = new JFormattedTextField(numberFormatter);
	private JTextField jTextFieldIconSize = new JFormattedTextField(numberFormatter);
	private JTextField jTextFieldMaxQueriesInHistory = new JFormattedTextField(numberFormatter);
	private JTextField jTextFieldMaxRowFetchSize = new JFormattedTextField(numberFormatter);
	private JTextField jTextFieldMaxColSize = new JFormattedTextField(numberFormatter);
	private JTextField jTextFieldCopyOpenExt = new JTextField();
	
	
	private JCheckBox jCheckBoxAutoCommit = new JCheckBox();
	private JCheckBox jCheckBoxAutoSavePoint = new JCheckBox();
	private JCheckBox jCheckBoxPopulateFilter = new JCheckBox();

	private JCheckBox jCheckBoxAutoComplete = new JCheckBox();
	private JCheckBox jCheckBoxAskBeforeExit = new JCheckBox();
	private JCheckBox jCheckBoxCheckUpdate = new JCheckBox();
	private JCheckBox jCheckBoxAutoSelectConnection = new JCheckBox();

	public static final String CONTENT_MAX_ROWS_FETCH_SIZE_KEY = "content.maxrowfetchsize";
	public static final String CHECK_FOR_UPDATE_KEY = "application.checkforupdate";
	public static final String ASK_BEFORE_EXIT_KEY = "application.askBeforeExit";
	public static final String AUTO_COMPLETE_KEY = "application.autoComplete";
	public static final String AUTO_SELECT_CON_KEY = "application.commandEditor.autoSelectConnection";
	public static final String QB_RELATION_RENDER_ARC_KEY = "querybuilder.isArcRelationRender";
	public static final String FONT_SIZE_PERCENTAGE = "application.fontSizePercentage";
	public static final String QB_SAVE_POS_IN_SQL = "querybuilder.savePosInSQL";
	public static final String MAX_QUERIES_IN_HISTORY = "application.maxQueriesInHistory";
	public static final Integer DEFAULT_MAX_QUERIES_IN_HISTORY = 10;
	public static final Integer DEFAULT_FONT_PERCENT = 110;
	public static final String ICON_SIZE_PERCENTAGE = "application.iconSizePercentage";
	public static final Integer DEFAULT_ICON_PERCENT = 160;
	public static final String COPY_OPEN_FILE_EXTENSION = "application.copyAndOpenFileExtension";
	public static final String POPULATE_FILTER_VALUES = "application.populateFilter";

	public DialogPreferences() {
		super(Application.window, Application.PROGRAM + ".preferences", 360,
				480);

		JPanel pnlQB = new JPanel(new GridLayout(0, 1));
		pnlQB.setBorder(new EmptyBorder(10, 5, 90, 5));

		pnlQB.add(optQbAutoJoin = new JCheckBox(I18n.getString(
				"application.preferences.autoJoin", "Auto join"), Preferences
				.getBoolean("querybuilder.auto-join", true)));
		pnlQB.add(optQbAutoAlias = new JCheckBox(I18n.getString(
				"application.preferences.autoAlias", "Auto alias"), Preferences
				.getBoolean("querybuilder.auto-alias", true)));
		pnlQB.add(new JSeparator());
		pnlQB.add(optQbUseQuote = new JCheckBox(I18n.getString(
				"application.preferences.alwaysQuoteIdentifiers",
				"Always quote identifiers"), Preferences.getBoolean(
				"querybuilder.use-quote", true)));
		pnlQB.add(optQbUseSchema = new JCheckBox(I18n.getString(
				"application.preferences.useSchemaName",
				"Use schema name in syntax definition"), Preferences
				.getBoolean("querybuilder.use-schema", true)));
		pnlQB.add(new JSeparator());
		pnlQB.add(optQbLoadAtOnce = new JCheckBox(I18n.getString(
				"application.preferences.loadObjectsAtOnce",
				"Load table objects list at once"), Preferences.getBoolean(
				"querybuilder.load-objects-at-once", true)));
		pnlQB.add(optQbSelectAll = new JCheckBox(I18n.getString(
				"application.preferences.selectAllColumns",
				"Auto select all colmuns"), Preferences.getBoolean(
				"querybuilder.select-all-columns", true)));
		pnlQB.add(new JSeparator());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,2));
		boolean relationRendering = Preferences.getBoolean(QB_RELATION_RENDER_ARC_KEY , true);
		optQbRelrenderArc  = new JRadioButton(I18n.getString(
				"application.preferences.relationsRenderArc",
				"Arc"), relationRendering);
		optQbRelrenderLinear  = new JRadioButton(I18n.getString(
				"application.preferences.relationsRenderLinear",
				"Linear"), !relationRendering);
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(optQbRelrenderArc);
		radioGroup.add(optQbRelrenderLinear);
		JLabel jLabelRelRender = new JLabel(I18n.getString("application.preferences.relationsRender","Relations render"));
		buttonPanel.add(jLabelRelRender);
		buttonPanel.add(optQbRelrenderArc);
		buttonPanel.add(optQbRelrenderLinear);
		pnlQB.add(buttonPanel);
		
		pnlQB.add(optQbSavePosInSQL = new JCheckBox(I18n.getString(
				"application.preferences.qb.savePosInSQL",
				"Save positions in SQL"), Preferences.getBoolean(
				QB_SAVE_POS_IN_SQL, false)));

		jLabelLanguage.setText(I18n.getString(
				"application.preferences.language", "Language"));
		jLabelAutoCommitInfo.setText(I18n.getString(
						"application.preferences.autoCommitWarning",
						"Warning:Commit in a window affects all opened windows for a connection when auto commit disabled!")		
		);
		jLabelAutoCommitInfo.setEditable(false);
		jLabelAutoCommitInfo.setForeground(Color.RED);
		jLabelAutoCommitInfo.setFont(new Font(jLabelAutoCommitInfo.getFont().getName(),Font.BOLD,jLabelAutoCommitInfo.getFont().getSize()));
		jLabelAutoCommitInfo.setLineWrap(true);
		jLabelAutoCommitInfo.setWrapStyleWord(true);
		jLabelAutoCommitInfo.setCaretPosition(0);
		JScrollPane scrollV = new JScrollPane (jLabelAutoCommitInfo);
		
		// Add all the available languages...
		java.util.List list = I18n.getListOfAvailLanguages();
		list.add(0, Locale.ENGLISH);


		int selectedItem = 0;
		for (int i = 0; i < list.size(); ++i) {
			java.util.Locale loc = (java.util.Locale) list.get(i);
			jComboBoxLanguage.addItem(new LocaleAdapter(loc));

			if (I18n.getCurrentLocale().toString().equals(loc.toString())) {
				selectedItem = i;
			}
		}
		jComboBoxLanguage.setSelectedIndex(selectedItem);

		jCheckBoxTrace.setText(I18n.getString("application.preferences.trace",
				"Trace log (need restart)"));
		jCheckBoxTrace.setSelected(Preferences.getBoolean("application.trace",
				true));
		jCheckBoxCheckUpdate.setText(I18n.getString("application.preferences.checkforupdate",
		"Check for update on startup"));
		jCheckBoxCheckUpdate.setSelected(Preferences.getBoolean(CHECK_FOR_UPDATE_KEY,
		true));
		jCheckBoxAutoComplete.setText(I18n.getString(
				"application.preferences.autoComplete", "Auto complete"));
		jCheckBoxAutoComplete.setSelected(Preferences.getBoolean(
				AUTO_COMPLETE_KEY, false));
		jCheckBoxAutoSelectConnection.setText(I18n.getString(
				AUTO_SELECT_CON_KEY, "Auto select latest connection"));
		jCheckBoxAutoSelectConnection.setSelected(Preferences.getBoolean(
				AUTO_SELECT_CON_KEY, false));
		jCheckBoxAskBeforeExit.setText(I18n.getString(
				"application.preferences.askBeforeExit", "Always ask before exit"));
		jCheckBoxAskBeforeExit.setSelected(Preferences.getBoolean(
				ASK_BEFORE_EXIT_KEY, true));
		jCheckBoxAutoCommit.setText(I18n.getString(
				"application.preferences.autoCommit", "Auto commit"));
		jCheckBoxAutoCommit.setSelected(Preferences.getBoolean(
				"application.autoCommit", false));
		jCheckBoxAutoCommit.addItemListener(new ItemListener() {

		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED){
		        	boolean isAutoCommitON = e.getStateChange() == ItemEvent.SELECTED;
		        	//update all connections autoCommit flags
		        	for(String keych : (Set<String>)ConnectionAssistant.getHandlers() ){
		        			try {
		        				ConnectionHandler ch = ConnectionAssistant.getHandler(keych);
				        		if(ch!=null){
				        			ch.get().setAutoCommit(isAutoCommitON);
				        		}
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
		        	}
		        	// toggle commit-rollback buttons on toolbars of commandEditor and contentpane windows.
		        	ClientCommandEditor cce = (ClientCommandEditor) Application.window
					.getClient(ClientCommandEditor.DEFAULT_TITLE);
		        	if(cce!=null){
		        		Toolbar ceToolbar = cce.getSubToolbar();
		        		ceToolbar.getActionMap().get("action-commit").setEnabled(!isAutoCommitON);
		        		ceToolbar.getActionMap().get("action-rollback").setEnabled(!isAutoCommitON);
		        	}	
		        	
		        	MDIClient[] contentClients = Application.window.getClientsOfConnection(ClientContent.DEFAULT_TITLE,null);
		        	for(int i=0; i<contentClients.length; i++){
			        	ClientContent cct = (ClientContent)contentClients[i];
			        	Toolbar cctToolbar = cct.getSubToolbar();
			        	cctToolbar.getActionMap().get("action-commit").setEnabled(!isAutoCommitON);
			        	cctToolbar.getActionMap().get("action-rollback").setEnabled(!isAutoCommitON);
		        	}
		        	MDIClient[] previewClients = Application.window.getClientsOfConnection(ClientContent.PREVIEW_TITLE,null);
		        	for(int i=0; i<previewClients.length; i++){
			        	ClientContent pct = (ClientContent)previewClients[i];
			        	Toolbar pctToolbar = pct.getSubToolbar();
			        	pctToolbar.getActionMap().get("action-commit").setEnabled(!isAutoCommitON);
			        	pctToolbar.getActionMap().get("action-rollback").setEnabled(!isAutoCommitON);
		        	}
		        	
		        	if (isAutoCommitON){
			        	jCheckBoxAutoSavePoint.setSelected(false);
		        	}
		        	
		        }
		    }
		});

		jCheckBoxAutoSavePoint.setText(I18n.getString(
				"application.preferences.autoSavePoint", "Auto savepoint"));
		jCheckBoxAutoSavePoint.setSelected(Preferences.getBoolean(
				"application.autoSavePoint", false));
		jCheckBoxAutoSavePoint.addItemListener(new ItemListener() {

		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED){
		        	boolean isAutoSavepointON = e.getStateChange() == ItemEvent.SELECTED;
		        	if (isAutoSavepointON){
		        		jCheckBoxAutoCommit.setSelected(false);
		        	}
		        }
		    }
		});

		jCheckBoxPopulateFilter.setText(I18n.getString(
				"application.preferences.populateFilter", "Populate filter values"));
		jCheckBoxPopulateFilter.setSelected(Preferences.getBoolean(POPULATE_FILTER_VALUES, true));

		
		JPanel pnlGeneral = new JPanel(new GridLayout(0, 1));

		JPanel pnlLanguage = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlLanguage.add(jLabelLanguage);
		pnlLanguage.add(jComboBoxLanguage);
		pnlGeneral.add(pnlLanguage);
		
		// add font size percentage
		JPanel pnlFont = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlFont.add(new JLabel(I18n.getString(
				FONT_SIZE_PERCENTAGE, "Font size scaling % (need restart)")));
		jTextFieldFontSize.setText(String.valueOf(Preferences.getInteger(
				FONT_SIZE_PERCENTAGE, DialogPreferences.DEFAULT_FONT_PERCENT)));
		jTextFieldFontSize.setColumns(5);
		pnlFont.add(jTextFieldFontSize);
		pnlGeneral.add(pnlFont);
	
		// add icon size percentage
		JPanel pnlIcon = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlIcon.add(new JLabel(I18n.getString(
				ICON_SIZE_PERCENTAGE, "Icon size scaling % (need restart)")));
		jTextFieldIconSize.setText(String.valueOf(Preferences.getInteger(
				ICON_SIZE_PERCENTAGE, DialogPreferences.DEFAULT_ICON_PERCENT)));
		jTextFieldIconSize.setColumns(5);
		pnlIcon.add(jTextFieldIconSize);
		pnlGeneral.add(pnlIcon);

		// add max queries in history
		JPanel pnlMaxQueries = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlMaxQueries.add(new JLabel(I18n.getString(
				MAX_QUERIES_IN_HISTORY, "Max queries in history")));
		jTextFieldMaxQueriesInHistory.setText(String.valueOf(Preferences.getInteger(
				MAX_QUERIES_IN_HISTORY, DEFAULT_MAX_QUERIES_IN_HISTORY)));
		jTextFieldMaxQueriesInHistory.setColumns(10);
		pnlMaxQueries.add(jTextFieldMaxQueriesInHistory);
		pnlGeneral.add(pnlMaxQueries);
		
		pnlGeneral.add(jCheckBoxTrace);
		pnlGeneral.add(jCheckBoxCheckUpdate);
		pnlGeneral.add(jCheckBoxAskBeforeExit);
		pnlGeneral.add(scrollV);
		pnlGeneral.add(jCheckBoxAutoCommit);
		pnlGeneral.add(jCheckBoxAutoSavePoint);
		pnlGeneral.add(jCheckBoxPopulateFilter);

		JPanel pnlCommand = new JPanel(new GridLayout(0, 1));
		pnlCommand.setBorder(new EmptyBorder(10, 5, 150, 5));
		
		//add max colums size
		JPanel pnlMaxColumns = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlMaxColumns.add(new JLabel(I18n.getString(
				"application.preferences.maxcolsize", "Maximum column size:")));
		jTextFieldMaxColSize.setText(String.valueOf(Preferences.getInteger(
				"editor.maxcolsize", 50)));
		jTextFieldMaxColSize.setColumns(10);
		pnlMaxColumns.add(jTextFieldMaxColSize);
		pnlCommand.add(pnlMaxColumns);
		
		// add max rows fetch size
		JPanel pnlMaxRows = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlMaxRows.add(new JLabel(I18n.getString(
				"application.preferences.maxrowfetchsize", "Maximum rows fetch size:")));
		jTextFieldMaxRowFetchSize.setText(String.valueOf(Preferences.getInteger(
				CONTENT_MAX_ROWS_FETCH_SIZE_KEY, 100)));
		jTextFieldMaxRowFetchSize.setColumns(10);
		pnlMaxRows.add(jTextFieldMaxRowFetchSize);
		pnlCommand.add(pnlMaxRows);
		
		pnlCommand.add(jCheckBoxAutoComplete);
		pnlCommand.add(jCheckBoxAutoSelectConnection);
		
		// add copyfileextension
		JPanel pnlCopyFileExt = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlCopyFileExt.add(new JLabel(I18n.getString(
				"application.copyAndOpenFileExtension", "Copy and open in editor file extension:")));
		jTextFieldCopyOpenExt.setText(Preferences.getString(COPY_OPEN_FILE_EXTENSION, "txt"));
		jTextFieldCopyOpenExt.setColumns(4);
		pnlCopyFileExt.add(jTextFieldCopyOpenExt);
		pnlCommand.add(pnlCopyFileExt);

		JTabbedPane options = new JTabbedPane();
		options.addTab("General", pnlGeneral);
		options.addTab("Command editor", pnlCommand);
		options.addTab("Query builder", pnlQB);

		getContentPane().add(options);
	}

	@Override
	protected void onOpen() {
		Object[] items = Application.window.menubar.getMenu(
				MDIMenubar.IDX_WINDOW).getMenuComponents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof JMenuItem) {
				String txt = ((JMenuItem) items[i]).getText();
				if (txt.indexOf(" - QUERY : ") != -1) {
					optQbAutoAlias.setEnabled(false);
					optQbAutoJoin.setEnabled(false);
					optQbUseQuote.setEnabled(false);
					optQbUseSchema.setEnabled(false);
					optQbLoadAtOnce.setEnabled(false);
					optQbSelectAll.setEnabled(false);
					optQbRelrenderArc.setEnabled(false);
					optQbRelrenderLinear.setEnabled(false);
					optQbSavePosInSQL.setEnabled(false);
					break;
				}
			}
		}
	}

	@Override
	protected boolean onConfirm() {
		QueryBuilder.autoJoin = optQbAutoJoin.isSelected();
		QueryBuilder.autoAlias = optQbAutoAlias.isSelected();
		QueryBuilder.useAlwaysQuote = optQbUseQuote.isSelected();
		QueryBuilder.loadObjectsAtOnce = optQbLoadAtOnce.isSelected();
		QueryBuilder.selectAllColumns = optQbSelectAll.isSelected();
		
		Preferences.set(QB_RELATION_RENDER_ARC_KEY,
				new Boolean(optQbRelrenderArc.isSelected()));
		Preferences.set("querybuilder.auto-join",
				new Boolean(optQbAutoJoin.isSelected()));
		Preferences.set("querybuilder.auto-alias",
				new Boolean(optQbAutoAlias.isSelected()));
		Preferences.set("querybuilder.use-quote",
				new Boolean(optQbUseQuote.isSelected()));
		Preferences.set("querybuilder.use-schema",
				new Boolean(optQbUseSchema.isSelected()));
		Preferences.set("querybuilder.load-objects-at-once", new Boolean(
				optQbLoadAtOnce.isSelected()));
		Preferences.set("querybuilder.select-all-columns", new Boolean(
				optQbSelectAll.isSelected()));
		Preferences.set(QB_SAVE_POS_IN_SQL, new Boolean(
				optQbSavePosInSQL.isSelected()));

		if (jComboBoxLanguage.getSelectedIndex() >= 0) {
			LocaleAdapter la = (LocaleAdapter) jComboBoxLanguage
					.getSelectedItem();
			I18n.setCurrentLocale(la.getLocale());
			Preferences.set("app.locale", la.getLocale().toString());
		}

		Preferences.set("application.trace",
				new Boolean(jCheckBoxTrace.isSelected()));
		Preferences.set(CHECK_FOR_UPDATE_KEY,
				new Boolean(jCheckBoxCheckUpdate.isSelected()));
		Preferences.set("application.autoCommit", new Boolean(
				jCheckBoxAutoCommit.isSelected()));
		Preferences.set("application.autoSavePoint", new Boolean(
				jCheckBoxAutoSavePoint.isSelected()));
		Preferences.set(POPULATE_FILTER_VALUES, new Boolean(
				jCheckBoxPopulateFilter.isSelected()));
		Preferences.set(ASK_BEFORE_EXIT_KEY, new Boolean(
				jCheckBoxAskBeforeExit.isSelected()));
		Preferences.set(AUTO_COMPLETE_KEY, new Boolean(
				jCheckBoxAutoComplete.isSelected()));
		Preferences.set(AUTO_SELECT_CON_KEY, new Boolean(
				jCheckBoxAutoSelectConnection.isSelected()));
		Preferences.set("editor.maxcolsize",
				new Integer(jTextFieldMaxColSize.getText()));
		Preferences.set(CONTENT_MAX_ROWS_FETCH_SIZE_KEY,
				new Integer(jTextFieldMaxRowFetchSize.getText()));
		final Integer fontSizeVal = new Integer(jTextFieldFontSize.getText());
		Preferences.set(FONT_SIZE_PERCENTAGE, fontSizeVal>200 ? 200 : fontSizeVal);
		final Integer iconSizeVal = new Integer(jTextFieldIconSize.getText());
		Preferences.set(ICON_SIZE_PERCENTAGE, iconSizeVal>400 ? 400 : iconSizeVal);

		Preferences.set(MAX_QUERIES_IN_HISTORY, new Integer(jTextFieldMaxQueriesInHistory.getText()));
		Preferences.set(COPY_OPEN_FILE_EXTENSION, jTextFieldCopyOpenExt.getText());

		return true;
	}
}