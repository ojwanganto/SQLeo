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
 *
 * This class was contributed by JasperSoft Corp.
 */

package com.sqleo.common.util;

import java.util.Locale;

/**
 *
 * @author gtoffoli
 */
/**
         * A simple class to enable locales to be placed in a combo box.
         *
         */
public class LocaleAdapter
{
    Locale l;
    public LocaleAdapter( Locale locale )
    {
        l = locale;
    }

    public Locale getLocale()
    {
        return l;
    }

    // ==> Modified by RL, June 3, 2005, introducing getVariant and Language Papiamentu
    // Getdisplayname is now used to display the name according to the Locale.
    // Possibly in Chinese language parts are combined differently.
    public String toString()
    {
        if ( l.getLanguage().equals("") )
        {
            return "" ;
        }
        else
        {
            return l.getDisplayName();
        }
    }
    

}
