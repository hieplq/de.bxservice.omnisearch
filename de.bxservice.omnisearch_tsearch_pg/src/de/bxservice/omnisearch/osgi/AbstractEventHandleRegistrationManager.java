package de.bxservice.omnisearch.osgi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

/**
 * every EventHandle need to extend from this abstract to manage unregister
 * @author hieplq
 *
 * @param <T>
 */
public abstract class AbstractEventHandleRegistrationManager<T extends EventHandler> implements IEventHandleRegistrationManager<T>, PropertyChangeListener{
	private final List<ServiceRegistration<EventHandler>> registrations = new ArrayList<ServiceRegistration<EventHandler>>();
	
	@Override
	public void addServiceRegistration(ServiceRegistration<EventHandler> registration) {
		synchronized (registrations) {
			this.registrations.add(registration);
		}
	}
	
	@Override
	public List<ServiceRegistration<EventHandler>> getAndClearServiceRegistration (){
		List<ServiceRegistration<EventHandler>> copyRegistrations;
		synchronized (registrations) {
			registrations.stream().forEach(registration->{
				
			});
			
			copyRegistrations = new ArrayList<ServiceRegistration<EventHandler>>(registrations);
			
			registrations.clear();
		}
		
		return copyRegistrations;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// handle ManagerEventConsumer deactive event
		OsigEventhandleUtil.unRegistryAllEvent(this);
		
	}
}