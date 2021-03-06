/**********************************************************************
* Copyright (C) Contributors                                          *
*                                                                     *
* This program is free software; you can redistribute it and/or       *
* modify it under the terms of the GNU General Public License         *
* as published by the Free Software Foundation; either version 2      *
* of the License, or (at your option) any later version.              *
*                                                                     *
* This program is distributed in the hope that it will be useful,     *
* but WITHOUT ANY WARRANTY; without even the implied warranty of      *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
* GNU General Public License for more details.                        *
*                                                                     *
* You should have received a copy of the GNU General Public License   *
* along with this program; if not, write to the Free Software         *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
* MA 02110-1301, USA.                                                 *
*                                                                     *
* Contributors:                                                       *
* - Diego Ruiz - BX Service GmbH                                      *
**********************************************************************/
package de.bxservice.omnisearch.tools;

import org.compiere.model.MTable;
import org.compiere.model.MUserDefWin;
import org.compiere.model.MWindow;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.compiere.util.Util;

public class TextSearchResult {
	
	private int AD_Table_ID;
	private int Record_ID;
	private String htmlHeadline;
	
	public int getAD_Table_ID() {
		return AD_Table_ID;
	}
	
	public void setAD_Table_ID(int AD_Table_ID) {
		this.AD_Table_ID = AD_Table_ID;
	}
	
	public int getRecord_ID() {
		return Record_ID;
	}
	
	public void setRecord_ID(int Record_ID) {
		this.Record_ID = Record_ID;
	}
	
	public String getHtmlHeadline() {
		return htmlHeadline;
	}
	
	public void setHtmlHeadline(String htmlHeadline) {
		this.htmlHeadline = htmlHeadline;
	}
	
	public String getLabel() {
		String windowName;
		int windowID = Env.getZoomWindowID(getAD_Table_ID(), getRecord_ID());
		
		MWindow win = MWindow.get(Env.getCtx(), windowID);
		MUserDefWin userDef = MUserDefWin.getBestMatch(Env.getCtx(), windowID);
		if (userDef != null && !Util.isEmpty(userDef.getName())) {
			windowName = userDef.getName();
		} else {
			windowName = win.get_Translation("Name");
		}
		
		MTable table = MTable.get(Env.getCtx(), getAD_Table_ID());
		PO po = table.getPO(getRecord_ID(), null);

		String titleLogic = win.getTitleLogic();
		StringBuilder recordIdentifier = new StringBuilder("");

		if (! Util.isEmpty(titleLogic)) { // default way
			titleLogic = Env.parseVariable(titleLogic, po, null, false);
			if (! Util.isEmpty(titleLogic))
				recordIdentifier.append(" ").append(titleLogic);
		}
		
		if (recordIdentifier.length() == 0) {
			if (po.get_ColumnIndex("DocumentNo") > 0)
				recordIdentifier.append(" ").append(po.get_ValueAsString("DocumentNo"));
			if (po.get_ColumnIndex("Value") > 0)
				recordIdentifier.append(" ").append(po.get_ValueAsString("Value"));
			if (po.get_ColumnIndex("Name") > 0)
				recordIdentifier.append(" ").append(po.get_ValueAsString("Name"));
			if (recordIdentifier.length() == 0)
				recordIdentifier.append(" ").append(po.toString());
			if (recordIdentifier.length() == 0)
				recordIdentifier.append(" [").append(po.get_ID()).append("]");
			if (recordIdentifier.length() == 0)
				recordIdentifier.append(" [no identifier]");
		}
		return windowName + ": " + recordIdentifier.substring(1);
	}
}
