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
package com.sqleo.environment.io;

import java.util.ArrayList;

public class ManualTableMetaData {

	private ArrayList alImportedKeys = new ArrayList();
	private ArrayList alExportedKeys = new ArrayList();;
	
	public void addImportedKey(CSVRelationDefinition def){
		addKey(def,alImportedKeys);
	}
	public void addExportedKey(CSVRelationDefinition def){
		addKey(def,alExportedKeys);
	}
	
	private void addKey(CSVRelationDefinition def,ArrayList keys){

		String[] row = new String[17];
		row[IDX_REL_PKTABLE_CAT] = def.getPktCatalog() ;
		row[IDX_REL_PKTABLE_SCHEM] = def.getPktSchema().isEmpty()?null:def.getPktSchema() ;
		row[IDX_REL_PKTABLE_NAME] = def.getPktName() ;
		row[IDX_REL_PKCOLUMN_NAME] = def.getPktColumnName() ;
		row[IDX_REL_FKTABLE_CAT] = def.getFktCatalog() ;
		row[IDX_REL_FKTABLE_SCHEM] = def.getFktSchema().isEmpty()?null:def.getFktSchema() ;
		row[IDX_REL_FKTABLE_NAME] = def.getFktName() ;
		row[IDX_REL_FKCOLUMN_NAME] = def.getFktColumnName() ;
		row[IDX_REL_KEY_SEQ] = String.valueOf(def.getKeySeq()) ;
		row[IDX_REL_UPDATE_RULE] =  String.valueOf(def.getUpdateRule()) ;
		row[IDX_REL_DELETE_RULE] =  String.valueOf(def.getDeleteRule()) ;
		row[IDX_REL_FK_NAME] = def.getFkName() ;
		row[IDX_REL_PK_NAME] = def.getPkName() ;
		row[IDX_REL_DEFERRABILITY] =  String.valueOf(def.getDeferrability()) ;	
		
		// extra
		row[14] = def.getJoinType() ;
		row[15] = def.getPktAlias() ;
		row[16] = def.getComment() ;
		
		
		keys.add(row);
	}
	

	
	public ArrayList getImportedKeys(){
		return alImportedKeys;
	}
	public ArrayList getExportedKeys(){
		return alExportedKeys;
	}
	
	
	/* RELATiONSHiP (iMPORTED&EXPORTED KEY) */
	public static final int IDX_REL_PKTABLE_CAT		= 0;
	public static final int IDX_REL_PKTABLE_SCHEM	= 1;
	public static final int IDX_REL_PKTABLE_NAME	= 2;
	public static final int IDX_REL_PKCOLUMN_NAME	= 3;
	public static final int IDX_REL_FKTABLE_CAT		= 4;
	public static final int IDX_REL_FKTABLE_SCHEM	= 5;
	public static final int IDX_REL_FKTABLE_NAME	= 6;
	public static final int IDX_REL_FKCOLUMN_NAME	= 7;
	public static final int IDX_REL_KEY_SEQ			= 8;
	public static final int IDX_REL_UPDATE_RULE		= 9;
	public static final int IDX_REL_DELETE_RULE		= 10;
	public static final int IDX_REL_FK_NAME			= 11;
	public static final int IDX_REL_PK_NAME			= 12;
	public static final int IDX_REL_DEFERRABILITY	= 13;

}
