/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;

/**
 * MDIClient with Commit/ Rollback actions
 */
public abstract class MDIClientWithCRActions extends MDIClient {

	public MDIClientWithCRActions(String title) {
		super(title);
	}

	// Subclasses where commit-rollback buttons are added must override this method
	protected void notifyResponseToView(boolean isCommitNotify){

	};
	
	// Ticket #333 - Commit/Rollback buttons KO out of initial connection
	// added to leave the responsability of changes to the caller. All related
	// code has changed
	abstract String getActiveConnection();

	protected class ActionCommit extends AbstractAction {
		private Action refresh = null;
		ActionCommit() {
			putValue(SMALL_ICON,
					Application.resources.getIcon(Application.ICON_ACCEPT));
			putValue(SHORT_DESCRIPTION, "Commit...");
			putValue(NAME, "action-commit");
			setEnabled(!ConnectionAssistant.getAutoCommitPrefered());

		}
		ActionCommit(final Action refresh) {
			this();
			this.refresh = refresh;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			
			String keyCah = getActiveConnection();
			
			if (keyCah == null) {
				Application.alert(Application.PROGRAM, "No connection!");
				return;
			}
			if (ConnectionAssistant.hasHandler(keyCah)) {
				ConnectionHandler ch = ConnectionAssistant
				.getHandler(keyCah);
				try {
					if(!ConnectionAssistant.getAutoCommitPrefered()){
						ch.get().commit();
						notifyResponseToView(true);
//						System.out.println("Commit successfull");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(this.refresh!=null){
					this.refresh.actionPerformed(null);
				}
			}
		}
	}

	protected class ActionRollback extends AbstractAction {
		private Action refresh = null;
		ActionRollback() {
			putValue(SMALL_ICON,
					Application.resources.getIcon(Application.ICON_DELETE)); //TODO keep rollback icon
			putValue(SHORT_DESCRIPTION, "Rollback...");
			putValue(NAME, "action-rollback");
			setEnabled(!ConnectionAssistant.getAutoCommitPrefered());
		}
		ActionRollback(final Action refresh) {
			this();
			this.refresh = refresh;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			String keyCah = getActiveConnection();

			if (keyCah == null) {
				Application.alert(Application.PROGRAM, "No connection!");
				return;
			}

			if (ConnectionAssistant.hasHandler(keyCah)) {
				ConnectionHandler ch = ConnectionAssistant
				.getHandler(keyCah);
				try {
					if(!ConnectionAssistant.getAutoCommitPrefered()){
						ch.get().rollback();
						notifyResponseToView(false);
//						System.out.println("Rollback successfull");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(this.refresh!=null){
					this.refresh.actionPerformed(null);
				}
			}
		}
	}
}
