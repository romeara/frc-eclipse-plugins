package edu.wpi.first.javadev.builder.view.views;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.view.dnd.DisplayObjectDragSourceListener;
import edu.wpi.first.javadev.builder.view.provider.ModelViewContentProvider;
import edu.wpi.first.javadev.builder.view.provider.ModelViewLabelProvider;
import edu.wpi.first.javadev.builder.view.sorter.TreeElementSorter;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * The base view for the two display trees.  Contains common functionality, and 
 * hooks to fill in view specific code
 * 
 * @author Ryan O'Meara
 */
public abstract class BaseCodeView extends ViewPart{
	//View Components
	protected CodeTreeViewer treeViewer;
	//Providers 
	protected ModelViewLabelProvider labelProvider;
	protected ModelViewContentProvider contentProvider;
	//Sorter
	protected ViewerSorter treeItemSorter;
	//DND Adapters
	protected DisplayObjectDragSourceListener dragListener; 
	//Actions
	protected Action doubleClickAction;
	
	
	
	protected BaseCodeView() {}
	
	
	
	/** Create action objects (filters, tool bars) */ 
	protected abstract void createActions();
	
	/** Creates any filters necessary */
	protected abstract void createFilters();
	
	/** Add any and additional DND support */
	protected abstract void addAdditionalDNDSupport();
	
	/** Create the button tool bar for the view */ //Specific
	protected abstract void addToolbarButtons();
	
	/** Puts available actions in the right click menu */  
	protected abstract void fillContextMenuElements(IMenuManager manager);
	
	/**
	 * Fill the menus with associated actions
	 * @param rootMenuManager Menu manager for the menus to be filled
	 */ 
	protected abstract void fillFilterMenu(IMenuManager rootMenuManager);
	
	/** Returns the root of the display tree */
	protected abstract FRCVParent getDisplayTreeRoot();
	
	/**
	 * Updates the filters currently used, multiple filters can be used at once 
	 * @param action The action that triggered the update
	 */  
	protected abstract void updateFilters(Action action);
	
	/** The initial tree depth to expand the view to on creation */
	protected abstract int getInitialLevel();
	
	/** Returns the transfer types each view supports */
	protected abstract Transfer[] getTransferTypes();
	
	
	
	/**
	 * Add support for dragging items from the view 
	 */ //Base
	protected void addDragSupport() {
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DragSource source = new DragSource(treeViewer.getControl(), operations);
			 
		// Provide data in Text format
		Transfer[] types = getTransferTypes();
		source.setTransfer(types);
		 	
		dragListener = new DisplayObjectDragSourceListener(treeViewer);
		source.addDragListener(dragListener);
	}
	
	/** Gets the selected elements in the tree */
	@SuppressWarnings("unchecked")
	protected FRCElement[] getSelected(boolean includeContainers){
		FRCElement[] returnElement = null;
		ISelection fSelection = treeViewer.getSelection();
		
		if(fSelection instanceof IStructuredSelection){
			IStructuredSelection selection = (IStructuredSelection)fSelection;
			
			FRCElement holder;
			
			if(selection.size() == 1){
				holder = ((FRCVElement)selection.getFirstElement()).getModelElement();
				
				if((holder == null)&&includeContainers){holder = ((FRCVParent)((FRCVElement)selection.getFirstElement()).getParent()).getModelElement();}
				returnElement = new FRCElement[]{holder};
			}else{
				if(selection.size() == 0){returnElement = new FRCElement[]{CodeViewerPlugin.getFRCModel().getActiveProject().getRobot()};}
				ArrayList<FRCElement> options = new ArrayList<FRCElement>();
				
				Iterator<IStructuredSelection> selIt = (Iterator<IStructuredSelection>)selection.iterator();
				while(selIt.hasNext()){
					holder = ((FRCVElement)selIt.next()).getModelElement();
					if((holder == null)&&includeContainers){holder = ((FRCVParent)((FRCVElement)selIt.next()).getParent()).getModelElement();}
					options.add(holder);
				}
				
				returnElement = new FRCElement[options.size()];
				
				returnElement = options.toArray(returnElement);
			}
		}
		
		return returnElement;
	}
	
