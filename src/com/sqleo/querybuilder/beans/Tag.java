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
 *
 * Tag.java
 * 
 * Created on 4 ottobre 2004, 22.17
 *
 */

package com.sqleo.querybuilder.beans;

/**
 *
 * @author  Administrator
 */
public class Tag {
    
    private Object value;
    private String name = "";
    
    public Tag(Object value, String name) {
        setName( name );
        setValue(value);
    }
    
    public Tag(String value) {
        setName( value );
        setValue(value);
    }
    
    public Tag(Object value) {
        setName( value+"");
        setValue(value);
    }
    
    public Tag(int value, String name) {
        setName( name );
        setValue(value);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public void setValue(int value) {
        this.value = ""+value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String toString()
    {
        return getName();
    }
    
}
