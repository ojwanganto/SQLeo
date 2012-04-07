/*
 * Copyright (C) 2006 JasperSoft http://www.jaspersoft.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 *
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  USA  02111-1307
 *
 *
 * EntityField.java
 *
 * Created on January 30, 2007, 4:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sqleo.querybuilder.beans;

import com.sqleo.querybuilder.syntax.QueryTokens;

/**
 *
 * @author gtoffoli
 */
public class EntityField  {
    
    private QueryTokens.Table table = null;
    private String fieldName = null;
    
    /** Creates a new instance of EntityField */
    public EntityField() {
    }



    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public QueryTokens.Table getTable() {
        return table;
    }

    public void setTable(QueryTokens.Table table) {
        this.table = table;
    }
    
}
