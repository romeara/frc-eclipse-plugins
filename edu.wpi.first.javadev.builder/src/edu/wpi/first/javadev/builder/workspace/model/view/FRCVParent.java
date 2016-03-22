package edu.wpi.first.javadev.builder.workspace.model.view;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.IFRCElementParent;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.library.FRCLElement;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRElement;

/**
 * Parent which contains view elements to display the model in the workspace
 * 
 * @author Ryan O'Meara
 */
public class FRCVParent extends FRCVElement implements IFRCElementParent<FRCVElement> {
	private boolean passEvents;
	private boolean disposing;
	private boolean logEvents;
	protected ArrayList<FRCVElement> children;
	protected ArrayList<FRCModelEvent> loggedEvents;
	
	protected FRCVParent(){
		super();
		initialize();
	}
	
	public FRCVParent(String name, ModelElementType inputOrder){
		super(name, ModelElementType.getParentType(name, inputOrder.getOrder()));
		initialize();
	}
	
	public FRCVParent(FRCElement associatedElement){
		super(associatedElement);
		initialize();
	}
	
	protected void initialize(){
		passEvents = true;
		disposing = false;
		logEvents = false;
		children = new ArrayList<FRCVElement>();
		loggedEvents = new ArrayList<FRCModelEvent>();
	}
	
	@Override
	protected void updateEnabled(ICompilationUnit newCompUnit, 
			boolean parentEnabled){
		boolean oldEnabled = getEnabled();
		
		disableEventPassing();
		clearEventLog();
		enableEventLogging();
		
		if(element == NO_ASSOC_ELEMENT){
			enabled = parentEnabled;
		}else if(element instanceof FRCLElement){
			enabled = true;
		}else if(element instanceof FRCRElement){
			enabled = ((FRCRElement)element)
			.isVisibleFrom(newCompUnit, parentEnabled);
		}
		
		for(FRCVElement currentElement : children){
			currentElement.updateEnabled(newCompUnit, enabled);
		}
		
		if((element == NO_ASSOC_ELEMENT)||(!(element instanceof FRCLElement))){
			for(FRCVElement currentElement : children){
				if(currentElement.getEnabled()){
					enabled = true;
					break;
				}
			}
		}
		
		if(element == NO_ASSOC_ELEMENT){
			boolean anyEnabled = false;
			for(FRCVElement currentElement : children){
				if(currentElement.getEnabled()){
					anyEnabled = true;
					break;
				}
			}
			enabled = anyEnabled;
		}
		
		disableEventLogging();
		
		FRCModelEvent enableEvent = new FRCModelEvent(this, 
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_ENABLED,
				"Enabled updated for " + getElementName(),
				loggedEvents.toArray(new FRCModelEvent[loggedEvents.size()]));
		
		enableEventPassing();
		
		if((oldEnabled != getEnabled())||(loggedEvents.size() > 0)){notifyListeners(enableEvent);}
		
		clearEventLog();
	}
	
	@Override
	public String getElementName(){
		if(element == NO_ASSOC_ELEMENT){return "{VP:" + displayName + "}";}
		
		return "{VP:" + element.getElementName() + "}";
	}
	
	@Override
	public boolean canModify(){
		//Defaults to true because of "containers" for devices and such
		if(element != null){return element.canModify();}
		
		return true;
	}
	
