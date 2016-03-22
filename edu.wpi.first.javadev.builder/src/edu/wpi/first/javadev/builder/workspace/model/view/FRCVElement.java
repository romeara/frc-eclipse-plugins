package edu.wpi.first.javadev.builder.workspace.model.view;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.FRCProject;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;
import edu.wpi.first.javadev.builder.workspace.model.library.FRCLElement;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRElement;

/**
 * Base element for displaying the model in the workspace
 * 
 * @author Ryan O'Meara
 */
public class FRCVElement extends FRCElement implements IFRCModelEventListener{
	protected static final int FRCV_HASH_SEED = 3000;
	public static final FRCElement NO_ASSOC_ELEMENT = null;
	
	protected FRCElement element;
	protected boolean enabled;
	protected String displayName;
	protected ModelElementType mType;
	
	protected FRCVElement(){
		super();
		element = NO_ASSOC_ELEMENT;
		enabled = false;
		displayName = "";
		mType = ModelElementType.LASTORDERTYPE;
	}
	
	public FRCVElement(String name, ModelElementType inputOrder){
		this(name);
		mType = inputOrder;
	}
	
	public FRCVElement(FRCElement associatedElement){
		if(associatedElement != NO_ASSOC_ELEMENT){
			element = associatedElement;
			element.addListener(this);
		}
	}
	
	private FRCVElement(String name){
		this();
		if(name != null){displayName = name;}
	}
	
	/**
	 * Returns whether this element should be displayed as enabled or not
	 * @return true if the element should display as enabled, false otherwise
	 */
	public boolean getEnabled(){return enabled;}
	
	/**
	 * Updates whether this view element should be enabled.  View elements
	 * displaying library elements are always enabled.  Robot displaying view
	 * elements are enabled when the item they represent is visible (and 
	 * therefore usable in) the currently active ICompilationUnit (Java source
	 * file)
	 * @param newCompUnit The ICompilationUnit that is currently active
	 * @param parentEnabled whether or not the parent would be enabled,
	 * independent of its children's enabled state
	 */
	protected void updateEnabled(ICompilationUnit newCompUnit, 
			boolean parentEnabled){
		boolean oldEnabled = getEnabled();
		
		if(element == NO_ASSOC_ELEMENT){
			enabled = false;
		}else if(element instanceof FRCLElement){
			enabled = true;
		}else if(element instanceof FRCRElement){
			enabled = ((FRCRElement)element)
			.isVisibleFrom(newCompUnit, parentEnabled);
		}
		
		if(oldEnabled != getEnabled()){notifyListeners(
				new FRCModelEvent(this, 
						FRCModelEvent.FT_DATA_CHANGE, 
						FRCModelEvent.FK_ENABLED, 
						getElementName() + " switched to " + getEnabled() 
						+ " enabled state"));}
		
	}
	
	/**
	 * The Associated element of this view object
	 * @return The element associated, or null if none
	 */
	public FRCElement getModelElement(){
		return element;
	}
	
	/**
	 * @return true if this view element can be dragged into either view
	 */
	public boolean isDraggable(){
		return getEnabled()&&(isDraggableIntoRobotView()||isDraggableIntoEditor());
	}
	
	/**
	 * @return true if this element can be dragged into the robot view
	 */
	public boolean isDraggableIntoRobotView(){
		return ((element != NO_ASSOC_ELEMENT)
				&&(element.getElementType().equals(
						ModelElementType.FRCLDEVICE)));
	}
	
	/**
	 * @return true if this element can be dragged into the editor window
	 */
	public boolean isDraggableIntoEditor(){
		return ((element != NO_ASSOC_ELEMENT)&&(element.getCodeFragment() != null));
	}
	
	/**
	 * @return The view this element is a part of, or null if none
	 */
	public FRCView getView(){
		if(!(getParent() instanceof FRCVParent)){return null;}
		
		FRCVParent currentParent = (FRCVParent)getParent();
		
		while((currentParent != null)&&(!(currentParent instanceof FRCView))){
			currentParent = (FRCVParent)currentParent.getParent();
		}
		
		if(currentParent == null){return null;}
		
		return (FRCView)currentParent;
	}
	
	@Override
	public String getElementName(){
		if(element == NO_ASSOC_ELEMENT){return "{VE:" + displayName + "}";}
		
		return "{VE:" + element.getElementName() + "}";
	}
	
	@Override
	public String getDisplayName(){
		if(element == NO_ASSOC_ELEMENT){return displayName;}
		
		return element.getDisplayName();
	}
	
	@Override
	public boolean canModify(){
		return ((element != null)&&(element.canModify()));
	}
	
	@Override
	public ModelElementType getElementType(){
		if(element != NO_ASSOC_ELEMENT){
			return element.getElementType();
		}
		
		if(mType != null){return mType;}
		
		return ModelElementType.LASTORDERTYPE;
	}
	
	@Override
	public void rebuild(){
		if(element != null){reconcile(element.getViewModel());}
		
		notifyListeners(new FRCModelEvent(
				this,
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_REBUILT,
				"Rebuilt " + getElementName()));
	}
	
	@Override
	public boolean definedByElement(IJavaElement element){return false;}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Nothing needs to be done, only view needs to execute an action,
		//all data that might need update gets an update from its element
		//being updated
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCVElement vEl = (FRCVElement)updateTo;
			if(element != NO_ASSOC_ELEMENT){
				element.removeListener(this);
			}
			element = vEl.element;
			
			if(element != NO_ASSOC_ELEMENT){
				element.addListener(this);
			}
			
			displayName = vEl.displayName;
			mType = vEl.mType;
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if(element != NO_ASSOC_ELEMENT){return element.openInEditor();}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		if(element != NO_ASSOC_ELEMENT){return element.getCodeFragment();}
		
		return null;
	}
	
	@Override
	public FRCVElement getViewModel(){return this;}
	
	@Override
	public FRCProject getFRCProject(){
		//Views are never part of a project
		return null;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		if(element != NO_ASSOC_ELEMENT){
			element.removeListener(this);
		}
		element = null;
	}

	@Override
	public void receiveEvent(FRCModelEvent event) {
		if((element != null)&&(element.equals(event.getNotifier()))){
			if(event.getType() == FRCModelEvent.FT_DISPOSED){
				if((getParent() != null)&&(getParent() instanceof FRCVParent)){
					((FRCVParent)getParent()).remove(this);
				}else{
					this.dispose();
				}
			}else if(event.getKind() == FRCModelEvent.FK_REBUILT){
				this.rebuild();
			}
		}
	}
	
	@Override
	public boolean equals(Object obj){
		if((obj != null)&&(obj instanceof FRCVElement)){
			FRCVElement vEl = (FRCVElement)obj;
			
			if(element == null){
				if(vEl.element != null){return false;}
			}else{
				if(vEl.element == null){return false;}
			}
			
			if((element == null)||(vEl.element.equals(element))){
				if(displayName == null){
					if(vEl.displayName != null){return false;}
				}else{
					if(vEl.displayName == null){return false;}
				}
				
				if((displayName == null)||(vEl.displayName.equals(displayName))){
					if(mType == null){
						if(vEl.mType != null){return false;}
					}else{
						if(vEl.mType == null){return false;}
					}
					
					if((mType == null)||(vEl.mType.equals(mType))){return true;}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int eHash = 0;
		if(element != NO_ASSOC_ELEMENT){eHash += element.hashCode();}
		if(displayName != null){eHash += displayName.hashCode();}
		if(mType != null){eHash += mType.hashCode();}
		return FRCV_HASH_SEED + super.hashCode() + eHash; 
	}
	
}
