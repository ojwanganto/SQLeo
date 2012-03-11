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
package nickyb.sqleonardo.environment.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import nickyb.sqleonardo.querybuilder.syntax._ReservedWords;

public class ManualDBMetaData {

	private static final String CSV_SEPARATOR = ";";
 	
	private static final int IDX_RELATION = 0; //pkName or fkName
 	private static final int IDX_JOIN_TYPE = 1; // joinType
 	private static final int IDX_FKT_SCHEMA = 2; // fktSchema
 	private static final int IDX_FKT_NAME = 3; //fktName
 	private static final int IDX_FKT_COLUMN = 4; //fktColum
 	private static final int IDX_PKT_SCHEMA = 5; //pktSchema
 	private static final int IDX_PKT_NAME = 6; // pktName
 	private static final int IDX_PK_TABLE_ALIAS = 7; //pktAlias
 	private static final int IDX_PKT_COLUMN = 8; //pktColumn
 	private static final int IDX_REL_COMMENT = 9;
 	
 	//key = tableschema+"-"+table; tableowner = schema or catalog TODO
 	private Map<String,ManualTableMetaData> relationsStore = new HashMap<String,ManualTableMetaData>();
	
	public ManualTableMetaData getManualTableMetaData(String catalog,String schema,String table){
		schema = schema == null ?"": schema;
		String key = schema + "-" + table;
		return relationsStore.get(key);
	}
	
	private ManualTableMetaData createOrGetManualTableMetaData(String key){
		if(null == relationsStore.get(key)){
			relationsStore.put(key, new ManualTableMetaData());
		}
		return relationsStore.get(key);
	}

	private void updateMap(CSVRelationDefinition rdef){
		String fkKey = rdef.getFktSchema() + "-" + rdef.getFktName();
		ManualTableMetaData std = createOrGetManualTableMetaData(fkKey);
		std.addImportedKey(rdef);
		
		String refKey = rdef.getPktSchema() + "-" + rdef.getPktName();
		ManualTableMetaData rtd = createOrGetManualTableMetaData(refKey);
		rtd.addExportedKey(rdef);
	}
	
	private String getSQLJoinType(String csvJoinType){
		if("INNER".equals(csvJoinType)){
			return _ReservedWords.INNER_JOIN;
		}
		if("LEFT".equals(csvJoinType)){
			return _ReservedWords.LEFT_OUTER_JOIN;
		}
		if("RIGHT".equals(csvJoinType)){
			return _ReservedWords.RIGHT_OUTER_JOIN;
		}
		if("FULL".equals(csvJoinType)){
			return _ReservedWords.FULL_OUTER_JOIN;
		}
		return csvJoinType;
	}
	
	public ManualDBMetaData(String fkDefFileName){
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fkDefFileName));
			String row= null;
			while((row  = in.readLine())!=null){
				String[] columns = row.split(CSV_SEPARATOR);
				String fktSchema = columns[IDX_FKT_SCHEMA];
				String fktName = columns[IDX_FKT_NAME];
				String fktColumn = columns[IDX_FKT_COLUMN];
				String pktSchema = columns[IDX_PKT_SCHEMA];
				String pktName = columns[IDX_PKT_NAME];
				String pktColumn = columns[IDX_PKT_COLUMN];
				String joinType = columns[IDX_JOIN_TYPE];
				String pktAlias =  columns[IDX_PK_TABLE_ALIAS];
				String pkName = columns[IDX_RELATION];
				String fkName = pkName;
				String comment = null;
				if(columns.length > 9){
				 comment = columns[IDX_REL_COMMENT];
				}
				
				if(pktSchema.isEmpty()){
					pktSchema = "";
				}
				if(fktSchema.isEmpty()){
					fktSchema = "";
				}

				CSVRelationDefinition rdef = new CSVRelationDefinition();
				rdef.setPktSchema(pktSchema);
				rdef.setPktName(pktName);
				rdef.setPktColumnName(pktColumn);
				rdef.setFktSchema(fktSchema);
				rdef.setFktName(fktName);
				rdef.setFktColumnName(fktColumn);
				
				rdef.setPktAlias(pktAlias);
				rdef.setJoinType(getSQLJoinType(joinType));
				rdef.setComment(comment!=null?comment:"");
				rdef.setFkName(fkName);
				rdef.setPkName(pkName);
				
				updateMap(rdef);
			}
			
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
