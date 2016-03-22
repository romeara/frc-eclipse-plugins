package edu.wpi.first.javadev.builder.workspace.model;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface implemented by elements which have rename support
 * for one element only
 * 
 * @author Ryan O'Meara
 */
public interface IStandardRename {
	/**
	 * Renames the given element with the given name 
	 * @param shell The shell to use to rename the element
	 * @param newName The new name to give the element
	 * @return true is successful, false otherwise
	 */
	public abstract boolean rename(Shell shell, String newName);
	
	/**
	 * Return the current name that a rename would replace
	 * @return The current name
	 */
	public abstract String getName();
	
	/**
	 * Gets the type of element that this rename will act on
	 * @return
	 */
	public abstract int getRenameType();
}
