package edu.wpi.first.javadev.builder.workspace.model.robot;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.IFRCElementParent;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.robot.event.IFRCREventParticipant;

/**
 * Base class for any robot model elements who are parents
 * 
 * @author Ryan O'Meara
 */
public abstract class FRCRParent extends FRCRElement implements
		IFRCElementParent<FRCRElement>, IFRCREventParticipant {
	private boolean passEvents;
	private boolean disposing;
	private boolean logEvents;
	
	protected ArrayList<FRCModelEvent> loggedEvents;
	
	protected ArrayList<FRCRElement> children;
	
	public FRCRParent(){
		super();
		disposing = false;
		passEvents = true;
		logEvents = false;
		children = new ArrayList<FRCRElement>();
		loggedEvents = new ArrayList<FRCModelEvent>();
	}
	
	/**
	 * Find and return the prefix to a method call to the given capability
	 * if made from the active file.  Builds the prefix recursively until the
	 * direct parent of the capability is found.
	 * @param cap The FRCRCapabilty to build the prefix for
	 * @return The prefix if the capability is a child of this parent, or 
	 * null if it is not
	 */
	public abstract String getCapabilityCodeFragment(FRCRCapability cap);
	
	/**
	 * Determines if the candidate parent represents an instance of the
	 * same class as this parent
	 * @param cadidate The parent element to test
	 * @return true, if the given parent represents an instance of the same class
	 * as this parent, false otherwise.  Will return false if either does not represent
	 * a java type
	 */
	public abstract boolean isInstanceOfSame(FRCRParent cadidate);
	
	private FRCModelEvent runChildReconcile(FRCRParent rPar){
		disableEventPassing();
		clearEventLog();
		enableEventLogging();
		
		String workspaceProjFQN = "";
		
		if((rPar.getFRCProject() == null)&&(getFRCProject() != null)){
			workspaceProjFQN = getFRCProject().getFullyQualifiedName() + ".";
		}else if((rPar.getFRCModel() == null)&&(getFRCModel() != null)){
			workspaceProjFQN = getFRCModel().getFullyQualifiedName() + ".";
		}
		
		ArrayList<FRCRElement> toRemove = new ArrayList<FRCRElement>();
		
		for(FRCRElement child : children){
			try{
			if(rPar.findChild(child.getFullyQualifiedName()
					.substring(workspaceProjFQN.length())) == null){
				toRemove.add(child);
			}
			}catch(Exception e){toRemove.add(child);}
		}
		
		//Remove after to avoid messing up for loop by removing from what
		//it is iterating over
		remove(toRemove.toArray(new FRCRElement[toRemove.size()]));
		
		for(FRCRElement curREl : rPar.getChildren()){
			FRCRElement inThis;
			
			if((inThis = findChild(workspaceProjFQN 
					+ curREl.getFullyQualifiedName())) != null){
				inThis.reconcile(curREl);
			}else{
				add(curREl);
			}
		}
		
		disableEventLogging();
		
		FRCModelEvent reconcileEvent = new FRCModelEvent(this, 
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_RECONCILE,
				getElementName() + " reconciled",
				loggedEvents.toArray(new FRCModelEvent[loggedEvents.size()]));
		
		enableEventPassing();
		
		return reconcileEvent;
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		for(FRCRElement curEl : children){curEl.update(element, node);}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCModelEvent reconcileEvent = runChildReconcile((FRCRParent)updateTo);
			
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
		
		for(FRCRElement curDis : children){curDis.dispose();}
		children.clear();
		
		loggedEvents.clear();
		
		enableEventPassing();
		disposeActionFinish();
	}
	
	@Override
	public void add(FRCRElement element){
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
	public void add(FRCRElement[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		ArrayList<FRCModelEvent> events = new ArrayList<FRCModelEvent>();
		disableEventPassing();
		for(FRCRElement addition : elements){
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
	public void remove(FRCRElement element){
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
	public void remove(FRCRElement[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		disableEventPassing();
		for(FRCRElement removal : elements){
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
	public FRCRElement[] getChildren(){
		return children.toArray(new FRCRElement[children.size()]);
	}
	
	@Override
	public FRCRElement findChild(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		if(fullyQualifiedName.equals(getFullyQualifiedName())){return this;}
		
		for(FRCRElement curEl : children){
			if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return curEl;
			}
		}
		
		return null;
	}
	
	@Override
	public FRCRElement findChildDeep(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		FRCRElement found = null;
		
		if((found = findChild(fullyQualifiedName)) != null){return found;}
		
		for(FRCRElement curEl : children){
			if(curEl instanceof FRCRParent){
				if((found = ((FRCRParent)curEl)
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
			if(curEl instanceof FRCRParent){
				if((found = ((FRCRParent)curEl)
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
		for(FRCRElement curEl : children){
			if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean containsDeep(String fullyQualifiedName){
		if(contains(fullyQualifiedName)){return true;}
		
		for(FRCRElement curEl : children){
			if(curEl instanceof FRCRParent){
				if(((FRCRParent)curEl).containsDeep(fullyQualifiedName)){
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
			if((contains(event.getNotifier().getFullyQualifiedName()))){
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
		if((super.equals(obj))&&(obj instanceof FRCRParent)){
			FRCRParent rPar = (FRCRParent)obj;
			
			if(rPar.getChildren().length == getChildren().length){
				for(FRCRElement curREl : rPar.getChildren()){
					if(!children.contains(curREl)){return false;}
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
	
	@Override
	public void receiveRobotEvent(FRCModelEvent event){
		if(event.getNotifier() instanceof FRCRParent){
			if(isInstanceOfSame((FRCRParent)event.getNotifier())){
				int type = event.getType();
				
				if((type == FRCModelEvent.FT_CHILDREN_ADDED)
						||(type == FRCModelEvent.FT_CHILDREN_REMOVED)){
					FRCModelEvent reconcileEvent = 
						runChildReconcile((FRCRParent)event.getNotifier());
					
					notifyListeners(reconcileEvent);
					
					clearEventLog();
				}
			}
		}
		//In the case of the robot, "notify robot" means let everyone in the robot
		//know that the event occurred
		//Don't have to check for equals, as there is only one robot ever
		for(FRCRElement child : children){
			if(child instanceof FRCRParent){
				((FRCRParent)child).receiveRobotEvent(event);
			}
		}
	}
	
	@Override
	public void notifyRobot(FRCModelEvent event){
		if(getRobot() != null){
			getRobot().receiveRobotEvent(event);
		}
	}
}
