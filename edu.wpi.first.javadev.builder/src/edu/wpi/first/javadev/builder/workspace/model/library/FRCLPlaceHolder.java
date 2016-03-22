package edu.wpi.first.javadev.builder.workspace.model.library;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Allows the model to be constructed even with circular references (device/mechanism
 * having a reference to itself as a field or some such)
 * 
 * @author Ryan O'Meara
 */
public class FRCLPlaceHolder extends FRCLElement {
	protected FRCLParent copy;
	
	protected FRCLPlaceHolder(FRCLParent copied){copy = copied;}

	@Override
	public String getElementName() {return copy.getElementName();}
	
	@Override
	public String getDisplayName(){return copy.getDisplayName();}
	
	@Override
	public boolean canModify(){return false;}

	@Override
	public ModelElementType getElementType() {return copy.getElementType();}

	@Override
	public void rebuild(){copy.rebuild();}
	
	@Override
	public boolean definedByElement(IJavaElement element){
		//library element itself, not a place holder, should say that it is 
		//defined by a java element
		return false;
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node) {
		//Do Nothing, as only a parent (which already called this on itself)
		//would require a place holder
	}

	@Override
	protected boolean runReconcile(FRCElement updateTo) {
		//Do Nothing, as only a parent (which already called this on itself)
		//would require a place holder
		return true;
	}

	@Override
	public boolean openInEditor() {return copy.openInEditor();}

	@Override
	public String getCodeFragment() {return copy.getCodeFragment();}

	@Override
	public FRCVElement getViewModel() {return new FRCVElement(this);}
	
	@Override
	public void dispose(){
		super.dispose();
		copy = null;
	}

}
