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

package nickyb.sqleonardo.environment.ctrl.explorer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import nickyb.sqleonardo.common.util.Text;
import nickyb.sqleonardo.environment.Application;

public class MaskDatasource extends JPanel
{
	private JTextField txtName;
	private JTextField txtUrl;
	private JTextField txtUid;
	private JPasswordField txtPwd;
	
	private JCheckBox cbxRemember;
	private JCheckBox cbxAutoconnect;
		
	MaskDatasource()
	{
		setBorder(LineBorder.createGrayLineBorder());
			
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
			
		addField(gbl,"name:"	, txtName	= new JTextField()		,0);
		addField(gbl,"url:"		, txtUrl	= new JTextField()		,25);
		addField(gbl,"user:"	, txtUid	= new JTextField()		,5);
		addField(gbl,"password:", txtPwd	= new JPasswordField()	,5);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.insets		= new Insets(8,5,0,0);
		
		cbxRemember = new JCheckBox("remember password");
		gbl.setConstraints(cbxRemember, gbc);
		add(cbxRemember);
		
		gbc.insets = new Insets(1,5,0,0);
		cbxAutoconnect = new JCheckBox("auto-connect on startup");
		gbl.setConstraints(cbxAutoconnect, gbc);
		add(cbxAutoconnect);
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
	}
	
	void load(UoDatasource info)
	{
		txtName.setText(info.name);
		txtUrl.setText(info.url);
		txtUid.setText(info.uid);
		txtPwd.setText(info.remember?info.pwd:null);
		
		cbxRemember.setSelected(info.remember);
		cbxAutoconnect.setSelected(info.auto_connect);
	}
	
	boolean unload(UoDatasource info)
	{
		if(Text.isEmpty(txtName.getText()) || Text.isEmpty(txtUrl.getText()))
		{
			Application.alert(Application.PROGRAM,"please, enter a valid name.");
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
			Application.alert(Application.PROGRAM,"characters \\.|,/;<:>@ aren't allowed!\nplease, enter a valid name.");
			return false;
		}
		
		info.name	= txtName.getText().trim();
		info.url	= txtUrl.getText().trim();
		info.uid	= txtUid.getText().trim();
		info.pwd	= String.valueOf(txtPwd.getPassword());
		
		info.remember		= cbxRemember.isSelected();
		info.auto_connect	= cbxAutoconnect.isSelected();
		
		return true;
	}
}