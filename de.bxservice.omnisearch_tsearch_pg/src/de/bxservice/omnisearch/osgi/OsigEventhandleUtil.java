package de.bxservice.omnisearch.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.adempiere.base.event.IEventManager;
import org.compiere.model.PO;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * EventHande can use DS to registry service http://blog.vogella.com/2017/05/16/osgi-event-admin-publish-subscribe
 * on idempiere sometime we need to dynamic build topic or filter up to condition (example plugin https://wiki.idempiere.org/en/Plugin:_Omnisearch)
 * function on this class help to program registry EventHandle
 * @author hieplq
 *
 */
public class OsigEventhandleUtil{
	/**
	 * registry many group of topic, each group has own filter
	 * @param <T> 
	 * @param bundleContext
	 * @param eventHandle
	 * @param clazz 
	 * @param eventInfos many group of topic, each group has own filter
	 */
	public static <T extends EventHandler> void registryEventHandle(BundleContext bundleContext, IEventHandleRegistrationManager<T> eventHandle, Class<T> clazz, Map<String[], String> eventInfos) {
		eventInfos.entrySet().stream().forEach(eventInfo -> {
			registryEventHandle(bundleContext, eventHandle, clazz, eventInfo.getValue(), eventInfo.getKey());
		});
	}
	
	/**
	 * registry list of topic with same filter to handle
	 * @param <T>
	 * @param bundleContext
	 * @param eventHandle
	 * @param clazz class of EventHandle object, because type erasure on generic, many place can't use T.getClass(), so need to pass class to use:https://docs.oracle.com/javase/tutorial/java/generics/erasure.html
	 * @param filter
	 * @param topics
	 */
	public static <T extends EventHandler> void registryEventHandle(BundleContext bundleContext, IEventHandleRegistrationManager<T> eventHandle, Class<T> clazz, String filter, String... topics) {
		Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();
		serviceProperties.put(EventConstants.EVENT_TOPIC, topics);
		if (filter != null)
			serviceProperties.put(EventConstants.EVENT_FILTER, filter);
		
		eventHandle.addServiceRegistration(bundleContext.registerService(EventHandler.class, eventHandle.getEventHandleObj(), serviceProperties));
	}
	
	/**
	 * clean all topic that event object listening for
	 * @param <T>
	 * @param eventHandle
	 */
	public static <T extends EventHandler> void unRegistryAllEvent(IEventHandleRegistrationManager<T> eventHandle) {
		eventHandle.getAndClearServiceRegistration().stream().forEach(registration -> {
			registration.unregister();
		});			
	}
	
	public static String filterByTableName (String tableName) {
		return "(tableName="+tableName+")";
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getEventProperty(Event event, String property) {
		return (T) event.getProperty(property);
	}
	
	/**
	 * @param event
	 * @return PO
	 */
	public static PO getPO(Event event) {
		return getEventProperty(event, IEventManager.EVENT_DATA);
	}
}