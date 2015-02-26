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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class DataComparerConfig {
	
	@XmlElement(name = "source")
	private DataComparerPanelConfig sourcePanelConfig;

	@XmlElement(name = "target")
	private DataComparerPanelConfig targetPanelConfig;

	@XmlElement(name = "onlyDifferentValues")
	private boolean onlyDifferentValues;
	
	@XmlElement(name = "addDiffStatusInOutput")
	private boolean addDiffStatusInOutput;

	public DataComparerPanelConfig getSourcePanelConfig() {
		return sourcePanelConfig;
	}

	public void setSourcePanelConfig(DataComparerPanelConfig sourcePanelConfig) {
		this.sourcePanelConfig = sourcePanelConfig;
	}

	public DataComparerPanelConfig getTargetPanelConfig() {
		return targetPanelConfig;
	}

	public void setTargetPanelConfig(DataComparerPanelConfig targetPanelConfig) {
		this.targetPanelConfig = targetPanelConfig;
	}

	public boolean isOnlyDifferentValues() {
		return onlyDifferentValues;
	}

	public void setOnlyDifferentValues(boolean onlyDifferentValues) {
		this.onlyDifferentValues = onlyDifferentValues;
	}

	public void setAddDiffStatusInOutput(boolean addDiffStatusInOutput) {
		this.addDiffStatusInOutput = addDiffStatusInOutput;
	}

	public boolean isAddDiffStatusInOutput() {
		return addDiffStatusInOutput;
	}

	
}
