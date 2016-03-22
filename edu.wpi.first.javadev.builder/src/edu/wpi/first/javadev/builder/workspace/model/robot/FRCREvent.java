package edu.wpi.first.javadev.builder.workspace.model.robot;

import org.eclipse.jdt.core.ICompilationUnit;
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
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.IStandardRename;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Represents an event in the robot model
 * 
 * @author Ryan O'Meara
 */
public class FRCREvent extends FRCRElement implements IStandardRename{
	protected IType eventType;  //class that extends Event
	
	protected FRCREvent(IType eventClass){
		eventType = eventClass;
	}
	
	@Override
	public boolean rename(Shell shell, String newName){
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(eventType, newName, conditions);

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
		if(eventType != null){
			return eventType.getElementName();
		}
		
		return null;
	}

	@Override
	public int getRenameType() {return FRCModel.TYPE_RENAME;}
	
	@Override
	public void removeFromCode(){
		try{
			if(eventType != null){
				IJavaModel jm = eventType.getJavaModel();
				jm.delete(new IJavaElement[]{eventType}, false, null);
			}
		}catch(Exception e){}
		
		dispose();
	}
	
	@Override
	public boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled){
		if(eventType != null){
			if((eventType.getCompilationUnit() != null)&&(compUnit != null)){
				return ModelBuilderUtil.isSameCompilationUnit(compUnit, 
						eventType.getCompilationUnit());
			}
		}
		
		return false;
	}
	
	@Override
	public String getElementName(){
		if(eventType != null){
			return "{E:" + eventType.getElementName() + "}";
		}
		
		return "{E:NULLEVENT}";
	}
	
	@Override
	public String getDisplayName(){
		if(eventType != null){
			return eventType.getElementName();
		}
		
		return "NULL_EVENT";
	}
	
	@Override
	public boolean canModify(){
		if(eventType.getCompilationUnit() != null){return true;}
		
		return false;
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCREVENT;
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
		if(eventType == null){return false;}
		
		return element.equals(eventType);
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		if((eventType == null)||(!eventType.exists())){
			rebuild();
			return;
		}
		
		if(element instanceof ICompilationUnit){
			ICompilationUnit comp = (ICompilationUnit)element;
			if(eventType != null){
				if(ModelBuilderUtil.isSameCompilationUnit(comp, 
						eventType.getCompilationUnit())){
					reconcile(new FRCREvent(comp.findPrimaryType()));
				}
			}
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCREvent ev = (FRCREvent)updateTo;
			
			eventType = null;
			eventType = ev.eventType;
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}

		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((eventType != null)&&(eventType.getClassFile() == null)){
			try {
				JavaUI.openInEditor(eventType);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		//TODO FRCREvent: getCodeFragment: Method Stub
		return null;
	}
	
	@Override
	public FRCVElement getViewModel(){
		return new FRCVElement(this);
	}
	
	@Override
	public void dispose(){
		super.dispose();
		eventType = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if(super.equals(obj)&&(obj instanceof FRCREvent)){
			FRCREvent ev = (FRCREvent)obj;
			
			if((eventType == null)&&(ev.eventType == null)){return true;}
			
			if((eventType != null)&&(ev.eventType != null)){
				return ev.eventType.equals(eventType);
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int eventHash = 0;
		
		if(eventType != null){eventHash = eventType.hashCode();}
		
		return super.hashCode() + eventHash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCREvent safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCREvent
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IType){
			//TODO make this use only full name once inserted into WPILibJ
			IType type = (IType)candidate;
			try {
				IType prevType = null;
				while (type != null && !type.equals(prevType)) {
					for (String name : ParseConstants.EVENT) {
						if (name.equals(type.getElementName())) return true;
					}
					prevType = type;
					type = ModelBuilderUtil.getJavaElement(type, type.getSuperclassTypeSignature());
				}

				return type != null;
			} catch (JavaModelException e) {
				e.printStackTrace();
			}

		}
		
		return false;
	}
	
}
