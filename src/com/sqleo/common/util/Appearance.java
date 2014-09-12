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

package com.sqleo.common.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Appearance
{
    public final static EmptyBorder NO_BORDER = new EmptyBorder(0,0,0,0);    
    
	public static Font fontBOLD	= new Font("Dialog", Font.BOLD, 11);
	public static Font fontPLAIN	= new Font("Dialog", Font.PLAIN, 11);
	public static Font fontMONO	= new Font("monospaced", Font.PLAIN, 11);
	
	private static final byte[] bytesMinus = new byte[] {(byte)71,(byte)73,(byte)70,(byte)56,(byte)57,(byte)97,(byte)9,(byte)0,(byte)9,(byte)0,(byte)179,(byte)0,(byte)0,(byte)82,(byte)82,(byte)247,(byte)206,(byte)206,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)44,(byte)0,(byte)0,(byte)0,(byte)0,(byte)9,(byte)0,(byte)9,(byte)0,(byte)0,(byte)4,(byte)21,(byte)48,(byte)200,(byte)73,(byte)133,(byte)189,(byte)66,(byte)226,(byte)171,(byte)119,(byte)14,(byte)2,(byte)32,(byte)138,(byte)159,(byte)87,(byte)122,(byte)221,(byte)70,(byte)173,(byte)65,(byte)4,(byte)0,(byte)59,};
	private static final Icon immagineMinus = new ImageIcon(bytesMinus)
	{
		public final void paintIcon(Component c, Graphics g, int x, int y) { c.setForeground(Color.black); g.setColor(Color.black); super.paintIcon(c,g,x-1,y); }
	}; // immagineMinus

	private static final byte[] bytesPlus = new byte[] {(byte)71,(byte)73,(byte)70,(byte)56,(byte)57,(byte)97,(byte)9,(byte)0,(byte)9,(byte)0,(byte)179,(byte)0,(byte)0,(byte)82,(byte)82,(byte)247,(byte)206,(byte)206,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)44,(byte)0,(byte)0,(byte)0,(byte)0,(byte)9,(byte)0,(byte)9,(byte)0,(byte)0,(byte)4,(byte)23,(byte)48,(byte)200,(byte)73,(byte)133,(byte)189,(byte)66,(byte)94,(byte)112,(byte)181,(byte)229,(byte)150,(byte)6,(byte)140,(byte)99,(byte)22,(byte)108,(byte)221,(byte)249,(byte)165,(byte)88,(byte)74,(byte)81,(byte)17,(byte)0,(byte)59,};
	private static final Icon immaginePlus = new ImageIcon(bytesPlus)
	{
		public final void paintIcon(Component c, Graphics g, int x, int y) { c.setForeground(Color.black); g.setColor(Color.black); super.paintIcon(c,g,x-1,y); }
	}; // immaginePlus

	static
	{
		UIManager.getDefaults().put("Button.font"					, fontBOLD);
		UIManager.getDefaults().put("Button.margin"					, new Insets(1,1,1,1));		
		UIManager.getDefaults().put("CheckBox.font"					, fontBOLD);
		UIManager.getDefaults().put("CheckBoxMenuItem.font"			, fontBOLD);
		UIManager.getDefaults().put("ComboBox.font"					, fontBOLD);
		UIManager.getDefaults().put("InternalFrame.font"			, fontBOLD);
		UIManager.getDefaults().put("InternalFrame.titleFont"		, fontBOLD);
		UIManager.getDefaults().put("Label.font"					, fontBOLD);
		UIManager.getDefaults().put("List.font"						, fontPLAIN);
		UIManager.getDefaults().put("Menu.font"						, fontBOLD);
		UIManager.getDefaults().put("MenuItem.font"					, fontBOLD);
		UIManager.getDefaults().put("OptionPane.border"				, new EmptyBorder(5,5,5,5));
		UIManager.getDefaults().put("OptionPane.buttonAreaBorder"	, new EmptyBorder(2,0,0,0));
		UIManager.getDefaults().put("OptionPane.yesButtonText"		, "yes");
		UIManager.getDefaults().put("OptionPane.noButtonText"		, "no");
		UIManager.getDefaults().put("OptionPane.okButtonText"		, "ok");
		UIManager.getDefaults().put("OptionPane.cancelButtonText"	, "cancel");
		UIManager.getDefaults().put("RadioButton.font"				, fontBOLD);
		UIManager.getDefaults().put("RadioButtonMenuItem.font"		, fontBOLD);
		UIManager.getDefaults().put("ScrollPane.border"				, LineBorder.createGrayLineBorder());
		UIManager.getDefaults().put("SplitPane.border"				, NO_BORDER);
		UIManager.getDefaults().put("SplitPane.dividerSize"			, new Integer(8));
		UIManager.getDefaults().put("SplitPaneDivider.border"		, NO_BORDER);
		UIManager.getDefaults().put("TabbedPane.font"				, fontBOLD);
		UIManager.getDefaults().put("Table.font"					, fontPLAIN);
		UIManager.getDefaults().put("TableHeader.font"				, fontPLAIN);
		UIManager.getDefaults().put("TextArea.font"					, fontMONO);
		UIManager.getDefaults().put("TextField.font"				, fontPLAIN);
		UIManager.getDefaults().put("TitledBorder.font"				, fontBOLD);
		UIManager.getDefaults().put("ToggleButton.font"				, fontBOLD);
		UIManager.getDefaults().put("ToggleButton.margin"			, new Insets(1,1,1,1));
		UIManager.getDefaults().put("ToolTip.font"					, fontPLAIN);
		UIManager.getDefaults().put("Tree.collapsedIcon"			, immaginePlus);
		UIManager.getDefaults().put("Tree.expandedIcon"				, immagineMinus);
		UIManager.getDefaults().put("Tree.font"						, fontPLAIN);
		UIManager.getDefaults().put("Tree.rowHeight"				, new Integer(18));
	}
	
//	public static void main(String args[])
//	{
//		for(Enumeration e = UIManager.getDefaults().keys(); e.hasMoreElements(); ) System.out.println(e.nextElement());
//	}
}