package edu.wpi.first.javadev.builder.workspace.model.library;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Represents a method in the project library
 * 
 * @author Ryan O'Meara
 */
public class FRCLMethod extends FRCLElement {
	protected IMethod method;
	
	protected FRCLMethod(IMethod javaMethod){
		super();
		method = javaMethod;
	}
	
	@Override
	public String getElementName(){
		if(method == null){return "NullMethod";}
		
		String elementName = "";
		
		try{
			elementName += method.getReturnType();
			elementName += method.getElementName();
			elementName += "(";
			
			String[] pTypes = method.getParameterTypes();
			
			for(String currentPType : pTypes){elementName += currentPType;}
			
			elementName += ")";
		}catch(Exception e){elementName = "MethodNameError";}
		
		return elementName;
	}
	
	@Override
	public String getDisplayName(){
		return ModelBuilderUtil.createDisplayName(method);
	}
	
	@Override
	public boolean canModify(){
		return ((getParent() != null)&&(((FRCElement)getParent()).canModify()));
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCLMETHOD;
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
		if(method == null){return false;}
		
		return element.equals(method);
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Handled by parents, elements on order of compilation units
		if((method == null)||(!method.exists())){
			rebuild();
			return;
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCLMethod meth = (FRCLMethod)updateTo;
			method = null;
			method = meth.method;
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((method != null)&&(method.getClassFile() == null)){
			try {
				JavaUI.openInEditor(method);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		//This processes public static methods correctly
		return ModelBuilderUtil.createMethodCodeFragment(method);
	}
	
	@Override
	public FRCVElement getViewModel(){
		return new FRCVElement(this);
	}
	
	@Override
	public void dispose(){
		super.dispose();
		method = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if((obj instanceof FRCLMethod)&&super.equals(obj)){
			FRCLMethod meth = (FRCLMethod)obj;
			
			if((method == null)&&(meth.method == null)){
				return true;
			}
			
			if((method != null)&&(meth.method != null)){
				return method.equals(meth.method);
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(method != null){hash += method.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCLMethod safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCLMethod
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IMethod){
			IMethod meth = (IMethod)candidate;
			
			try{
				int flags = meth.getFlags();
				
				return Flags.isPublic(flags)&&Flags.isStatic(flags);
			}catch(Exception e){return false;}
		}
		
		return false;
	}
	
}
