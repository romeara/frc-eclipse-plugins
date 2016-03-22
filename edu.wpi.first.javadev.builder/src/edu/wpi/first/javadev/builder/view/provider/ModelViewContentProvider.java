package edu.wpi.first.javadev.builder.view.provider;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.wpi.first.javadev.builder.view.views.CodeTreeViewer;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventNotifier;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Updates and manages the content of the tree view
 * @author Ryan O'Meara
 */
public class ModelViewContentProvider implements ITreeContentProvider, IFRCModelEventListener{
	
	protected CodeTreeViewer viewer;
	protected int defExpandLevel;
	
	protected static final int DEFAULT_EXPAND = 3;
	
	public ModelViewContentProvider(int defaultExpand){
		defExpandLevel = defaultExpand;
		if(defExpandLevel < 1){defExpandLevel = DEFAULT_EXPAND;}
	}
	
	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (CodeTreeViewer)viewer;
		if(oldInput != null) {
			((IFRCModelEventNotifier)oldInput).removeListener(this);
		}
		if(newInput != null) {
			((IFRCModelEventNotifier)newInput).addListener(this);
		}
		this.viewer.refreshTree();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof FRCVParent) {
			FRCVParent parent = (FRCVParent)parentElement;
			return parent.getChildren();
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof FRCVElement){
			return ((FRCVElement)element).getParent();
		}
		return null;
	}

	@Override
	public void receiveEvent(FRCModelEvent event) {
		//Must refresh full tree
		viewer.expandToLevel(defExpandLevel); 
		
		
		//TODO This requires a breakdown on the reconcile process with child events
		if((event.getChildEvents() != null)
				&&(((event.getType() == FRCModelEvent.FT_DATA_CHANGE)
				&&(event.getKind() == FRCModelEvent.FK_RECONCILE))
				||(event.getType() == FRCModelEvent.FT_CHILDREN_ADDED))){
			for(FRCModelEvent current : event.getChildEvents()){
				if(current.getType() == FRCModelEvent.FT_ADDED){
					viewer.expandToLevel(current.getNotifier(), 2);
				}
			}
		}
		
		viewer.refreshTree();
	}
	
	public String toString(){
		return "ModelViewContentProvider";
	}
}