	@Override
	public ModelElementType getElementType(){
		if(element != NO_ASSOC_ELEMENT){
			return element.getElementType();
		}
		
		if(children.size() > 0){
			FRCVElement highestPriority = children.get(0);
			
			for(FRCVElement currentElement : children){
				if(currentElement.getElementType().getOrder() 
						< highestPriority.getElementType().getOrder()){
					highestPriority = currentElement;
				}
			}
			
			return highestPriority.getElementType();
		}
		
		return ModelElementType.LASTORDERTYPE;
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		if(element != null){reconcile(element.getViewModel());}
		
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
		//Nothing needs to be done, only view needs to execute an action,
		//all data that might need update gets an update from its element
		//being updated
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			disableEventPassing();
			clearEventLog();
			enableEventLogging();
			
			FRCVParent vPar = (FRCVParent)updateTo;
			String workspaceFQN = "";
			
			if((vPar.getView() == null)&&(getView() != null)){
				workspaceFQN = getView().getFullyQualifiedName() + ".";
			}else if((vPar.getFRCModel() == null)&&(getFRCModel() != null)){
				workspaceFQN = getFRCModel().getFullyQualifiedName() + ".";
			}
			
			super.runReconcile(vPar);
			
			ArrayList<FRCVElement> toRemove = new ArrayList<FRCVElement>();
			
			for(FRCVElement child : children){
				try{
				if(vPar.findChild(child.getFullyQualifiedName()
						.substring(workspaceFQN.length())) == null){
					toRemove.add(child);
				}
				}catch(Exception e){toRemove.add(child);}
			}
			
			//Remove after to avoid messing up for loop by removing from what
			//it is iterating over
			remove(toRemove.toArray(new FRCVElement[toRemove.size()]));
		
			for(FRCVElement curVEl : vPar.getChildren()){
				FRCVElement inThis;
				
				if((inThis = findChild(workspaceFQN + curVEl.getFullyQualifiedName())) != null){
					inThis.reconcile(curVEl);
				}else{
					add(curVEl);
				}
			}
			
			FRCView view = getView();
			if(view != null){
				view.updateEnabled(FRCModel.getActiveJavaFile(), false);
			}
			
			disableEventLogging();
			
			FRCModelEvent reconcileEvent = new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled",
					loggedEvents.toArray(new FRCModelEvent[loggedEvents.size()]));
			
			enableEventPassing();
			
			notifyListeners(reconcileEvent);
			
