package de.bxservice.omnisearch.validator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;

import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MColumn;
import org.compiere.model.PO;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.bxservice.omniimpl.TextSearchValues;
import de.bxservice.omnisearch.osgi.AbstractEventHandleRegistrationManager;
import de.bxservice.omnisearch.osgi.OsigEventhandleUtil;
import de.bxservice.omnisearch.tools.OmnisearchAbstractFactory;
import de.bxservice.omnisearch.tools.OmnisearchHelper;

public class TSearchIndexEventHandler extends AbstractEventHandleRegistrationManager<TSearchIndexEventHandler> implements EventHandler{
	// snapshots list fkTableNames query at time registry handle event
	private Set<String> fkTableNames;
	
	//it's not clear to use PropertyChangeListener for this event but fast implement
	private final PropertyChangeSupport updateColumnUseFTS = new PropertyChangeSupport(this);
	
	public void addUpdateColumnUseFTSListener(PropertyChangeListener listener) {
		updateColumnUseFTS.addPropertyChangeListener(listener);
	}
	
	public void removeUpdateColumnUseFTSListener(PropertyChangeListener listener) {
		updateColumnUseFTS.removePropertyChangeListener(listener);
	}
		 
	public void setFKTableNames (Set<String> fkTableNames) {
		this.fkTableNames = fkTableNames;
	}
	
	@Override
	public void handleEvent(Event event) {
		String type = event.getTopic();
		PO po = OsigEventhandleUtil.getPO(event);
		
		if (po instanceof MColumn) {
			if ((type.equals(IEventTopics.PO_AFTER_CHANGE) &&
					po.is_ValueChanged(TextSearchValues.TS_INDEX_NAME)) || 
					(po.get_ValueAsBoolean(TextSearchValues.TS_INDEX_NAME))) {
				//If the Text search index flag is changed -> register/unregister the modified table
				// it's not clear to use PropertyChangeEvent for this event but fast implement
				PropertyChangeEvent updateEvent = new PropertyChangeEvent(this, null, null, po.get_TrxName());
				fireEventUpdateEventHandleRegistry(updateEvent);
			}
		} else if (type.equals(IEventTopics.PO_AFTER_DELETE))
			OmnisearchHelper.deleteFromDocument(OmnisearchAbstractFactory.TEXTSEARCH_INDEX, po);
		else 
			OmnisearchHelper.updateDocument(OmnisearchAbstractFactory.TEXTSEARCH_INDEX, po, 
					type.equals(IEventTopics.PO_AFTER_NEW));
		
		if (fkTableNames.contains(po.get_TableName())) {
			OmnisearchHelper.updateParent(OmnisearchAbstractFactory.TEXTSEARCH_INDEX, po);
		}
		
	}
	
	public void fireEventUpdateEventHandleRegistry(PropertyChangeEvent event) {
		updateColumnUseFTS.firePropertyChange(event);
	}

	@Override
	public TSearchIndexEventHandler getEventHandleObj() {
		return this;
	}
	
}