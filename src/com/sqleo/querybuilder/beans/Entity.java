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
 * File contributed by JasperSoft Corp.
 *
 *
 * Entity.java
 *
 * Created on January 25, 2007, 5:38 PM
 *
 */

package com.sqleo.querybuilder.beans;

/**
 *
 * @author gtoffoli
 */
public class Entity {
    
    private String schema = "";
    private String entityName = "";
    /** Creates a new instance of Entity */
    public Entity(String schema, String entityName) {
        
        this.setSchema(schema);
        this.setEntityName(entityName);
    }
    
    public String toString()
    {
        return getEntityName();
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
}
