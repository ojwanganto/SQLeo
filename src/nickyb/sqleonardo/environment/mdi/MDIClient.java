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

package nickyb.sqleonardo.environment.mdi;

import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import nickyb.sqleonardo.common.gui.ClientFrame;
import nickyb.sqleonardo.common.gui.Toolbar;
import nickyb.sqleonardo.common.jdbc.ConnectionAssistant;
import nickyb.sqleonardo.common.jdbc.ConnectionHandler;
import nickyb.sqleonardo.environment.Application;
import nickyb.sqleonardo.environment.Preferences;

public abstract class MDIClient extends ClientFrame {
	private static int counter = 0;
	private int id = -1;

	public MDIClient(String title) {
		super(title);
		setFrameIcon(null);
		setMaximizable(true);
		setResizable(true);
		setClosable(true);

		if (getName() == null) {
			setName("MDIClient_" + ++counter);
			setTitle((id = counter) + " - " + title);
			//setClosable(true); redundant
		}
	}

	protected int getID() {
		return id;
	}

	public abstract JMenuItem[] getMenuActions();

	public abstract Toolbar getSubToolbar();

	protected abstract void setPreferences();

}
