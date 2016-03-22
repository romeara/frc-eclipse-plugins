package edu.wpi.first.javadev.builder.workspace.model.event;

/**
 * Notifier interface implemented by any elements who wish to generate FRC
 * model change events
 * 
 * @author Ryan O'Meara
 */
public interface IFRCModelEventNotifier {
	/**
	 * Adds a listener to this element to be informed of changes
	 * @param newListener The listener to add
	 */
	public abstract void addListener(IFRCModelEventListener newListener);
	
	/**
	 * Removes a listener from the list of listeners to be notified
	 * @param revListener The listener to remove
	 */
	public abstract void removeListener(IFRCModelEventListener revListener);
	
	/**
	 * Notifies registered listeners 
	 * @param event The event to notify listeners of
	 */
	public abstract void notifyListeners(FRCModelEvent event);
}
