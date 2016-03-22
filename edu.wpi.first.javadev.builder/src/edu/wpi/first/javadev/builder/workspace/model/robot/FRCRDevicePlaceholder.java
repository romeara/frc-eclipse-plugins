package edu.wpi.first.javadev.builder.workspace.model.robot;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

public class FRCRDevicePlaceholder extends FRCRDevice {
	protected FRCRDevicePlaceholder(){super();}
	
	protected FRCRDevicePlaceholder(IField deviceDeclaration){
		super();
		deviceField = deviceDeclaration;
		deviceType = ModelBuilderUtil.createIFieldType(deviceField);
	}
	
	@Override
	public boolean canModify(){return false;}
	
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
	public void rebuild(){}
	
	@Override
	public FRCVParent getViewModel() {return new FRCVParent(this);}

}
