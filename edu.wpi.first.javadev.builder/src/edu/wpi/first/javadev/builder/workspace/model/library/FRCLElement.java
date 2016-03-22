package edu.wpi.first.javadev.builder.workspace.model.library;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;

/**
 * Base element for FRC library model elements
 * 
 * @author Ryan O'Meara
 */
public abstract class FRCLElement extends FRCElement {
	protected static final int FRCL_HASH_SEED = 2000;
	
	protected FRCLElement(){super();}
	
	/**
	 * Returns the library this element is a part of.  An FRCLibrary will return 
	 * itself
	 * @return FRCLibrary containing this element
	 */
	public FRCLibrary getLibrary(){
		FRCElement currentParent = (FRCElement)getParent();
		
		while((currentParent != null)&&(!(currentParent instanceof FRCLibrary))){
			currentParent = (FRCElement)currentParent.getParent();
		}
		
		if(currentParent == null){return null;}
		
		return (FRCLibrary)currentParent;
	}
	
	/**
	 * @return An IType, if applicable, to use to add a device to an element in
	 * the robot model.  Returns null if not applicable for this library element
	 */
	public IType getType(){return null;}
	
	@Override
	protected abstract void runUpdate(IJavaElement element, ASTNode node);
	
	@Override
	public boolean equals(Object obj){
		if((obj != null)&&(obj instanceof FRCLElement)){return true;}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		return FRCL_HASH_SEED + super.hashCode();
	}
	
}
