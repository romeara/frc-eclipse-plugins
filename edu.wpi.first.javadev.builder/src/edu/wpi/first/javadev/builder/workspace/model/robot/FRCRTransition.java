package edu.wpi.first.javadev.builder.workspace.model.robot;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.editor.CodeAndDiagramEditor;
import edu.wpi.first.javadev.builder.editor.StateMachineEditor;
import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Represents a transition in the robot model
 * 
 * @author Ryan O'Meara
 */
public class FRCRTransition extends FRCRElement{
	protected ISourceRange tranSrcRng;
	protected IType declaringType;
	protected FRCRState originState;
	protected FRCRState destState;
	
	protected FRCRTransition(FRCRState startState, FRCRState endState, 
			IType decType, ISourceRange sourcerange){
		super();
		tranSrcRng = sourcerange;
		declaringType = decType;
		originState = startState;
		destState = endState;
	}
	
	/**
	 * @return The origin state of this transition
	 */
	public FRCRState getOriginState(){return originState;}
	
	/**
	 * @return The destination state of this transition
	 */
	public FRCRState getDestinationState(){return destState;}
	
	/**
	 * @return The IFile this transition is in
	 */
	private IFile getFile(){
		if(declaringType == null){return null;}
		
		try{
			return (IFile)declaringType.getCompilationUnit().getUnderlyingResource();
		}catch(Exception e){return null;}
	}
	
	/**
	 * @return The source range of the transition
	 */
	public ISourceRange getSourceRange(){
		return tranSrcRng;
	}
	
	@Override
	public void removeFromCode(){
		//TODO FRCRTransition: removeFromCode: Method Stub
	}
	
	@Override
	public boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled){
		if(declaringType != null){
			return ModelBuilderUtil.isSameCompilationUnit(compUnit,
					declaringType.getCompilationUnit());
		}
		
		return false;
	}
	
	@Override
	public String getElementName(){
		String retName = "{TRAN:";
		
		if(declaringType != null){
			retName += declaringType.getElementName() + ":";
		}else{
			retName += "NULLDECLARINGTYPE:";
		}
		
		if(originState != null){
			retName += originState.getElementName() + ":";
		}else{
			retName += "NULLORIGINSTATE:";
		}
		
		if(destState != null){
			retName += destState.getElementName();
		}else{
			retName += "NULLDESTSTATE";
		}
		
		retName += "}";
		
		return retName;
	}
	
	@Override
	public String getDisplayName(){
		String retName = "";
		
		if(declaringType != null){
			retName += declaringType.getElementName() + " : ";
		}else{
			retName += "NULLDECLARINGTYPE : ";
		}
		
		if(originState != null){
			retName += originState.getDisplayName() + "->";
		}else{
			retName += "NULLORIGINSTATE->";
		}
		
		if(destState != null){
			retName += destState.getDisplayName();
		}else{
			retName += "NULLDESTSTATE";
		}
		
		return retName;
	}
	
	@Override
	public boolean canModify(){
		//TODO FRCRTransition: canModify: Method Stub
		return false;
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCRTRANSITION;
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
		//Transitions are declared in, but not defined by, a java element
		return false;
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Will be handled from state machine
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCRTransition tran = (FRCRTransition)updateTo;
			declaringType = null;
			tranSrcRng = null;
			declaringType = tran.declaringType;
			tranSrcRng = tran.tranSrcRng;
			
			//Don't have to reconcile States as they reconcile independently
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((declaringType != null)&&(declaringType.getClassFile() == null)){
			try {
				IFile openFile = getFile();
				IEditorDescriptor desc = PlatformUI.getWorkbench()
				.getEditorRegistry().getDefaultEditor(openFile.getName());
				
				IEditorPart opened = CodeViewerPlugin.getDefault().getPage()
				.openEditor(new FileEditorInput(openFile),desc.getId());
	
				if (opened instanceof CodeAndDiagramEditor) {
					((CodeAndDiagramEditor) opened).focusOn(this);
				} else if (opened instanceof StateMachineEditor) {
					((StateMachineEditor) opened).focusOn(this);
				} else if (opened instanceof ITextEditor) {
					if (tranSrcRng != null) {
						((ITextEditor) opened).setHighlightRange(
								tranSrcRng.getOffset(), 
								tranSrcRng.getLength(), 
								true);
					}
				}
	
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){return null;}
	
	@Override
	public FRCVParent getViewModel(){
		FRCVParent tranDisplay = new FRCVParent(this);
		
		if(originState != null){
			tranDisplay.add(originState.getViewModel());
		}
		
		if(destState != null){
			tranDisplay.add(destState.getViewModel());
		}
		
		return tranDisplay;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		declaringType = null;
		tranSrcRng = null;
		originState = null;
		destState = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof FRCRTransition){
			FRCRTransition tran = (FRCRTransition)obj;
			
			if(!((tran.declaringType == null)&&(declaringType == null))){
				if(!((declaringType != null)&&(tran.declaringType != null))){
					if(!declaringType.equals(tran.declaringType)){
						return false;
					}
				}
			}
			
			if(!((tran.tranSrcRng == null)&&(tranSrcRng == null))){
				if(!((tranSrcRng != null)&&(tran.tranSrcRng != null))){
					if(!tranSrcRng.equals(tran.tranSrcRng)){
						return false;
					}
				}
			}
			
			if((originState != null)&&(!(originState.equals(tran.originState)))){
				return false;
			}
			
			if((destState != null)&&(!(destState.equals(tran.destState)))){
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(declaringType != null){hash += declaringType.hashCode();}
		
		if(tranSrcRng != null){hash += tranSrcRng.hashCode();}
		
		if(originState != null){hash += originState.hashCode();}
		
		if(destState != null){hash += destState.hashCode();}
		
		return super.hashCode() + hash;
	}
	
}
