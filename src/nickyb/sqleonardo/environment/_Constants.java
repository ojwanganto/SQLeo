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

package nickyb.sqleonardo.environment;

public interface _Constants
{
	public static final String ENTRY_PREFERENCES	= "$PREFERENCES";
	public static final String ENTRY_JDBC			= "$JDBC";
	public static final String ENTRY_INFO			= "$INFO";

/*******************************************************************************/
// actions
/*******************************************************************************/
	public static final String ACTION_ABOUT			= "action.050";
	public static final String ACTION_EXIT			= "action.051";
	public static final String ACTION_NEW_QUERY		= "action.052";
	public static final String ACTION_LOAD_QUERY	= "action.053";
	
	public static final String ACTION_SHOW_PREFERENCES	= "action.100";
	public static final String ACTION_MDI_SHOW_EXPLORER	= "action.101";
	public static final String ACTION_MDI_SHOW_EDITOR	= "action.102";
	public static final String ACTION_MDI_SHOW_COMPARER	= "action.103";
	public static final String ACTION_SHOW_CONTENT		= "action.104";
	public static final String ACTION_SHOW_DEFINITION	= "action.105";

	public static final String ACTION_MDI_CASCADE	= "action.110";
	public static final String ACTION_MDI_TILEH		= "action.111";
	public static final String ACTION_MDI_CLOSE_ALL	= "action.112";
	public static final String ACTION_MDI_GOBACK	= "action.113";
	public static final String ACTION_MDI_GOFWD		= "action.114";
/*******************************************************************************/
// icons
/*******************************************************************************/
	public static final String ICON_CONNECT		= "icon.001";
	public static final String ICON_DISCONNECT	= "icon.002";
	
	public static final String ICON_ACCEPT	= "icon.009";
	public static final String ICON_SAVE	= "icon.010";
	public static final String ICON_STOP	= "icon.011";
	public static final String ICON_FIND	= "icon.012";
	public static final String ICON_FILTER	= "icon.013";
	public static final String ICON_DELETE	= "icon.014";
	public static final String ICON_BACK	= "icon.015";
	public static final String ICON_FWD		= "icon.016";
	
	public static final String ICON_PREFERENCES	= "icon.050";
    public static final String ICON_EXPLORER	= "icon.051";
    public static final String ICON_EDITOR		= "icon.052";
	public static final String ICON_COMPARER	= "icon.053";
	
	public static final String ICON_EXPLORER_DRIVER_OK		= "icon.99";
	public static final String ICON_EXPLORER_DRIVER_KO		= "icon.100";
	public static final String ICON_EXPLORER_DATASOURCE_OK	= "icon.101";
	public static final String ICON_EXPLORER_DATASOURCE_KO	= "icon.102";
	public static final String ICON_EXPLORER_SCHEMA			= "icon.103";
	public static final String ICON_EXPLORER_TYPES			= "icon.104";
	public static final String ICON_EXPLORER_ALL			= "icon.105";
	public static final String ICON_EXPLORER_LINKS			= "icon.106";
	public static final String ICON_EXPLORER_ADD_GROUP		= "icon.107";
	public static final String ICON_EXPLORER_REMOVE_GROUP	= "icon.108";
	public static final String ICON_EXPLORER_DRIVER_NEW		= "icon.110";
	public static final String ICON_EXPLORER_DATASOURCE_NEW	= "icon.111";

	public static final String ICON_EDITOR_OPEN = "icon.120";
	public static final String ICON_EDITOR_SAVE = "icon.121";
	public static final String ICON_EDITOR_RUN	= "icon.122";

	public static final String ICON_QUERY_LAUNCH	= "icon.151";
	public static final String ICON_DIAGRAM_SAVE	= "icon.152";
	
	public static final String ICON_CONTENT_INSERT	= "icon.160";
	public static final String ICON_CONTENT_DELETE	= "icon.161";
	public static final String ICON_CONTENT_UPDATE	= "icon.162";
}