	/** Creates all actions for this view */
	protected void createAllActions(){
		doubleClickAction = new Action() {
			public void run() {
				ISelection fSelection = treeViewer.getSelection();
				
				if(fSelection instanceof IStructuredSelection){
					IStructuredSelection selection = (IStructuredSelection)fSelection;
					if(selection.size() == 1){
						FRCElement selectedObject = ((FRCVElement)selection.getFirstElement()).getModelElement();
						if(selectedObject != null){	
							selectedObject.openInEditor();
						}
					}
				}
			}
		};
			
		createActions();
	}
	
	/**
	 * Instantiates the filters and sorters available for the tree
	 */
	protected void createFiltersAndSorters() {
		treeItemSorter = new TreeElementSorter(); 
		createFilters();
	}
	
	/**
	 * Create the view menus
	 */ //Base
	protected void createMenus() {
		IMenuManager rootMenuManager = getViewSite().getActionBars().getMenuManager();
		rootMenuManager.setRemoveAllWhenShown(true);
		rootMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillFilterMenu(mgr);
			}
		});
		fillFilterMenu(rootMenuManager);
	}
	
	/** Clears and puts available actions in the right click menu */
	protected void fillContextMenu(IMenuManager manager) {
		manager.removeAll();
		fillContextMenuElements(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Retrieves the control for the view, which is the control for the treeViewer it contains
	 * @return The Control for the view
	 */ 
	public Control getControl(){
		if(treeViewer != null){
			return treeViewer.getControl();
		}
		
		return null;
	}
	
	/** Finds the primary parent element, or the parent of the primary if it is 
	 * not a parent, or null if none */
	public FRCVParent getPrimaryElement(FRCVElement[] available){
		FRCVParent retParent = null;
		FRCVElement temp;
		
		for(int i =0; i < available.length; i++){
			if(!(available[i] instanceof FRCVParent)){
				temp = (FRCVParent)available[i].getParent();
				
				if((temp != null)&&(temp.getElementType().getOrder() < retParent.getElementType().getOrder())){
					retParent = (FRCVParent)temp;
				}
			}
		}
		
		for(int i =0; i < available.length; i++){
			if(available[i] instanceof FRCVParent){
				temp = (FRCVParent)available[i];
				
				if((temp != null)&&(temp.getElementType().getOrder() < retParent.getElementType().getOrder())){
					retParent = (FRCVParent) temp;
				}
			}
		}
		
		return retParent;
	}
	
	/** Hooks into right click listener */ //Base
	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BaseCodeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}
	
	/** Hooks into listener for double clicking *///Base
	protected void hookDoubleClickAction() {
		//Uses addOpenListener instead of addDoubleClickListener to honor the
		//single/double click preference in Eclipse
		treeViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				doubleClickAction.run();
				
			}
		});
	}
	
	/**
	 * Set listeners for selection changes 
	 */ //Base
	protected void hookTreeSelectionListener() {
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				//Kept to allow easy example of how to get v element 
				/*if(event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					StringBuffer toShow = new StringBuffer();
					for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
						Object domain = (FRCVElement) iterator.next();
						String value = labelProvider.getText(domain);
						toShow.append(value);
						toShow.append(", ");
					}
					// remove the trailing comma space pair
					if(toShow.length() > 0) {
						toShow.setLength(toShow.length() - 2);
					}
					text.setText(toShow.toString());
				}*/
			}
		});
	}
	
	@Override
	public void createPartControl(Composite parent) {
		/* Create a grid layout object so the text and tree viewer
		 * have the desired layout */
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);
		
		//Create the tree viewer as a child of the composite parent
		treeViewer = new CodeTreeViewer(parent);
		contentProvider = new ModelViewContentProvider(getInitialLevel());
		treeViewer.setContentProvider(contentProvider);
		labelProvider = new ModelViewLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		
		treeViewer.setUseHashlookup(true);
		
		// layout the tree viewer below the text field
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		treeViewer.getControl().setLayoutData(layoutData);
		
		// Create menu, tool bars, filters, sorters.
		createFiltersAndSorters();
		createAllActions();
		createMenus();
		addToolbarButtons();
		hookTreeSelectionListener();
		hookContextMenu();
		hookDoubleClickAction();
		
		addDragSupport();
		addAdditionalDNDSupport();
		
		//Set the sorter, input, and expanded levels
		treeViewer.setSorter(treeItemSorter);
		treeViewer.setInput(getDisplayTreeRoot());
		treeViewer.expandToLevel(getInitialLevel());
	}
	
	@Override
	public void setFocus() {}
}
