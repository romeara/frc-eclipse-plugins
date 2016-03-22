package edu.wpi.first.javadev.builder.workspace.model.library;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.IFRCElementParent;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;

/**
 * Represents a parent of other library elements
 * 
 * @author Ryan O'Meara
 */
public abstract class FRCLParent extends FRCLElement implements
		IFRCElementParent<FRCLElement> {
	private boolean passEvents;
	private boolean disposing;
	private boolean logEvents;
	
	protected ArrayList<FRCModelEvent> loggedEvents;
	
	protected ArrayList<FRCLElement> children;
	
	public FRCLParent(){
		super();
		disposing = false;
		passEvents = true;
		logEvents = false;
		children = new ArrayList<FRCLElement>();
		loggedEvents = new ArrayList<FRCModelEvent>();
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		for(FRCLElement curEl : children){curEl.update(element, node);}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			disableEventPassing();
			clearEventLog();
			enableEventLogging();
			
			FRCLParent lPar = (FRCLParent)updateTo;
			String workspaceProjFQN = "";
			
			if((lPar.getFRCProject() == null)&&(getFRCProject() != null)){
				workspaceProjFQN = getFRCProject().getFullyQualifiedName() + ".";
			}else if((lPar.getFRCModel() == null)&&(getFRCModel() != null)){
				workspaceProjFQN = getFRCModel().getFullyQualifiedName() + ".";
			}
			
			ArrayList<FRCLElement> toRemove = new ArrayList<FRCLElement>();
			
			for(FRCLElement child : children){
				if(lPar.findChild(child.getFullyQualifiedName()
						.substring(workspaceProjFQN.length())) == null){
					toRemove.add(child);
				}
			}
			
			//Remove after to avoid messing up for loop by removing from what
			//it is iterating over
			remove(toRemove.toArray(new FRCLElement[toRemove.size()]));
			
			for(FRCLElement curLEl : lPar.getChildren()){
				FRCLElement inThis;
				
				if((inThis = findChild(workspaceProjFQN 
						+ curLEl.getFullyQualifiedName())) != null){
					inThis.reconcile(curLEl);
				}else{
					add(curLEl);
				}
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
		
		for(FRCLElement curDis : children){curDis.dispose();}
		children.clear();
		
		loggedEvents.clear();
		
		enableEventPassing();
		disposeActionFinish();
	}
	
	@Override
	public void add(FRCLElement element){
		element.setParent(this);
		children.add(element);
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
	public void add(FRCLElement[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		ArrayList<FRCModelEvent> events = new ArrayList<FRCModelEvent>();
		disableEventPassing();
		for(FRCLElement addition : elements){
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
	public void remove(FRCLElement element){
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
	public void remove(FRCLElement[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		disableEventPassing();
		for(FRCLElement removal : elements){
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
	public FRCLElement[] getChildren(){
		return children.toArray(new FRCLElement[children.size()]);
	}
	
	@Override
	public FRCLElement findChild(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		if(fullyQualifiedName.equals(getFullyQualifiedName())){return this;}
		
		for(FRCLElement curEl : children){
			if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return curEl;
			}
		}
		
		return null;
	}
	
	@Override
	public FRCLElement findChildDeep(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		FRCLElement found = null;
		
		if((found = findChild(fullyQualifiedName)) != null){return found;}
		
		for(FRCLElement curEl : children){
			if(curEl instanceof FRCLParent){
				if((found = ((FRCLParent)curEl)
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
			if(curEl instanceof FRCLParent){
				if((found = ((FRCLParent)curEl)
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
		for(FRCLElement curEl : children){
			if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean containsDeep(String fullyQualifiedName){
		if(contains(fullyQualifiedName)){return true;}
		
		for(FRCLElement curEl : children){
			if(curEl instanceof FRCLParent){
				if(((FRCLParent)curEl).containsDeep(fullyQualifiedName)){
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
		if(event.getType() == FRCModelEvent.FT_DISPOSED){
			if((contains(event.getNotifier().getFullyQualifiedName()))
					&&(event.getNotifier() instanceof FRCLElement)){
				if(!isDisposing()){
					children.remove(event.getNotifier());
					notifyListeners(new FRCModelEvent(
							this,
							FRCModelEvent.FT_CHILDREN_REMOVED,
							"Removed disposed child"));
				}
				return;
			}
		}
		
		if(passEvents){notifyListeners(event);}
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
		if((obj instanceof FRCLParent)&&(super.equals(obj))){
			FRCLParent lPar = (FRCLParent)obj;
			
			if(lPar.getChildren().length == getChildren().length){
				for(FRCLElement curLEl : lPar.getChildren()){
					if(!children.contains(curLEl)){return false;}
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
