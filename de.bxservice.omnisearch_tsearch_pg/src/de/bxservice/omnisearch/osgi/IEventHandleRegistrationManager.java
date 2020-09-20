package de.bxservice.omnisearch.osgi;

import java.util.List;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;

/**
 * manage ServiceRegistration of EventHandle
 * @author hieplq
 *
 * @param <T>
 */
public interface IEventHandleRegistrationManager<T extends EventHandler>{
	public void addServiceRegistration(ServiceRegistration<EventHandler> registration);
	
	public List<ServiceRegistration<EventHandler>> getAndClearServiceRegistration ();
	
	public T getEventHandleObj ();
}