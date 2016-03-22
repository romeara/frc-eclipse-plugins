package edu.wpi.first.javadev.builder.workspace.model.robot;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.IComplexRename;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

public class FRCRState extends FRCRElement implements IComplexRename{
	protected IField stateField;
	protected IType stateType;
	
	protected FRCRState(IField inputField){
		stateField = inputField;
		stateType = ModelBuilderUtil.createIFieldType(inputField);
	}
	
	/**
	 * @param state The state to compare names with
	 * @return true if the given state and this state have the same name, 
	 * false otherwise
	 */
	public boolean sameName(Object state) {
		return (state instanceof FRCRState ? 
				((FRCRState) state).getElementName().equals(getElementName()) 
				: false);
	}
	
	@Override
	public boolean renameField(Shell shell, String newName){
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(stateField, newName, conditions);

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
	public boolean renameClass(Shell shell, String newName){
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(stateType, newName, conditions);

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
	public String getFieldName(){
		if(stateField != null){
			return stateField.getElementName();
		}
		
		return null;
	}
	
	@Override
	public String getClassName(){
		if(stateType != null){
			return stateType.getElementName();
		}
		
		return null;
	}
	
	/**
	 * @return The state name that can be compared to an AST SimpleName
	 */
	public String getSimpleName(){
		if(stateField != null){return stateField.getElementName();}
		
		return null;
	}
	
	@Override
	public void removeFromCode(){
		try{
			if(stateField != null){
				IJavaModel jm = stateField.getJavaModel();
				jm.delete(new IJavaElement[]{stateField}, false, null);
			}
		}catch(Exception e){}
		
		dispose();
	}
	
	@Override
	public boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled){
		//Visible if:  unit is its or its type's compilation unit, field is public
		//and its parent is enabled, or field is public and static
		if(compUnit != null){
			if((stateField != null)&&(stateField.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit,
						stateField.getCompilationUnit())){
					return true;
				}
			}
			
			if((stateType != null)&&(stateType.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit,
						stateType.getCompilationUnit())){
					return true;
				}
			}
		}
		
		if(stateField != null){
			try{
				int flags = stateField.getFlags();
				
				if(Flags.isPublic(flags)){
					return parentEnabled || Flags.isStatic(flags);
				}
			}catch(Exception e){return false;}
		}
		
		return false;
	}
	
	@Override
	public String getElementName(){
		String name = "{S:";
		
		if(stateType != null){
			name += stateType.getElementName() + ":";
		}else{
			name += "NULLTYPE:";
		}
		
		if(stateField != null){
			name += stateField.getElementName() + "}";
		}else{
			name += "NULLTYPE}";
		}
		
		return name;
	}
	
	@Override
	public String getDisplayName(){
		String name = "";
		
		if(stateField != null){name = stateField.getElementName();}
		
		if(stateType != null){name += " : " + stateType.getElementName();}
		
		return name;
	}
	
	@Override
	public boolean canModify(){
		if((stateField != null)&&(stateField.getCompilationUnit() != null)){
			return true;
		}
		
		return false;
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCRSTATE;
	}
	
	@Override
	public void rebuild(){
		if((stateField == null)||(!stateField.exists())){
			((FRCElement)getParent()).rebuild();
		}else{
			reconcile(new FRCRState(stateField));
		}
		
		notifyListeners(new FRCModelEvent(
				this,
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_REBUILT,
				"Rebuilt " + getElementName()));
	}
	
	@Override
	public boolean definedByElement(IJavaElement element){
		if((stateType == null)&&(stateField == null)){return false;}
		
		if(stateType != null){
			if(element.equals(stateType)){return true;}
		}
		
		if(stateField != null){
			return element.equals(stateField);
		}
		
		return false;
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Only need to check against types comp unit, since fields are handled
		//by parent
		if((stateType == null)||(!stateType.exists())){
			rebuild();
			return;
		}
		
		if(element != null){
			if(element instanceof ICompilationUnit){
				ICompilationUnit unit = (ICompilationUnit)element;
				if((stateType != null)&&(stateType.getCompilationUnit() != null)){
					if(ModelBuilderUtil.isSameCompilationUnit(unit,
							stateType.getCompilationUnit())){
						if(stateField != null){
							//Use field, since children are still processed, 
							//and keeps proper associations
							FRCRState recState = new FRCRState(stateField);
							reconcile(recState);
							return;
						}
					}
				}
			}
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCRState state = (FRCRState)updateTo;
			stateField = null;
			stateType = null;
			stateField = state.stateField;
			stateType = state.stateType;
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((stateType != null)&&(stateType.getClassFile() == null)){
			try {
				JavaUI.openInEditor(stateType);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		if(stateField != null){return stateField.getElementName();}
		
		return null;
	}
	
	@Override
	public FRCVElement getViewModel(){return new FRCVElement(this);}
	
	@Override
	public void dispose(){
		super.dispose();
		stateField = null;
		stateType = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if((super.equals(obj))&&(obj instanceof FRCRState)){
			FRCRState state = (FRCRState)obj;
			boolean retValF = false;
			boolean retValT = false;
			
			if((state.stateField == null)&&(stateField == null)){
				retValF = true;
			}else if((state.stateField != null)&&(stateField != null)){
				if(state.stateField.equals(stateField)){
					retValF = true;
				}
			}
			
			
			if((state.stateType == null)&&(stateType == null)){
				retValT = true;
			}else if((state.stateType != null)&&(stateType != null)){
				if(state.stateType.equals(stateType)){
					retValT = true;
				}
			}
			
			return retValT && retValF;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(stateType != null){hash += stateType.hashCode();}
		if(stateField != null){hash += stateField.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCRTransition safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCRTransition
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IField){
			IType sType = ModelBuilderUtil.createIFieldType((IField)candidate);
			
			// ~ Determine if the type extends state
			try {
				IType prevType = null;
				while (sType != null && !sType.equals(prevType)) {
					for (String name : ParseConstants.STATE_ID) {
						if (name.equals(sType.getElementName())) return true;
					}
					prevType = sType;
					sType = ModelBuilderUtil.getJavaElement(sType, sType.getSuperclassTypeSignature());
				}

				return sType != null;
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
}
