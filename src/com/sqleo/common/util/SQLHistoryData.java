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
package com.sqleo.common.util;

public class SQLHistoryData {
	
	public String timestamp;
	public String connection;
	public String window;
	public String query;
	public static String DELIMITER = "|";
	private static SQLHistoryData headerRow = null;
	
	public SQLHistoryData(String timestamp, String connection,String window,String query) {
		this.timestamp = timestamp;
		this.connection = connection;
		this.window = window;
		this.query = query;
	}
	
	public static SQLHistoryData getHeaderRow(){
		if(headerRow!=null){
			return headerRow; 
		}else{
			headerRow = new SQLHistoryData("Timestamp","Connection","Window","Query");
			return headerRow;
		}
	}
	
}
