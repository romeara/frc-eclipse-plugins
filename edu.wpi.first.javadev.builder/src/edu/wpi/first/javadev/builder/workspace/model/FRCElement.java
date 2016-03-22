package edu.wpi.first.javadev.builder.workspace.model;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventNotifier;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Base element for all FRC model elements.  Contains common methods and 
 * template for any FRC model element
 * 
 * @author Ryan O'Meara
 */
public abstract class FRCElement implements IFRCModelEventNotifier{
	private ArrayList<IFRCModelEventListener> modelListeners;
	private boolean allowUpdate;
	private IFRCElementContainer parent;
	
	/**
	 * Class which allows multi-threading of view model construction. Creates the
	 * necessary element, and includes a function which both waits for the thread
	 * to finish execution and returns the created element
	 * 
	 * @author Ryan O'Meara
	 */
	protected class AddViewModelElement extends Thread{
		private FRCElement getViewModelFor;
		FRCVElement returnElement;
		
		public AddViewModelElement(FRCElement element){
			getViewModelFor = element;
			returnElement = null;
		}
		
		@Override
		public void run(){
			returnElement = getViewModelFor.getViewModel();
		}
		
		/**
		 * Waits for this thread to end via Thread.join, and then
		 * returns the created FRCVElement
		 * @return The created FRCVElement, or null if none was created
		 * @throws Exception Any exception thrown by Thread.join
		 */
		public FRCVElement joinAndReturn() throws Exception{
			super.join();
			return returnElement;
		}
	}
	
	public FRCElement(){
		modelListeners = new ArrayList<IFRCModelEventListener>();
		allowUpdate = true;
		parent = null;
	}
	
	/**
	 * @return Name of this element
	 */
	public abstract String getElementName();
	
	/**
	 * @return A display name to use when creating an element's view model
	 */
	public abstract String getDisplayName();
	
	/**
	 * Determines if this element can be modified.  An element can be modified
	 * if any part of it is able to be changed.  For example, a field in a 
	 * compilation unit which is of a type defined by a class file would still 
	 * be considered modifiable, even though the class cannot be changed, because 
	 * the field can be changed.  The method contained in the type defined by the 
	 * class file would be considered unmodifiable
	 * @return true if the element can be modified, false otherwise
	 */
	public abstract boolean canModify();
	
	/**
	 * @return Fully qualified name of this element, indicating its place in 
	 * the model
	 */
	public final String getFullyQualifiedName(){
		String qualifiedName = "";
		
		if((parent != null)&&(parent instanceof FRCElement)){
			qualifiedName += ((FRCElement)parent).getFullyQualifiedName() + ".";
		}
		
		qualifiedName += getElementName();
		
		return qualifiedName;
	}
	
	/**
	 * @return The element type, which determines display and ordering
	 */
	public abstract ModelElementType getElementType();
	
	/**
	 * Triggers a rebuild of the element.  The element reconciles
	 * itself with its existing data, refreshing it and its children,
	 * useful for when elements change in such a way as the old defining 
	 * elements are abandoned by the java model.  Should rebuild as "locally"
	 * as possible, meaning that the parent should not be rebuilt unless
	 * necessary.  It is a safe assumption that any java elements the element
	 * is built off of may be invalid.
	 */
	public abstract void rebuild();
	
	/**
	 * Determines if this element is in some part defined by the given
	 * IJavaElement
	 * @param element
	 * @return
	 */
	public abstract boolean definedByElement(IJavaElement element);
	
	/**
	 * @return This element's parent, or null if it has none
	 */
	public IFRCElementContainer getParent(){
		return parent;
	}
	
	/**
	 * Sets this element's parent to the given element parent.  Does not
	 * notify the model of this data change; this is a notification sent 
	 * by the parent the element is being added to
	 * @param inputParent The new parent of this element
	 */
	public void setParent(IFRCElementContainer inputParent){
		if(parent != null){removeListener(parent);}
		parent = inputParent;
		if(parent != null){addListener(parent);}
	}
	
	/** Turns on allowing external calls to update the element */
	public void enableOutsideUpdate(){
		allowUpdate = true;
	}
	
	/** Turns off allowing external calls to update the element */
	public void disableOutsideUpdate(){
		allowUpdate = false;
	}
	
	/**
	 * Updates the element if it is affected by the given element or node, and
	 * updates are enabled for the element
	 * @param element The IJavaElement changed, or null of no element was changed
	 * @param node The ASTNode changed, or null if no ASTNode was changed
	 */
	public final void update(IJavaElement element, ASTNode node){
		if(allowUpdate){runUpdate(element, node);}
	}
	
