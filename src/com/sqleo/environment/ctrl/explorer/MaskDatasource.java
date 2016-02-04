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

package com.sqleo.environment.ctrl.explorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

import com.sqleo.common.util.I18n;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;



public class MaskDatasource extends JPanel
{
	private JTextField txtName;
	private JTextField txtUrl;
	private JTextField txtUid;
	private JPasswordField txtPwd;
	
	private JCheckBox cbxRemember;
	private JCheckBox cbxReadonly;
	private JCheckBox cbxAutoconnect;
		
	private static final Color GREY_COLOR = new Color(225,225,225);
	private static final Color WHITE_COLOR = Color.white;
	private Color[] colors={WHITE_COLOR, new Color(255,180,180), new Color(180,255,255), new Color(180,255,180), new Color(255,255,180),GREY_COLOR};
	private JComboBox cbxColor;
	private JLabel colorLbl;
	
	MaskDatasource()
	{
		setBorder(LineBorder.createGrayLineBorder());
			
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
			
		addField(gbl,I18n.getString("datasource.message.name","name")	, txtName	= new JTextField()		,0);
		addField(gbl,I18n.getString("datasource.message.url","url")	, txtUrl	= new JTextField()		,25);
		addField(gbl,I18n.getString("datasource.message.user","user")	, txtUid	= new JTextField()		,5);
		addField(gbl,I18n.getString("datasource.message.password","password"), txtPwd	= new JPasswordField()	,5);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.insets		= new Insets(8,5,0,0);
		
		cbxRemember = new JCheckBox(I18n.getString("datasource.message.remember","remember password"));
		gbl.setConstraints(cbxRemember, gbc);
		add(cbxRemember);
		
		gbc.insets = new Insets(1,5,0,0);
		cbxAutoconnect = new JCheckBox(I18n.getString("datasource.message.autoConnect","auto-connect on startup"));
		gbl.setConstraints(cbxAutoconnect, gbc);
		add(cbxAutoconnect);

		gbc.insets = new Insets(1,5,0,0);
		cbxReadonly = new JCheckBox(I18n.getString("datasource.message.readonly","readonly connection"));
		gbl.setConstraints(cbxReadonly, gbc);
		add(cbxReadonly);
		
		gbc.insets = new Insets(1,5,0,0);
		final JPanel colorPnl = new JPanel();
		colorLbl = new JLabel(I18n.getString("datasource.message.color","color"));
		colorPnl.add(colorLbl);
		cbxColor = new JComboBox(colors);
		cbxColor.setMaximumRowCount(colors.length);
		cbxColor.setPreferredSize(new Dimension(200,25));
		cbxColor.setRenderer(new ColorCellRenderer());
		colorPnl.add(cbxColor);
		gbl.setConstraints(colorPnl, gbc);
		add(colorPnl);
		colorLbl.setEnabled(false);
		
		//set grey color for readonly by default
		cbxReadonly.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(cbxReadonly.isSelected()){
					cbxColor.setSelectedItem(GREY_COLOR);
					cbxColor.setEnabled(false);
				}else{
					cbxColor.setSelectedItem(WHITE_COLOR);
					cbxColor.setEnabled(true);
				}
			}
		});

	}
	
	private class ColorCellRenderer extends JButton implements ListCellRenderer {  
	     public ColorCellRenderer() {  
	        setOpaque(true); 
	 		setPreferredSize(new Dimension(200,25));
	     }
	     private Color lastSelectedColor;
	     @Override
	     public void setBackground(Color bg) {
	    	 if(lastSelectedColor!=null){
	    		 super.setBackground(lastSelectedColor);
	    	 }
	     }
	     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,  
	        boolean cellHasFocus){  
	    	lastSelectedColor = (Color)value;
	        setText("");           
	        setBackground((Color)value);
	        if(index == -1){
	        	lastSelectedColor = null;
	        }
	        return this;  
	     }
	}


	private void addField(GridBagLayout gbl, String text, JComponent txt, int top)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.insets		= new Insets(top,8,1,8);
				
		JLabel lbl = new JLabel(text);
		gbl.setConstraints(lbl, gbc);
		add(lbl);
		
		gbc.fill	= GridBagConstraints.HORIZONTAL;
		gbc.insets	= new Insets(0,8,0,8);
		gbc.weightx	= 1.0;
		
		gbl.setConstraints(txt, gbc);
		add(txt);
	}
	
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		for(int i=0; i<getComponentCount();i++)
			getComponent(i).setEnabled(b);

		colorLbl.setEnabled(b);
		cbxColor.setEnabled(b);
	}
	
	void load(UoDatasource info)
	{
		txtName.setText(info.name);
		txtUrl.setText(info.url);
		txtUid.setText(info.uid);
		txtPwd.setText(info.remember?info.pwd:null);
		
		cbxRemember.setSelected(info.remember);
		cbxAutoconnect.setSelected(info.auto_connect);
		cbxReadonly.setSelected(info.readonly);
		cbxColor.setSelectedItem(info.color);
	}
	
	boolean unload(UoDatasource info)
	{
		if(Text.isEmpty(txtName.getText()) || Text.isEmpty(txtUrl.getText()))
		{
			Application.alert(Application.PROGRAM,I18n.getString("datasource.message.validName","please, enter a valid name."));
			return false;
		}
		
		if(txtName.getText().indexOf('.')!=-1
		|| txtName.getText().indexOf(',')!=-1
		|| txtName.getText().indexOf(';')!=-1
		|| txtName.getText().indexOf(':')!=-1
		|| txtName.getText().indexOf('|')!=-1
		|| txtName.getText().indexOf('/')!=-1
		|| txtName.getText().indexOf('<')!=-1
		|| txtName.getText().indexOf('>')!=-1
		|| txtName.getText().indexOf('@')!=-1
		|| txtName.getText().indexOf('\\')!=-1)
		{
			Application.alert(Application.PROGRAM,I18n.getString("datasource.message.invalidChar","characters \\.|,/;<:>@ aren't allowed!\nplease, enter a valid name."));
			return false;
		}
		
		info.name	= txtName.getText().trim();
		info.url	= txtUrl.getText().trim();
		info.uid	= txtUid.getText().trim();
		info.pwd	= String.valueOf(txtPwd.getPassword());
		
		info.remember		= cbxRemember.isSelected();
		info.auto_connect	= cbxAutoconnect.isSelected();
		info.readonly       = cbxReadonly.isSelected();
		info.color = (Color) cbxColor.getSelectedItem();
		
		return true;
	}
}