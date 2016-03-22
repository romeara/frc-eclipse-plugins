package edu.wpi.first.javadev.builder.workspace.model.robot;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.util.StringUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.IStandardRename;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Represents a capability (method) in the robot model
 * 
 * @author Ryan O'Meara
 */
public class FRCRCapability extends FRCRElement implements IStandardRename{
	protected IMethod capabilityMethod;
	
	protected FRCRCapability(IMethod methodDeclaration){
		super();
		capabilityMethod = methodDeclaration;
	}
	
	@Override
	public boolean rename(Shell shell, String newName) {
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(capabilityMethod, newName, conditions);

			if (!rename.preCheck().isOK()) return false;

			String oldElementName = getElementName();
			
			rename.perform(shell, new ProgressMonitorDialog(null));
			
			notifyListeners(new FRCModelEvent(
					this,
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RENAME,
					oldElementName + " was renamed to " + getElementName()));

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public String getName() {
		if(capabilityMethod != null){
			return capabilityMethod.getElementName();
		}
		
		return null;
	}

	@Override
	public int getRenameType() {return FRCModel.METHOD_RENAME;}
	
	@Override
	public void removeFromCode(){
		try{
			if(capabilityMethod != null){
				IJavaModel jm = capabilityMethod.getJavaModel();
				jm.delete(new IJavaElement[]{capabilityMethod}, false, null);
			}
		}catch(Exception e){}
		
		dispose();
	}
	
	@Override
	public boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled){
		//Returns true if this is the compilation unit of the method, if the parent
		//is enabled and the method is public, or the method is public and static
		if(capabilityMethod != null){
			if((compUnit != null
					)&&(capabilityMethod.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit, 
						capabilityMethod.getCompilationUnit())){
					return true;
				}
			}
			
			try{
				int flags = capabilityMethod.getFlags();
				
				if(Flags.isPublic(flags)){
					if(Flags.isStatic(flags) || parentEnabled){
						return true;
					}
				}
				
				if(Flags.isProtected(flags)
						&&ModelBuilderUtil.inSamePackage(
								compUnit, 
								capabilityMethod.getDeclaringType()
								.getCompilationUnit())){
					return true;
				}
				
			}catch(Exception e){return false;}
		}
		
		return false;
	}
	
	@Override
	public String getElementName(){
		if(capabilityMethod == null){return "NullMethod";}
		
		String elementName = "";
		
		try{
			elementName += capabilityMethod.getReturnType();
			elementName += capabilityMethod.getElementName();
			elementName += "(";
			
			String[] pTypes = capabilityMethod.getParameterTypes();
			
			for(String currentPType : pTypes){elementName += currentPType;}
			
			elementName += ")";
		}catch(Exception e){elementName = "MethodNameError";}
		
		return elementName;
	}
	
	@Override
	public String getDisplayName(){
		return ModelBuilderUtil.createDisplayName(capabilityMethod);
	}
	
	@Override
	public boolean canModify(){
		if((capabilityMethod != null)&&(capabilityMethod.getCompilationUnit() != null)){
			return true;
		}
		
		return false;
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCRCAPABILITY;
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
		if(capabilityMethod == null){return false;}
		
		return element.equals(capabilityMethod);
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//No action needed, if anything needs update, will be replaced by
		//containing device
		if((capabilityMethod == null)||(!capabilityMethod.exists())){
			rebuild();
			return;
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			capabilityMethod = null;
			capabilityMethod = ((FRCRCapability)updateTo).capabilityMethod;
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((capabilityMethod != null)
				&&(capabilityMethod.getClassFile() == null)){
			try {
				JavaUI.openInEditor(capabilityMethod);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		if(capabilityMethod != null){
			try{
				int flags = capabilityMethod.getFlags();
				String methodCode = ModelBuilderUtil
				.createMethodCodeFragment(capabilityMethod);
				
				//if private, never needs instance name, default return is fine
				if(Flags.isPrivate(flags)){
					return methodCode;
				}else{
					//if protected, any class which is is or extends it needs 
					//no front, but any class in the same package will need 
					//that front
					//public methods could need no front in their own file, or 
					//extended class
					if(ModelBuilderUtil.isClass(
							FRCModel.getActiveJavaFile().findPrimaryType(), 
							capabilityMethod.getDeclaringType()
							.getFullyQualifiedName())){
						return StringUtil.replaceFirst(methodCode, 
								FRCModel.INSERT_INSTANCE_NAME + ".", 
								"");
					}
					
					//In same package requires a full prefix, same as a public
					String prefix = getRobot().getCapabilityCodeFragment(this);
					
					if(prefix == null){return null;}
					
					if(prefix.equals("")){
						return StringUtil.replaceFirst(methodCode, 
								FRCModel.INSERT_INSTANCE_NAME + ".", 
								"");
					}
					
					return StringUtil.replaceFirst(methodCode, 
							FRCModel.INSERT_INSTANCE_NAME, 
							prefix);
				}
			
			}catch(Exception e){return null;}
		}
	
		return null;
	}
	
	@Override
	public FRCVElement getViewModel(){
		if(capabilityMethod != null){
			return new FRCVElement(this);
		}
		
		return null;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		capabilityMethod = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof FRCRCapability){
			FRCRCapability cap = (FRCRCapability)obj;
			if((capabilityMethod == null)&&(cap.capabilityMethod == null)){
				return true;
			}
			
			if((capabilityMethod != null)&&(cap.capabilityMethod != null)){
				if(capabilityMethod.equals(cap.capabilityMethod)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(capabilityMethod != null){hash += capabilityMethod.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCRCapability safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCRCapability
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IMethod){
			IMethod method = (IMethod)candidate;
			
			try{
				int flags = method.getFlags();
				if((method.isConstructor())
						&&((Flags.isPrivate(flags))
								||(Flags.isProtected(flags)))){
					return false;
				}
				
				if(Flags.isStatic(flags)){return false;}
				
				return true;
			}catch(Exception e){return false;}
		}
		
		return false;
	}	
}
