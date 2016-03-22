package edu.wpi.first.javadev.builder.workspace.model;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface which indicates that the given element has rename support,
 * for two types of things:  a type and a field
 * 
 * @author Ryan O'Meara
 */
public interface IComplexRename {
	/**
	 * Renames the given element's field with the given name 
	 * @param shell The shell to use to rename the element
	 * @param newName The new name to give the element
	 * @return true is successful, false otherwise
	 */
	public abstract boolean renameField(Shell shell, String newName);
	
	/**
	 * Renames the given element's type with the given name 
	 * @param shell The shell to use to rename the element
	 * @param newName The new name to give the element
	 * @return true is successful, false otherwise
	 */
	public abstract boolean renameClass(Shell shell, String newName);
	
	/**
	 * Returns the name a rename operation for a field would replace
	 * @return The current field name
	 */
	public abstract String getFieldName();
	
	
	/**
	 * Returns the name a rename operation for a class would replace
	 * @return The current class name
	 */
	public abstract String getClassName();
}
