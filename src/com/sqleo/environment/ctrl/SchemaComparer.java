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

package com.sqleo.environment.ctrl;

import javax.swing.JSplitPane;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.environment.ctrl.comparer.CriteriaPane;
import com.sqleo.environment.ctrl.comparer.ReportPane;


public class SchemaComparer extends BorderLayoutPanel
{
	public SchemaComparer()
	{
		super(2,2);
		
		ReportPane report = new ReportPane();
		CriteriaPane criteria = new CriteriaPane(report);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,criteria,report);
		split.setDividerLocation(250);
		split.setOneTouchExpandable(true);
		setComponentCenter(split);
	}	
}