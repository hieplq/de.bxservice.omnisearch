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

import java.lang.reflect.InvocationTargetException;

import org.adempiere.base.Service;
import org.adempiere.base.ServiceQuery;
import org.adempiere.exceptions.AdempiereException;

public class OmnisearchDocumentFactory extends OmnisearchAbstractFactory {

	@Override
	public OmnisearchIndex getIndex(String indexType) {
		return null;
	}

	@Override
	public OmnisearchDocument getDocument(String documentType) {
		if (documentType == null)
			return null;
		
		ServiceQuery query = new ServiceQuery();
		query.put("documentType", documentType);
		OmnisearchDocument custom = Service.locator().locate(OmnisearchDocument.class, query).getService();			
		if (custom == null)
			throw new AdempiereException("No OmnisearchDocument provider found for documentType " + documentType);
		try {
			return custom.getClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException |
				IllegalArgumentException | InvocationTargetException |
				NoSuchMethodException | SecurityException e) {}
		
		return null;
	}

}
