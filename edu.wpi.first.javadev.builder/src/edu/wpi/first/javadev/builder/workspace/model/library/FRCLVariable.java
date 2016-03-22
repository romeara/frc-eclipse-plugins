package edu.wpi.first.javadev.builder.workspace.model.library;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Represents a variable in the library of the project
 * 
 * @author Ryan O'Meara
 */
public class FRCLVariable extends FRCLElement {
	protected IField variableField;
	
	protected FRCLVariable(IField field){
		super();
		variableField = field;
	}
	
	@Override
	public String getElementName(){
		if(variableField != null){
			return "{V:" + variableField.getElementName() + "}";
		}
		
		return "{V:NULLFIELD}";
	}
	
	@Override
	public String getDisplayName(){
		if(variableField != null){
			try{
				return variableField.getElementName() 
				+ " : " + Signature.toString(variableField.getTypeSignature());
			}catch(Exception e){return variableField.getElementName();}
		}
		
		return "NULLFIELD";	
	}
	
	@Override
	public boolean canModify(){
		return ((getParent() != null)&&(((FRCElement)getParent()).canModify()));
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCLVARIABLE;
	}
	
	@Override
	public void rebuild(){
		((FRCElement)getParent()).rebuild();
		
		notifyListeners(new FRCModelEvent(
				this,
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_REBUILT,
				"Rebuilt " + getElementName()));
	}
	
	@Override
	public boolean definedByElement(IJavaElement element){
		if(variableField == null){return false;}
		
		return element.equals(variableField);
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Handled by parents, since elements are compilation units
		if((variableField == null)||(!variableField.exists())){
			rebuild();
			return;
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCLVariable var = (FRCLVariable)updateTo;
			variableField = null;
			variableField = var.variableField;
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((variableField != null)&&(variableField.getClassFile() == null)){
			try {
				JavaUI.openInEditor(variableField);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		ICompilationUnit unit = FRCModel.getActiveJavaFile();
		IType type = variableField.getDeclaringType();
		
		//Only use variable name in its own file
		if(unit != null){
			if(type.getCompilationUnit() != null){
				if(unit.equals(type.getCompilationUnit())){
					return variableField.getElementName();
				}
			}
		}
		
		if(type != null){
			return type.getElementName() + "." 
			+ variableField.getElementName();
		}
		
		return null;
	}
	
	@Override
	public FRCVElement getViewModel(){
		return new FRCVElement(this);
	}
	
	@Override
	public void dispose(){
		super.dispose();
		variableField = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if(super.equals(obj)&&(obj instanceof FRCLVariable)){
			FRCLVariable var = (FRCLVariable)obj;
			
			if((variableField == null)&&(var.variableField == null)){
				return true;
			}
			
			if((variableField != null)&&(var.variableField != null)){
				return variableField.equals(var.variableField);
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(variableField != null){hash += variableField.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCLVariable safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCLVariable
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IField){
			IField field = (IField)candidate;
			
			try{
				int flags = field.getFlags();
				
				return Flags.isPublic(flags)&&Flags.isStatic(flags)
						&&Flags.isFinal(flags);
			}catch(Exception e){return false;}
		}
		
		return false;
	}
	
}
