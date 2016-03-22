package edu.wpi.first.javadev.builder.workspace.model.view;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.FRCProject;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;

/**
 * Element which contains a view model to display in the workspace
 * 
 * @author Ryan O'Meara
 */
public class FRCView extends FRCVParent {
	
	public FRCView(){super();}
	
	public FRCView(String name){
		super(name, ModelElementType.getParentType(name, 0));
	}
	
	@Override
	public boolean getEnabled(){return true;}
	
	@Override
	public void updateEnabled(ICompilationUnit newCompUnit, 
			boolean parentEnabled){
		disableEventPassing();
		clearEventLog();
		enableEventLogging();
		
		for(FRCVElement child : children){
			child.updateEnabled(newCompUnit, false);
		}
		
		enableEventPassing();
		
		disableEventLogging();
		
		FRCModelEvent enableEvent = new FRCModelEvent(this, 
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_ENABLED,
				"Enabled updated for " + getElementName(),
				loggedEvents.toArray(new FRCModelEvent[loggedEvents.size()]));
		
		enableEventPassing();
		
		if(loggedEvents.size() > 0){notifyListeners(enableEvent);}
		
		clearEventLog();
	}
	
	@Override
	public FRCView getView(){return this;}
	
	@Override
	public String getElementName(){return "{View:" + displayName + "}";}
	
	@Override
	public boolean canModify(){return true;}
	
	@Override
	public ModelElementType getElementType(){
		return new ModelElementType(
				"FRC View", 
				ModelElementType.LASTORDERTYPE.getIconPath(), 
				3);
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		if(displayName.equals(FRCModel.ROBOT_VIEW_NAME)){
			reconcile(getFRCModel().getActiveProject().getRobotViewModel());
		}else if(displayName.equals(FRCModel.PALETTE_VIEW_NAME)){
			reconcile(getFRCModel().getActiveProject().getLibraryViewModel());
		}
		
		disableEventLogging();
		enableEventPassing();
		
		notifyListeners(new FRCModelEvent(
				this,
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_REBUILT,
				"Rebuilt " + getElementName(),
				loggedEvents.toArray(new FRCModelEvent[loggedEvents.size()])));
		
		clearEventLog();
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Does nothing; this affects the code model only, and any changes there
		//which require reconcile will be triggered by events from the model, 
		//which the model will call
	}
	
	
	@Override
	public boolean equals(Object obj){
		if(super.equals(obj)&&(obj instanceof FRCView)){return true;}
		
		return false;
	}
	
	@Override
	public void receiveEvent(FRCModelEvent event){
		if(event.getNotifier() instanceof FRCModel){
			if(event.getType() == FRCModelEvent.FT_DATA_CHANGE){
				if(event.getKind() == FRCModelEvent.FK_ACTIVE_PROJ_CHANGE){
					
					FRCProject activeProject = getFRCModel().getActiveProject();
					
					if(activeProject != null){
						
						if(getElementName().indexOf(FRCModel.ROBOT_VIEW_NAME) != -1){
							FRCView view = activeProject.getRobotViewModel();
							if(view != null){
								reconcile(view);
							}else{
								reconcile(getFRCModel().getNoRobotView(getElementName()));
							}
						}else if(getElementName().indexOf(FRCModel.PALETTE_VIEW_NAME) != -1){
							FRCView view = activeProject.getLibraryViewModel();
							if(view != null){
								reconcile(view);
							}else{
								reconcile(getFRCModel().getNoRobotView(getElementName()));
							}
						}
					}else{
						reconcile(getFRCModel().getNoRobotView(getElementName()));
					}
					
					ICompilationUnit activePage = FRCModel.getActiveJavaFile();
					
					if(activePage != null){
						updateEnabled(activePage, false);
					}
					
				}else if(event.getKind() == FRCModelEvent.FK_ACTIVE_PAGE_CHANGE){
					ICompilationUnit activePage = FRCModel.getActiveJavaFile();
					
					if(activePage != null){
						updateEnabled(activePage, false);
					}
				}
			}
		}else{
			if((event.getNotifier() instanceof FRCVElement)
					&&((event.getType() == FRCModelEvent.FT_CHILDREN_ADDED)
							||(event.getType() == FRCModelEvent.FT_ADDED)
							||(event.getType() == FRCModelEvent.FK_RECONCILE))){
				ICompilationUnit activePage = FRCModel.getActiveJavaFile();
				
				if(activePage != null){
					updateEnabled(activePage, false);
				}
			}
			
			super.receiveEvent(event);
		}
	}
	
}
