package edu.wpi.first.javadev.builder.workspace.model.event;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;

/**
 * Event which indicates a change in an FRC model.  Events notify of changes
 * such as the addition or removal of children, reconciling, disposing, active
 * project changes, and active page changes.  They have a type and kind, which
 * provide two levels of description about what triggered the event
 * 
 * @author Ryan O'Meara
 */
public class FRCModelEvent {
	//Event types
	/**Sent by a parent when it has children added */
	public static final int FT_CHILDREN_ADDED = 0;
	/** Sent by a parent when it has children removed */
	public static final int FT_CHILDREN_REMOVED = 1;
	/** Sent by an element when its data changes.  More specific information 
	 * about the type of change is described in the event kind */
	public static final int FT_DATA_CHANGE = 2;
	/** Sent by an element when it is disposed.  These events are internal to 
	 * the model.  disposed elements are removed from their parent, which 
	 * generates a visible event */
	public static final int FT_DISPOSED = 3;
	/** Sent by a child when it is added to a parent.  This event is found as
	 * a sub event of events of type {@link FT_CHILDREN_ADDED} */
	public static final int FT_ADDED = 4;
	/** Sent by a child when it is removed from a parent.  This event is found as
	 * a sub event of events of type {@link FT_CHILDREN_REMOVED}, if the child
	 * was not disposed */
	public static final int FT_REMOVED = 5;
	/** Sent by a FRCRElement when a new element is added to the robot model
	 * which other models may want to be particularly aware of, such as the
	 * library model's interest in new devices */
	public static final int FT_ADDED_NEW_ELEMENT = 6;
	/** Sent by a FRCRElement when an element is removed from the robot model
	 * which other models may want to be particularly aware of, such as the
	 * library model's interest in removed devices */
	public static final int FT_REMOVED_ELEMENT = 7;
	
	//Event kinds
	/** Generic descriptor which is the kind for any event which has no 
	 * additional information to convey via the kind field */
	public static final int FK_NONE = 10;
	/** Kind for an event which is the result of the element being reconciled */
	public static final int FK_RECONCILE = 11;
	/** Kind of an event which notifies of changes to its instance data */
	public static final int FK_INSTANCE_DATA = 12;
	/** Kind of an event which notifies of changes of name */
	public static final int FK_RENAME = 13;
	/** Kind which indicates a change in the enabled state of the element.  Used
	 * in the view model */
	public static final int FK_ENABLED = 14;
	/** Kind of event sent by the model when the active project in workspace
	 * has changed */
	public static final int FK_ACTIVE_PROJ_CHANGE = 15;
	/** Kind of event sent by the model when the active page in the workspace 
	 * has changed */
	public static final int FK_ACTIVE_PAGE_CHANGE = 16;
	/** Kind of event where the associated element was rebuilt */
	public static final int FK_REBUILT = 17;
	
	private int type;	//The type of event.  add, remove, or data change
	private int kind;	//The kind of event (if type is data change).  
						//reconcile, instance data
	
	private FRCElement notifier;	//The FRCElement which generated the event
	private String message;			//Message sent with the event to describe it
	
	private FRCModelEvent[] childEvents;
	
	public FRCModelEvent(FRCElement inputNotifier, int inputType, String inputMessage){
		notifier = inputNotifier;
		type = inputType;
		message = inputMessage;
		kind = FK_NONE;
		childEvents = null;
	}
	
	public FRCModelEvent(FRCElement inputNotifier, int inputType, int inputKind, 
			String inputMessage){
		this(inputNotifier, inputType, inputMessage);
		kind = inputKind;
	}
	
	public FRCModelEvent(FRCElement inputNotifier, int inputType, String inputMessage,
			FRCModelEvent[] childEvent){
		this(inputNotifier, inputType, inputMessage);
		childEvents = childEvent;
	}
	
	public FRCModelEvent(FRCElement inputNotifier, int inputType, int inputKind, 
			String inputMessage, FRCModelEvent[] childEvent){
		this(inputNotifier, inputType, inputKind, inputMessage);
		childEvents = childEvent;
	}
	
	/**
	 * @return The element which generated the event
	 */
	public FRCElement getNotifier(){return notifier;}
	
	/**
	 * @return The type of event (either F_ADDED, F_REMOVED, or F_DATA_CHANGE)
	 */
	public int getType(){return type;}
	
	/**
	 * Returns the kind of data change, or F_NONE if the event is not a data 
	 * change event
	 * @return F_NONE if not of type F_DATA_CHANGE, otherwise F_RECONCILE or
	 * F_INSTANCE_DATA
	 */
	public int getKind(){return kind;}
	
	/**
	 * @return The message describing the event
	 */
	public String getMessage(){return message;}
	
	/**
	 * @return The array of children of this event, or null if none
	 */
	public FRCModelEvent[] getChildEvents(){return childEvents;}
	
	@Override
	public String toString(){
		String retString = getNotifier().getFullyQualifiedName() + " : ";
		
		switch(getType()){
		case FT_CHILDREN_ADDED:
			retString += "ADDED, ";
			break;
		case FT_CHILDREN_REMOVED:
			retString += "REMOVED, ";
			break;
		case FT_DATA_CHANGE:
			retString += "DATA_CHANGED, ";
			break;
		case FT_DISPOSED:
			retString += "DISPOSED, ";
			break;
		default:
			retString += "UNKNOWN:" + getType() + ", ";
			break;	
		}
		
		switch(getKind()){
		case FK_NONE:
			retString += "NONE;";
			break;
		case FK_RECONCILE:
			retString += "RECONCILE;";
			break;
		case FK_INSTANCE_DATA:
			retString += "INSTANCE_DATA;";
			break;
		case FK_ENABLED:
			retString += "ENABLED;";
			break;
		case FK_ACTIVE_PROJ_CHANGE:
			retString += "ACTIVE_PROJ_CHANGE;";
			break;
		case FK_ACTIVE_PAGE_CHANGE:
			retString += "ACTIVE_PAGE_CHANGE;";
			break;
		default:
			retString += "UNKNOWN:" + getKind() + ";";
			break;
		}
		
		if(childEvents != null){
			for(FRCModelEvent cur : childEvents){
				String curStr = cur.toString();
				
				if(curStr != null){
					retString += "\n\t" + curStr;
				}
			}
		}
		
		return retString;
	}
}