			clearEventLog();
		}
		
		return true;
	}
	
	@Override
	public void dispose(){
		disposeActionStart();
		super.dispose();
		
		disableEventPassing();
		
		loggedEvents.clear();
		
		for(FRCVElement curDis : children){curDis.dispose();}
		children.clear();
		
		enableEventPassing();
		disposeActionFinish();
	}
	
	@Override
	public void add(FRCVElement element){
		element.setParent(this);
		children.add(element);
		
		disableEventPassing();
		FRCView view = getView();
		if(view != null){
			view.updateEnabled(FRCModel.getActiveJavaFile(), false);
		}
		
		enableEventPassing();
		
		notifyListeners(new FRCModelEvent(
			this, 
			FRCModelEvent.FT_CHILDREN_ADDED, 
			element.getElementName() + " added to " + getElementName(),
			new FRCModelEvent[]{new FRCModelEvent(
					element,
					FRCModelEvent.FT_ADDED,
					"Added to " + getElementName())}));
	}
	
	@Override
	public void add(FRCVElement[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		ArrayList<FRCModelEvent> events = new ArrayList<FRCModelEvent>();
		disableEventPassing();
		for(FRCVElement addition : elements){
			add(addition);
			elementNames += "(" + addition.getElementName() + ")";
			events.add(new FRCModelEvent(
					addition,
					FRCModelEvent.FT_ADDED,
					"Added to " + getElementName()));
		}
		if(lastState){enableEventPassing();}
		notifyListeners(new FRCModelEvent(
			this, 
			FRCModelEvent.FT_CHILDREN_ADDED, 
			elementNames + " added to " + getElementName(),
			events.toArray(new FRCModelEvent[events.size()])));
		
	}
	
	@Override
	public void remove(FRCVElement element){
		if(children.remove(element)){
			notifyListeners(new FRCModelEvent(
					this, 
					FRCModelEvent.FT_CHILDREN_REMOVED, 
					element.getElementName() + " removed from " + getElementName()));
			disposeActionStart();
			element.dispose();
			disposeActionFinish();
		}
	}
	
	@Override
	public void remove(FRCVElement[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		disableEventPassing();
		for(FRCVElement removal : elements){
			remove(removal);
			elementNames += "(" + removal.getElementName() + ")";
		}
		if(lastState){enableEventPassing();}
		notifyListeners(new FRCModelEvent(
			this, 
			FRCModelEvent.FT_CHILDREN_REMOVED, 
			elementNames + " removed from " + getElementName()));
	}
	
	@Override
	public FRCVElement[] getChildren(){
		return children.toArray(new FRCVElement[children.size()]);
	}
	
	@Override
	public FRCVElement findChild(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		if(fullyQualifiedName.equals(getFullyQualifiedName())){return this;}
		
		for(FRCVElement curEl : children){
			if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return curEl;
			}
		}
		
		return null;
	}
	
	@Override
	public FRCVElement findChildDeep(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		FRCVElement found = null;
		
		if((found = findChild(fullyQualifiedName)) != null){return found;}
		
		for(FRCVElement curEl : children){
			if(curEl instanceof FRCVParent){
				if((found = ((FRCVParent)curEl)
						.findChildDeep(fullyQualifiedName)) != null){
					return found;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public FRCElement findElement(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		return findChildDeep(fullyQualifiedName);
	}
	
	@Override
	public FRCElement findElement(IJavaElement element){
		FRCElement found = null;
		
		if(definedByElement(element)){return this;}
		
		for(FRCElement curEl : children){
			if(curEl instanceof FRCVParent){
				if((found = ((FRCVParent)curEl)
						.findElement(element)) != null){
					return found;
				}
			}else if(curEl.definedByElement(element)){
				return curEl;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean contains(String fullyQualifiedName){
		for(FRCVElement curEl : children){
			if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean containsDeep(String fullyQualifiedName){
		if(contains(fullyQualifiedName)){return true;}
		
		for(FRCVElement curEl : children){
			if(curEl instanceof FRCVParent){
				if(((FRCVParent)curEl).containsDeep(fullyQualifiedName)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void enableEventPassing(){passEvents = true;}
	
	@Override
	public void disableEventPassing(){passEvents = false;}
	
	@Override
	public void disposeActionStart(){disposing = true;}
	
	@Override
	public void disposeActionFinish(){disposing = true;}
	
	@Override
	public void enableEventLogging() {logEvents = true;}
	
	@Override
	public void disableEventLogging() {logEvents = false;}
	
	@Override
	public void clearEventLog() {loggedEvents.clear();}
	
	@Override
	public boolean isDisposing(){return disposing;}
	
	@Override
	public void receiveEvent(FRCModelEvent event){
		if((element != NO_ASSOC_ELEMENT)&&(element.equals(event.getNotifier()))){
			if(event.getType() == FRCModelEvent.FT_DISPOSED){
				this.dispose();
				return;
			}else if(event.getKind() == FRCModelEvent.FK_REBUILT){
				this.rebuild();
				return;
			}
		}
		
		if(event.getType() == FRCModelEvent.FT_DISPOSED){
			if((contains(event.getNotifier().getFullyQualifiedName()))
					&&(event.getNotifier() instanceof FRCVElement)){
				if(!isDisposing()){
					children.remove(event.getNotifier());
					notifyListeners(new FRCModelEvent(
							this,
							FRCModelEvent.FT_CHILDREN_REMOVED,
							"Removed disposed child"));
				}
				return;
			}
		}else if((event.getType() == FRCModelEvent.FT_CHILDREN_ADDED)
				||(event.getType() == FRCModelEvent.FT_CHILDREN_REMOVED)
				||((event.getType() == FRCModelEvent.FT_DATA_CHANGE)
						&&(event.getKind() == FRCModelEvent.FK_RECONCILE))){
			if((element != NO_ASSOC_ELEMENT)&&(event.getNotifier().equals(element))){
				reconcile(event.getNotifier().getViewModel());
				return;
			}
		}
		
		notifyListeners(event);	
	}
	
	@Override
	public void notifyListeners(FRCModelEvent event){
		if(passEvents){super.notifyListeners(event);}
		
		if(logEvents){
			if(event.getType() == FRCModelEvent.FT_CHILDREN_ADDED){
				if(event.getChildEvents() != null){
					for(FRCModelEvent current : event.getChildEvents()){
						loggedEvents.add(current);
					}
				}
			}else{
				loggedEvents.add(event);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if((super.equals(obj))&&(obj instanceof FRCVParent)){
			FRCVParent vPar = (FRCVParent)obj;
			
			if(vPar.getChildren().length == getChildren().length){
				for(FRCVElement curVEl : vPar.getChildren()){
					if(!children.contains(curVEl)){return false;}
				}
			}else{
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		if(children != null){hash = children.hashCode();}
		return super.hashCode() + hash;
	}
}
