package edu.wpi.first.javadev.builder.workspace.model;


/**
 * Interface implemented by any FRCElement wishing to be a parent (contain 
 * other FRCElements).  This is only used by elements which have a dynamic
 * set of children based on the model, not for elements with an always constant,
 * known set of children
 * 
 * @author Ryan O'Meara
 */
public interface IFRCElementParent<ElementType extends FRCElement> extends IFRCElementContainer{
	
	/**
	 * Adds the given FRCElement to the parent
	 * @param element The element to add
	 */
	public abstract void add(ElementType element);
	
	/**
	 * Adds the given FRCElements to the parent
	 * @param elements The elements to add
	 */
	public abstract void add(ElementType[] elements);
	
	/**
	 * Removes the given FRCElement from the parent by calling its dispose method
	 * @param element The element to remove, no effect if element not in parent
	 */
	public abstract void remove(ElementType element);
	
	/**
	 * Removes the given FRCElements from the parent by calling their dispose methods
	 * @param elements The element to remove, no effect if an element is not 
	 * in parent
	 */
	public abstract void remove(ElementType[] elements);
	
	/**
	 * @return An array of the children in the parent
	 */
	public abstract ElementType[] getChildren();
	
	/**
	 * Searches the parent for a child with the given fully qualified name,
	 * but not within its children's children
	 * @param fullyQualifiedName The fully qualified name of the child to 
	 * search for
	 * @return The element, if found, or null if  not found
	 */
	public abstract ElementType findChild(String fullyQualifiedName);
	
	/**
	 * Searches the parent for a child with the given fully qualified name,
	 * including within its children's children , and returns that child if 
	 * it exists
	 * @param fullyQualifiedName The fully qualified name of the child to 
	 * search for
	 * @return The element, if found, or null if  not found
	 */
	public abstract ElementType findChildDeep(String fullyQualifiedName);
	
	/**
	 * Determines if this parent contains the element with the given fully
	 * qualified name
	 * @param fullyQualifiedName The fully qualified name of the element to 
	 * search for
	 * @return true if this parent contains the given element, false otherwise
	 */
	public abstract boolean contains(String fullyQualifiedName);
	
	/**
	 * Determines if this parent contains the element with the given fully
	 * qualified name, or one of its children contain it
	 * @param fullyQualifiedName The fully qualified name of the element to 
	 * search for
	 * @return true if this parent or any of its children contains the given 
	 * element, false otherwise
	 */
	public abstract boolean containsDeep(String fullyQualifiedName);
}
