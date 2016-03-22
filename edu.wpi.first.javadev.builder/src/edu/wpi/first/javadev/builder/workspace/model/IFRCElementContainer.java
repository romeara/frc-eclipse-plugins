package edu.wpi.first.javadev.builder.workspace.model;

import org.eclipse.jdt.core.IJavaElement;

import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;

public interface IFRCElementContainer extends IFRCModelEventListener{
	
	/**
	 * Find the FRCElement defined by the given fully qualified name
	 * @param fullyQualifiedName The fully qualified name of the element 
	 * to find
	 * @return The associated FRCElement, if any
	 */
	public abstract FRCElement findElement(String fullyQualifiedName);
	
	/**
	 * Find the FRCElement defined by the given IJavaElement
	 * @param element The element to search with
	 * @return The associated FRCElement, if any
	 */
	public abstract FRCElement findElement(IJavaElement element);
	
	/** Turns on passing events up the hierarchy */
	public abstract void enableEventPassing();
	
	/** Turns off passing events up the hierarchy */
	public abstract void disableEventPassing();
	
	/** Turns on flag indicating that the parent is currently disposing
	 * a child */
	public abstract void disposeActionStart();
	
	/** Turns off flag indicating that the parent is currently disposing
	 * a child */
	public abstract void disposeActionFinish();
	
	/**Enables event logging within the element, which stores events that 
	 * are or would have been sent */
	public abstract void enableEventLogging();
	
	/**Disables event logging within the element, which stores events that 
	 * are or would have been sent */
	public abstract void disableEventLogging();
	
	/** Clears the accumulated log of events in the element */
	public abstract void clearEventLog();
	
	/**
	 * @return true if the parent is currently disposing a child, false 
	 * otherwise
	 */
	public abstract boolean isDisposing();
}
