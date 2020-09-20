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
package de.bxservice.omnisearch.validator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.EventManager;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MColumn;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.bxservice.omniimpl.TextSearchValues;
import de.bxservice.omnisearch.osgi.OsigEventhandleUtil;
import de.bxservice.omnisearch.tools.OmnisearchHelper;

/**
 * Osgi component to get reference to Osgi framework (BundleContext), also life cycle
 * up to that stuff program registry EventHandle to Idempiere event (PO event, window event,...)
 * it's same {@link EventManager} but difference at:
 * 	1. EventManager use registry all EventHandle to BaseActivator.getBundleContext(), this one registry EventHandle to its bundle
 * 	2. at moment every EventHandle extend from {@link AbstractEventHandler} need to depend on adempiereBase
 *  3. {@link EventManager} central manage all EventHandle it's a bit tight dependency between bundle has EventHandle object and adempiereBase
 * @author hieplq
 *
 */
@Component
public class ManagerEventHandle {
	// it's not clear to use PropertyChangeListener for this event but fast implement
	private final PropertyChangeSupport deactiveEventHandle = new PropertyChangeSupport(this);
	private BundleContext bundleContext;
	
	@Activate
    protected void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		bundleContext.addFrameworkListener(fwEvent -> {
			// wait framework ready to make sure database ready
			if (fwEvent.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
				registryTSearchIndexEventHandler(null, null);
			}
		});
    }
	
	@Deactivate
	protected void deactivate() {
		synchronized (this) {
			PropertyChangeEvent event = new PropertyChangeEvent(this, null, null, null);
			deactiveEventHandle.firePropertyChange(event);
		}
	}
	
	public void addDeactiveEventHandleListener(PropertyChangeListener listener) {
		deactiveEventHandle.addPropertyChangeListener(listener);
	}
	
	public void removeDeactiveEventHandleListener(PropertyChangeListener listener) {
		deactiveEventHandle.removePropertyChangeListener(listener);
	}
	
	/**
	 * registry to handle PO event
	 * in case eventHandle is null then create {@link TSearchIndexEventHandler} and registry it to handle PO event
	 * in case eventHandle is not null then un-registry after that do registry handle PO event to update new set of column use full search text
	 * @param eventHandle
	 */
	protected void registryTSearchIndexEventHandler(TSearchIndexEventHandler eventHandle, String trxName) {
		List<String> indexedTables = OmnisearchHelper.getIndexedTableNames(TextSearchValues.TS_INDEX_NAME, trxName);
		Set<String>  fkTableNames = OmnisearchHelper.getForeignTableNames(TextSearchValues.TS_INDEX_NAME, trxName);
		
		Map<String[], String> eventInfos = buildEventInfosForTSearchIndexEventHandler(indexedTables, fkTableNames);
		
		if (eventHandle == null) {
			eventHandle = new TSearchIndexEventHandler();
			eventHandle.addUpdateColumnUseFTSListener(changeEvent -> {
				// re-registry handle PO event to update new set of column use full search text
				TSearchIndexEventHandler eventHandlerUpdate = (TSearchIndexEventHandler)changeEvent.getSource();
				synchronized (this) {
					this.addDeactiveEventHandleListener(eventHandlerUpdate);
					registryTSearchIndexEventHandler(eventHandlerUpdate, changeEvent.getNewValue().toString());
				}
			});
		}else {
			// un-registry PO event for old set of column use full text search
			OsigEventhandleUtil.unRegistryAllEvent(eventHandle);
		}

		//registry PO event for current set of column use full text search
		eventHandle.setFKTableNames(fkTableNames);
		OsigEventhandleUtil.registryEventHandle(bundleContext, eventHandle, TSearchIndexEventHandler.class, eventInfos);
	}
		
	protected Map<String[], String> buildEventInfosForTSearchIndexEventHandler (List<String> indexedTables, Set<String>  fkTableNames){
		Map<String[], String> eventInfos = new HashMap<String[], String>();
		
		indexedTables.stream().forEach(tableName -> {
			eventInfos.put(new String [] {IEventTopics.PO_AFTER_NEW, IEventTopics.PO_AFTER_CHANGE, IEventTopics.PO_AFTER_DELETE}, 
					OsigEventhandleUtil.filterByTableName(tableName));
		});
		
		//Index the FK tables
		fkTableNames.stream().filter(fkTableName -> {
				return !indexedTables.contains(fkTableName);
			}).forEach(nonIndexTableName -> {
				//Don't duplicate the Event for the same table
				eventInfos.put(new String [] {IEventTopics.PO_AFTER_CHANGE}, 
						OsigEventhandleUtil.filterByTableName(nonIndexTableName));
			});
		
		//Handle the changes in MColumn to update the index
		eventInfos.put(new String [] {IEventTopics.PO_AFTER_NEW, IEventTopics.PO_AFTER_CHANGE, IEventTopics.PO_AFTER_DELETE},
				OsigEventhandleUtil.filterByTableName(MColumn.Table_Name));
		
		return eventInfos;
	}

}
