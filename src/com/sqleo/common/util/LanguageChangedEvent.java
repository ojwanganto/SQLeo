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
 */

package com.sqleo.common.util;

/**
 *
 * @author  Administrator
 */
public class LanguageChangedEvent {
    
    private java.util.Locale locale;
    
    /** Creates a new instance of LanguageChangedEvent */
    public LanguageChangedEvent(java.util.Locale locale) {
        
        this.locale = locale;
        
    }
    
    /** Getter for property locale.
     * @return Value of property locale.
     *
     */
    public java.util.Locale getLocale() {
        return locale;
    }
    
    /** Setter for property locale.
     * @param locale New value of property locale.
     *
     */
    public void setLocale(java.util.Locale locale) {
        this.locale = locale;
    }
    
}