	/**
	 * Executes any actions needed to update the element
	 * @param element The changed java element, or null if no java element
	 * was changed
	 * @param node The AST node changed, or null if no ASTnode was changed
	 */
	protected abstract void runUpdate(IJavaElement element, ASTNode node);
	
	/**
	 * Determines if a given FRCElement is of the right type to reconcile with
	 * the given class (takes into account subclasses)
	 * @param updateTo The FRCElement being used as the reference for the update
	 * @return True if updateTo can be used to update this object 
	 */
	protected boolean canReconcile(FRCElement updateTo){
		return getClass().isInstance(updateTo);
	}
	
	/**
	 * Takes the given FRCElement and checks if it can be used to update this
	 * object.  If it can, then it is used to update it
	 * @param updateTo The FRCElement being used to update this object
	 * @return true, if the reconcile was successful
	 */
	public final boolean reconcile(FRCElement updateTo){
		if(canReconcile(updateTo)){return runReconcile(updateTo);}
		
		return false;
	}
	
	/**
	 * Takes the given FRCElement and uses it to update this FRCElement.  
	 * Should only be called from the reconcile method in FRCElement, where the
	 * input FRCElement is checked for safe casting to this object type
	 * beforehand.  Any other needs for this method can be fulfilled by
	 * calling reconcile, defined in FRCElement
	 * @param updateTo The FRCElement being used to update this object
	 * @return true, if the reconcile was successful
	 */
	protected abstract boolean runReconcile(FRCElement updateTo);
	
	/**
	 * Opens the element in the Eclipse editor if possible, otherwise does
	 * nothing
	 * @return true if the element was opened, false otherwise
	 */
	public abstract boolean openInEditor();
	
	/**
	 * Returns a code fragment for this element that can be dragged into
	 * the java editor
	 * @return String representation of the code fragment to drag in, or
	 * null if there is no associated code fragment for the element
	 */
	public abstract String getCodeFragment();
	
	/**
	 * Returns the view model corresponding to this robot element 
	 * @return The view model element with the proper information to
	 * display to represent this robot element
	 */
	public abstract FRCVElement getViewModel();
	
	/**
	 * @return The FRCProject that contains this element
	 */
	public FRCProject getFRCProject(){
		IFRCElementContainer currentParent = getParent();
		
		while(currentParent != null){
			if(currentParent instanceof FRCProject){
				return (FRCProject)currentParent;
			}
			
			if(currentParent instanceof FRCElement){
				currentParent = ((FRCElement) currentParent).getParent();
			}else{
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * @return The FRCModel for the workspace
	 */
	public FRCModel getFRCModel(){
		FRCProject project = getFRCProject();
		
		if(project != null){
			return getFRCProject().getFRCModel();
		}
		
		IFRCElementContainer currentParent = getParent();
		
		while(currentParent != null){
			if(currentParent instanceof FRCModel){
				return (FRCModel)currentParent;
			}
			
			if(currentParent instanceof FRCElement){
				currentParent = ((FRCElement) currentParent).getParent();
			}else{
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * Causes this object to cleanup any references it has to other objects
	 * and otherwise prepare for being deleted, after which it sends an event
	 * to notify its parent
	 */
	public void dispose(){
		notifyListeners(new FRCModelEvent(
				this,
				FRCModelEvent.FT_DISPOSED,
				getElementName() + " disposed"));
		modelListeners.clear();
		parent = null;
	}
	
	@Override
	public void addListener(IFRCModelEventListener newListener){
		if(newListener != null){modelListeners.add(newListener);}
	}
	
	@Override
	public void removeListener(IFRCModelEventListener revListener){
		if(revListener != null){modelListeners.remove(revListener);}
	}
	
	@Override
	public void notifyListeners(FRCModelEvent event){
		//Prevent concurrent modification exception
		ArrayList<IFRCModelEventListener> tempduplicate = 
			new ArrayList<IFRCModelEventListener>();
		
		for(IFRCModelEventListener currentListener : modelListeners){
			tempduplicate.add(currentListener);
		}
		
		for(IFRCModelEventListener currentListener : tempduplicate){
			currentListener.receiveEvent(event);
		}
	}
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public int hashCode(){
		if(getElementName() !=  null){return getElementName().hashCode();}
		
		return 0;
	}
	
	@Override
	public String toString(){return getFullyQualifiedName();}
}
