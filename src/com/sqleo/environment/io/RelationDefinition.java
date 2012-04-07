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
/* RELATiONSHiP (iMPORTED&EXPORTED KEY) 14 columns*/
public class RelationDefinition {
	
	String pktCatalog;
	String pktSchema;
	String pktName;
	String pktColumnName;
	String fktCatalog;
	String fktSchema;
	String fktName;
	String fktColumnName;
	short keySeq;
	short updateRule;
	short deleteRule;
	String fkName;
	String pkName;
	short deferrability;
	
	public String getPktCatalog() {
		return pktCatalog;
	}
	public void setPktCatalog(String pktCatalog) {
		this.pktCatalog = pktCatalog;
	}
	public String getPktSchema() {
		return pktSchema;
	}
	public void setPktSchema(String pktSchema) {
		this.pktSchema = pktSchema;
	}
	public String getPktName() {
		return pktName;
	}
	public void setPktName(String pktName) {
		this.pktName = pktName;
	}
	public String getPktColumnName() {
		return pktColumnName;
	}
	public void setPktColumnName(String pktColumnName) {
		this.pktColumnName = pktColumnName;
	}
	public String getFktCatalog() {
		return fktCatalog;
	}
	public void setFktCatalog(String fktCatalog) {
		this.fktCatalog = fktCatalog;
	}
	public String getFktSchema() {
		return fktSchema;
	}
	public void setFktSchema(String fktSchema) {
		this.fktSchema = fktSchema;
	}
	public String getFktName() {
		return fktName;
	}
	public void setFktName(String fktName) {
		this.fktName = fktName;
	}
	public String getFktColumnName() {
		return fktColumnName;
	}
	public void setFktColumnName(String fktColumnName) {
		this.fktColumnName = fktColumnName;
	}
	public short getKeySeq() {
		return keySeq;
	}
	public void setKeySeq(short keySeq) {
		this.keySeq = keySeq;
	}
	public short getUpdateRule() {
		return updateRule;
	}
	public void setUpdateRule(short updateRule) {
		this.updateRule = updateRule;
	}
	public short getDeleteRule() {
		return deleteRule;
	}
	public void setDeleteRule(short deleteRule) {
		this.deleteRule = deleteRule;
	}
	public String getFkName() {
		return fkName;
	}
	public void setFkName(String fkName) {
		this.fkName = fkName;
	}
	public String getPkName() {
		return pkName;
	}
	public void setPkName(String pkName) {
		this.pkName = pkName;
	}
	public short getDeferrability() {
		return deferrability;
	}
	public void setDeferrability(short deferrability) {
		this.deferrability = deferrability;
	}

}
