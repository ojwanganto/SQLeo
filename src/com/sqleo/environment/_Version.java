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

package com.sqleo.environment;

public interface _Version
{
    public static final String PROGRAM = "SQLeoVQB";
    public static final String AUTHOR = "";
    
    public static final String MAJOR = "2016";
    public static final String MINOR = "05.rc1";
    public static final String SRC = "_svn";
    public static final String WEB = "http://sqleo.sourceforge.net/guide.htm";
    public static final String SVN_BUILD_XML_FILE = "http://svn.code.sf.net/p/sqleo/code/trunk/build.xml";
    public static final String SF_WEB = "http://sourceforge.net/projects/sqleo/";
    public static final String DONATE_URL = "http://sqleo.sourceforge.net/support.htm";
    public static final String VERSION_TRACK = "http://www.google-analytics.com/collect?v=1&tid=UA-38580300-2&cid=555&t=pageview&dt=Version&dp=%2Fversion_"+MAJOR+"."+MINOR.replace("+","%2B")+SRC;

    //unicode characters for (c) is \u00a9
    public static String SQLEO_IMAGE_WATERMARK = "\u00a9 SQLeo " + MAJOR;
}