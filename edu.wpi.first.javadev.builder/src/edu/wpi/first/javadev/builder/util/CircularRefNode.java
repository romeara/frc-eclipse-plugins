package edu.wpi.first.javadev.builder.util;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Node in a linked list which is used to identify circular references when building 
 * the model.  This method is Thread-safe as specific information is used to check the 
 * correct path for circular references
 * 
 * @author Ryan O'Meara
 */
public class CircularRefNode {
	private CircularRefNode parent;
	private IJavaElement[] elements;
	private IJavaElement identifier;
	
	/**
	 * Creates a node with the given identifier (to match when searching) and description
	 * elements to compare against for circular referencing.  The identifier is included in
	 * the list of elements to compare against when searching for a circular reference
	 * @param identifier IJavaElement to use to search for this node
	 * @param description Elements compared against for circular reference
	 */
	public CircularRefNode(IJavaElement iidentifier, IJavaElement[] description){
		if(description != null){
		elements = new IJavaElement[description.length + 1];
			for(int i = 0; i <= description.length; i++){
				if(i < description.length){
					elements[i] = description[i];
				}else{
					elements[i] = identifier;
				}
			}
		}else{
			elements = new IJavaElement[]{identifier};
		}
		identifier = iidentifier;
		parent = null;
	}
	
	/**
	 * Creates a node with the given identifier (to match when searching) and description
	 * elements to compare against for circular referencing.  The identifier is included in
	 * the list of elements to compare against when searching for a circular reference
	 * @param identifier IJavaElement to use to search for this node
	 * @param description Elements compared against for circular reference
	 * @param parentNode The CircularReferenceNode to use as this node's parent
	 */
	public CircularRefNode(IJavaElement identifier, IJavaElement[] description, 
			CircularRefNode parentNode){
		this(identifier, description);
		parent = parentNode;
	}
	
	/** Frees node resources */
	@SuppressWarnings("unused")
	public void dispose(){
		if(elements != null){
			for(IJavaElement current : elements){current = null;}
		}
		
		parent = null;
	}
	
	/**
	 * Determines if this node is identified by the given element
	 * @param check The element to check against
	 * @return true if identified by the element, false otherwise
	 */
	public boolean identifiedBy(IJavaElement check){
		if((check == null)||(identifier == null)){return false;}
		
		return check.equals(identifier);
	}
	
	/**
	 * Determines if the given element is a circular reference to the path this
	 * node is a part of
	 * @param check The element to check against
	 * @return true if the element constitutes a circular reference, false otherwise
	 */
	public boolean isCircularReference(IJavaElement check){
		if(check == null){return false;}
		for(IJavaElement current : elements){
			if((current != null)&&(current.equals(check))){
				return true;
			}
		}
		
		if(parent != null){return parent.isCircularReference(check);}
		
		return false;
	}
}
