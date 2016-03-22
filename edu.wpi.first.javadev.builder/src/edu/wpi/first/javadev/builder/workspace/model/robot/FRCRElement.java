package edu.wpi.first.javadev.builder.workspace.model.robot;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;

/**
 * Represents a robot element 
 * 
 * @author Ryan O'Meara
 */
public abstract class FRCRElement extends FRCElement{
	protected static final int FRCR_HASH_SEED = 1000;
	
	protected FRCRElement(){super();}
	
	/**
	 * Determines if this element is visible from the given compilation unit.
	 * Takes into account element visibility.  Only accounts for this 
	 * individual element, and whether its parent being enabled means that it is.
	 * (Ex:  public element will return true even if the compilation unit does
	 * not contain it, if its parent is enabled)
	 * @param compUnit The compilation unit to check against
	 * @param parentEnabled true if this element's parent is enabled, false otherwise
	 * @return true if the item would be visible in the compilation unit
	 */
	public abstract boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled);
	
	/**
	 * Removes this element from the java file that contains it.  Robots cannot be
	 * removed.
	 */
	public abstract void removeFromCode();
	
	/**
	 * Retrieves the closest parent of this element which is an FRCRDevice,
	 * FRCRMechanism, or FRCRobot.  FRCRDevices and FRCRMechanisms will not 
	 * return themselves, but the closest parent which fits the criteria. An
	 * FRCRobot will return itself
	 * @return FCRDevice, FRCRMechanism, or FRCRobot which is the parent of 
	 * this element
	 */
	public FRCRDevice getParentDevice(){
		FRCElement currentParent = (FRCElement)getParent();
		
		while((currentParent != null)&&(!(currentParent instanceof FRCRDevice))){
			currentParent = (FRCElement)currentParent.getParent();
		}
		
		if(currentParent == null){return null;}
		
		return (FRCRDevice)currentParent;
	}
	
	/**
	 * Retrieves the closest parent of this element which is an 
	 * FRCRMechanism or FRCRobot.  FRCRMechanisms will not return themselves,
	 * but the closest parent which fits the criteria. An FRCRobot will 
	 * return itself
	 * @return FRCRMechanism or FRCRobot which is the parent of 
	 * this element
	 */
	public FRCRMechanism getParentMechanism(){
		FRCElement currentParent = (FRCElement)getParent();
		
		while((currentParent != null)&&(!(currentParent instanceof FRCRMechanism))){
			currentParent = (FRCElement)currentParent.getParent();
		}
		
		if(currentParent == null){return null;}
		
		return (FRCRMechanism)currentParent;
	}
	
	/**
	 * Retrieves the robot which this element is a part of.  An FRCRobot
	 * will return itself
	 * @return FRCRobot which this element is ultimately part of, or null if
	 * it could not be found
	 */
	public FRCRobot getRobot(){
		FRCRDevice currentParent = getParentDevice();
		
		while((currentParent != null)&&(!(currentParent instanceof FRCRobot))){
			currentParent = currentParent.getParentDevice();
		}
		
		if(currentParent == null){return null;}
		
		return (FRCRobot)currentParent;
	}
	
	@Override
	protected abstract void runUpdate(IJavaElement element, ASTNode node);
	
	@Override
	public boolean equals(Object obj){
		if((obj != null)&&(obj instanceof FRCRElement)){return true;}
		
		return false;
	}
	
	@Override
	public int hashCode(){return FRCR_HASH_SEED + super.hashCode();}
}
