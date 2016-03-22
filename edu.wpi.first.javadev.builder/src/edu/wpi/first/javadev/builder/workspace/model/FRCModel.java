package edu.wpi.first.javadev.builder.workspace.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.util.CircularRefNode;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCView;
import edu.wpi.first.javadev.projects.nature.FRCProjectNature;
import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.listener.ISDKInstallListener;

/**
 * Overall model for the entire workspace.  Contains all {@link FRCProjects}
 * and a view model for each view in the workspace
 * 
 * @author Ryan O'Meara
 */
public class FRCModel extends FRCElement 
implements IFRCElementParent<FRCProject>, IWindowListener, IPartListener2, 
IPropertyListener, IElementChangedListener, IResourceChangeListener, 
ISDKInstallListener {
	public static final String ROBOT_VIEW_NAME = "FRC Robotcentric View";
	public static final String PALETTE_VIEW_NAME = "FRC Palette View";
	public static final String INSERT_INSTANCE_NAME = 
		"{$edu.wpi.first.javadev.builder.instanceSubString}";
	
	private static ICompilationUnit activeCompUnit;
	
	private static FRCElement refactoredElement;
	
	protected FRCProject activeProject;
	protected FRCView paletteView;
	protected FRCView robotView;
	
	private boolean passEvents;
	private boolean disposing;
	private boolean logEvents;
	
	private ArrayList<FRCModelEvent> loggedEvents;
	private static ArrayList<CircularRefNode> linkedTree;
	
	private ArrayList<FRCProject> children;
	
	private boolean delayListenerAdded;
	private boolean javaInit, pageInit, resourceinit;
	
	
	public static final int TYPE_RENAME = 3;
	public static final int FIELD_RENAME = 2;
	public static final int METHOD_RENAME = 1;
	
	public FRCModel(){
		super();
		SDKPlugin.getDefault().addInstallListener(this);
		activeCompUnit = null;
		passEvents = true;
		logEvents = false;
		disposing = false;
		refactoredElement = null;
		loggedEvents = new ArrayList<FRCModelEvent>();
		children = new ArrayList<FRCProject>();
		linkedTree = new ArrayList<CircularRefNode>();
		delayListenerAdded = false;
		javaInit = false;
		pageInit = false;
		resourceinit = false;
		initializeViews();
		initializeListeners();
	}
	
	/**
	 * Initializes listeners which notify the model of relevant java code 
	 * changes, and updates the model accordingly
	 */
	private void initializeListeners(){
		if((!javaInit)||(!pageInit)||(!resourceinit)){
			if(PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null){
				if(!delayListenerAdded){
					PlatformUI.getWorkbench().addWindowListener(this);
					delayListenerAdded = true;
				}
				return;
			}
			
			if(!javaInit){
				try{
					JavaCore.addElementChangedListener(this);
					javaInit = true;
				}catch(Exception e){javaInit = false;}
			}
			
			if(!pageInit){
				try{
					CodeViewerPlugin.getDefault().getPage().addPartListener(this);
					pageInit = true;
				}catch(Exception e){pageInit = false;}
			}
			
			if(!resourceinit){
				try{
					ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
				}catch(Exception e){resourceinit = false;}
			}
			
			
		}
	}
	
	/**
	 * Initializes the robot and palette views
	 */
	private void initializeViews(){
		class createView implements Runnable{
			@Override
			public void run() {
				IJavaProject recentProject;
				if((recentProject = getMostRecentProject()) != null){
					FRCProject roboProj = getFRCProject(recentProject);
					robotView.reconcile(roboProj.getRobotViewModel());
					paletteView.reconcile(roboProj.getLibraryViewModel());
				}else{
					robotView.reconcile(getNoRobotView(ROBOT_VIEW_NAME));
					paletteView.reconcile(getNoRobotView(PALETTE_VIEW_NAME));
				}
			}
			
		}
		
		robotView = new FRCView(ROBOT_VIEW_NAME);
		paletteView = new FRCView(PALETTE_VIEW_NAME);
		
		
		
		robotView.setParent(this);
		paletteView.setParent(this);
		
		robotView.reconcile(getNoRobotView(ROBOT_VIEW_NAME));
		paletteView.reconcile(getNoRobotView(PALETTE_VIEW_NAME));
		
		//Add views as listeners to catch active project and page changes
		addListener(robotView);
		addListener(paletteView);
		
		spawnTask(new createView());
	}
	
	/**
	 * Returns the circular reference node for the given identifying
	 * element, or creates the node if necessary
	 * @param element
	 * @param description
	 * @return
	 */
	public static CircularRefNode getReferenceNode(IJavaElement element, 
			IJavaElement[] description){
		for(CircularRefNode current : linkedTree){
			if(current.identifiedBy(element)){return current;}
		}
		
		CircularRefNode node = new CircularRefNode(element, description);
		linkedTree.add(node);
		
		return node;
	}
	
	/**
	 * Creates a circular reference node with the given arguments 
	 * @param element The identifying element
	 * @param description The elements which with the identifying element comprise the
	 * full list of elements which should be compared against to check for circular
	 * referencing
	 * @param parent The node which is the parent of the node to be created
	 * @return The created node
	 */
	public static CircularRefNode createCircularReferenceNode(IJavaElement element, 
			IJavaElement[] description, CircularRefNode parent){
		CircularRefNode node = new CircularRefNode(element, description, parent);
		linkedTree.add(node);
		return node;
	}
	
	/**
	 * Clears the circular reference node list
	 */
	public static void clearCircularReference(){
		for(CircularRefNode current : linkedTree){
			current.dispose();
		}
		linkedTree.clear();
	}
	
	/**
	 * Returns the view which indicates no robot is current available
	 * @param name The name to give the view.  Available to preserve preset 
	 * names when reconciling to this view
	 * @return The View reflecting no robot found
	 */
	public FRCView getNoRobotView(String name){
		if(name == null){name = "";}
		
		FRCView noRobot = new FRCView(name);
		noRobot.add(new FRCVElement("No Robot Found", ModelElementType.FRCROBOT));
		
		return noRobot;
	}
	
	/**
	 * Spawns an asynchronous runnable task in the Eclipse workspace
	 * @param run The {@link Runnable} to execute
	 */
	private void spawnTask(Runnable run){
		Display d = Display.getDefault();

		if (d != null) {d.asyncExec(run);}
	}
	
	private static void updateActiveCompUnit(){
		IWorkingCopyManager mgr = JavaUI.getWorkingCopyManager();
		IEditorInput edIn = null;
		
		try {
			edIn = CodeViewerPlugin.getDefault().getPage()
			.getActiveEditor().getEditorInput();

			mgr.connect(edIn);
			ICompilationUnit unit = mgr.getWorkingCopy(edIn);
			
			activeCompUnit = unit;
			return;
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			mgr.disconnect(edIn);
		}
		
		activeCompUnit = null;
	}
	
	/**
	 * Gets the java project associated with the active editor file
	 * @return The IJavaProject associated, or null if none
	 */
	public static IJavaProject getActiveJavaProject(){
		ICompilationUnit file = getActiveJavaFile();
		
		if(file == null){return null;}
		
		return file.getJavaProject();
	}
	
	/**
	 * @return The ICompilationUnit associated with the currently open editor,
	 * or null if no compilation unit available
	 */
	public static ICompilationUnit getActiveJavaFile(){
		return activeCompUnit;
	}
	
	/**
	 * @return The active FRCProject (file is open in editor)
	 */
	public FRCProject getActiveProject(){
		initializeListeners();
		return activeProject;
	}
	
	/**
	 * @return The current view model for the palette view
	 */
	public FRCView getPaletteViewModel(){
		initializeListeners();
		return paletteView;
	}
	
	/**
	 * @return The current view model for the robot view
	 */
	public FRCView getRobotViewModel(){
		initializeListeners();
		return robotView;
	}
	
	/**
	 * Updates the currently active FRCProject to reflect the project being
	 * worked on in the Eclipse editor.  Updates the view models
	 */
	public void updateActiveProject(){
		//Should change view models, and notify listeners
		FRCProject project = getFRCProject(getActiveJavaProject());
		
		if((project == null)&&(getActiveProject() == null)){
			//Both are null, no change has occurred
			return;
		}
		
		if(((project == null)&&(getActiveProject() != null))
				||((project != null)&&(getActiveProject() == null))){
			//One is null, other is not, change occurred
			project.runUpdate(getActiveJavaFile(), null);
			activeProject = project;
			notifyListeners(new FRCModelEvent(
					this, 
					FRCModelEvent.FT_DATA_CHANGE, 
					FRCModelEvent.FK_ACTIVE_PROJ_CHANGE, 
					"Active Project changed"));
			
			return;
		}
		
		if(!project.getFullyQualifiedName()
				.equals(getActiveProject().getFullyQualifiedName())){
			//Not the same project, change has occurred
			activeProject = null;
			activeProject = project;
			notifyListeners(new FRCModelEvent(
					this, 
					FRCModelEvent.FT_DATA_CHANGE, 
					FRCModelEvent.FK_ACTIVE_PROJ_CHANGE, 
					"Active Project changed"));
			
			return;
		}
		
		if((activeProject != null)
				&&(activeProject.getRobot() == null)){
			activeProject.rebuild();
			notifyListeners(new FRCModelEvent(
					this, 
					FRCModelEvent.FT_DATA_CHANGE, 
					FRCModelEvent.FK_ACTIVE_PROJ_CHANGE, 
					"Active Project changed"));
			
			return;
		}
	}
	
	/**
	 * Updates the currently active FRCProject to the given project.
	 * @param makeActive The FRCProject to set as the active project
	 */
	public void updateActiveProject(FRCProject makeActive){
		if(makeActive == null){return;}
		
		if((activeProject == null)||(!activeProject.equals(makeActive))){
			activeProject = null;
			activeProject = makeActive;
			makeActive.runUpdate(getActiveJavaFile(), null);
			notifyListeners(new FRCModelEvent(
					this, 
					FRCModelEvent.FT_DATA_CHANGE, 
					FRCModelEvent.FK_ACTIVE_PROJ_CHANGE, 
					"Active Project changed"));
			
			return;
		}
	}
	
	/**
	 * Finds the FRCProject associated with the given java project.  Only 
	 * searches existing projects; getFRCProject should be used if the 
	 * given project should be created if it does not already exist
	 * @param javaProject The java project to search for
	 * @return The FRCProject associated with the given java project, or
	 * null if it does not exist
	 */
	public FRCProject findProject(IJavaProject javaProject){
		initializeListeners();
		for(FRCProject curProj : children){
			//This work because within a given workspace, two projects cannot
			//have the same name, and an FRCModel is only for use within a 
			//single Eclipse instance, which only opens one workspace at a 
			//time
			if(curProj.getJavaProject() == null){continue;}
			if(curProj.getJavaProject().getElementName()
					.equals(javaProject.getElementName())){
				return curProj;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the FRCProject associated with the given java project.  If an 
	 * FRCProject for a given java project does not yet exist, it is created.
	 * @param javaProject The java project that the returned FRCProject is 
	 * based on
	 * @return The FRCProject associated with the given java project, or null
	 * if the given java project is not an FRC project
	 */
	public FRCProject getFRCProject(IJavaProject javaProject){
		initializeListeners();
		if(javaProject == null){return null;}
		FRCProject project = findProject(javaProject);
		
		if(project == null){
			project = createFRCProject(javaProject);
			add(project);
		}
		
		return project;
	}
	
	/**
	 * Finds and returns the most recently modified java project with the FRC 
	 * nature.  Returns null if no projects are left in the workspace with the
	 * FRC nature
	 * @return IJavaProject which is most recently modified FRC project, or
	 * null if none
	 */
	public static IJavaProject getMostRecentProject(){
		try{
			IJavaModel model = 
				JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
			IJavaProject[] projects = model.getJavaProjects();
			
			IJavaProject best = null;
			
			for(IJavaProject jProj : projects){
				if(jProj.getProject().exists()
						&&jProj.getProject().isOpen()
						&&jProj.getProject().hasNature(FRCProjectNature.FRC_PROJECT_NATURE)){
					if(jProj.getProject().getModificationStamp() 
							!= IResource.NULL_STAMP){
						if(best == null){
							best = jProj;
						}else if(jProj.getProject().getModificationStamp() 
								< best.getProject().getModificationStamp()){
							best = jProj;
						}
					}
				}
			}
			
			return best;
			
		}catch(Exception e){}
		
		return null;
	}
	
	@Override
	public FRCElement findElement(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		FRCElement found = null;
		
		if(getFullyQualifiedName().equals(fullyQualifiedName)){return this;}
		
		for(FRCElement curEl : children){
			if(curEl instanceof FRCProject){
				if((found = ((FRCProject)curEl)
						.findElement(fullyQualifiedName)) != null){
					return found;
				}
			}else if(curEl.getFullyQualifiedName().equals(fullyQualifiedName)){
				return curEl;
			}
		}
		
		return findChildDeep(fullyQualifiedName);
	}
	
	@Override
	public FRCElement findElement(IJavaElement element){
		if(element == null){return null;}
		FRCElement found = null;
		
		if(definedByElement(element)){return this;}
		
		for(FRCElement curEl : children){
			if(curEl instanceof FRCProject){
				if((found = ((FRCProject)curEl)
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
	public String getElementName(){
		return "{WS:" + 
		ResourcesPlugin.getWorkspace().getRoot().getLocation().lastSegment() 
		+ "}";
	}
	
	@Override
	public String getDisplayName(){
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().lastSegment();
	}
	
	@Override
	public boolean canModify(){return false;}
	
	@Override
	public ModelElementType getElementType(){
		return new ModelElementType(
				"FRC Workspace Model", 
				ModelElementType.LASTORDERTYPE.getIconPath(), 
				0);
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		reconcile(new FRCModel());
		
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
	public boolean definedByElement(IJavaElement element){return false;}
	
	@Override
	public IFRCElementContainer getParent(){return null;}
	
	@Override
	public void setParent(IFRCElementContainer inputParent){}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		activeProject.update(element, node);
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		initializeListeners();
		if(!equals(updateTo)){
			disableEventPassing();
			clearEventLog();
			enableEventLogging();
			FRCModel mod = (FRCModel)updateTo;
			String workspaceProjFQN = "";
			
			if((mod.getFRCModel() == null)&&(getFRCModel() != null)){
				workspaceProjFQN = getFRCModel().getFullyQualifiedName() + ".";
			}
			
			if((robotView != null)&&(mod.robotView != null)){
				robotView.reconcile(mod.robotView);
			}else if(robotView != null){
				robotView.dispose();
				robotView = mod.robotView;
			}else{
				robotView = mod.robotView;
			}
			
			if((paletteView != null)&&(mod.paletteView != null)){
				paletteView.reconcile(mod.paletteView);
			}else if(paletteView != null){
				paletteView.dispose();
				paletteView = mod.paletteView;
			}else{
				paletteView = mod.paletteView;
			}
			
			ArrayList<FRCProject> toRemove = new ArrayList<FRCProject>();
			
			for(FRCProject child : children){
				if(mod.findChild(child.getFullyQualifiedName()
						.substring(workspaceProjFQN.length())) == null){
					toRemove.add(child);
				}
			}
			
			//Remove after to avoid messing up for loop by removing from what
			//it is iterating over
			remove(toRemove.toArray(new FRCProject[toRemove.size()]));
			
			for(FRCProject curProj : mod.getChildren()){
				FRCProject inThis;
				
				if((inThis = findChild(workspaceProjFQN 
						+ curProj.getFullyQualifiedName())) != null){
					inThis.reconcile(curProj);
				}else{
					add(curProj);
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
	public boolean openInEditor(){return false;}
	
	@Override
	public String getCodeFragment(){return null;}
	
	@Override
	public FRCVElement getViewModel(){return null;}
	
	@Override
	public FRCProject getFRCProject(){return null;}
	
	@Override
	public FRCModel getFRCModel(){return this;}
	
	@Override
	public void dispose(){
		disableOutsideUpdate();
		disposeActionStart();
		
		super.dispose();
		
		robotView.dispose();
		paletteView.dispose();
		for(FRCProject current : children){current.dispose();}
		children.clear();
		loggedEvents.clear();
		children = null;
		robotView = null;
		paletteView = null;
		activeProject = null;
		
		disposeActionFinish();
		enableOutsideUpdate();
	}
	
	@Override
	public boolean equals(Object obj){
		initializeListeners();
		if(obj instanceof FRCModel){
			FRCModel mod = (FRCModel)obj;
			
			boolean rvEq = false;
			boolean pvEq = false;
			
			//robot view
			if((robotView == null)&&(mod.robotView == null)){
				rvEq = true;
			}else if((robotView != null)&&(mod.robotView != null)){
				rvEq = robotView.equals(mod.robotView);
			}
			
			//palette view
			if((paletteView == null)&&(mod.paletteView == null)){
				rvEq = true;
			}else if((paletteView != null)&&(mod.paletteView != null)){
				rvEq = paletteView.equals(mod.paletteView);
			}
			
			if(mod.getChildren().length == getChildren().length){
				for(FRCProject curVEl : mod.getChildren()){
					if(!children.contains(curVEl)){return false;}
				}
			}else{
				return false;
			}
			
			return rvEq && pvEq;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){ 
		int hash = 0;
		
		if(robotView != null){hash += robotView.hashCode();}
		
		if(paletteView != null){hash += paletteView.hashCode();}
		
		if(children != null){hash += children.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	@Override
	public void add(FRCProject element){
		if(element == null){return;}
		element.setParent(this);
		children.add(element);
		notifyListeners(new FRCModelEvent(
			this, 
			FRCModelEvent.FT_CHILDREN_ADDED, 
			element.getElementName() + " added to " + getElementName(),
			new FRCModelEvent[]{
				new FRCModelEvent(element,
					FRCModelEvent.FT_ADDED,
					"Added to " + getElementName())}));
	}
	
	@Override
	public void add(FRCProject[] elements){
		if(elements == null){return;}
		boolean lastState = passEvents;
		String elementNames = "";
		ArrayList<FRCModelEvent> events = new ArrayList<FRCModelEvent>();
		disableEventPassing();
		for(FRCProject addition : elements){
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
	public void remove(FRCProject element){
		if(element == null){return;}
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
	public void remove(FRCProject[] elements){
		boolean lastState = passEvents;
		String elementNames = "";
		disableEventPassing();
		for(FRCProject removal : elements){
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
	public FRCProject[] getChildren(){
		return children.toArray(new FRCProject[children.size()]);
	}
	
	@Override
	public FRCProject findChild(String fullyQualifiedName){
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		for(FRCProject curProj : children){
			if(curProj.getFullyQualifiedName().equals(fullyQualifiedName)){
				return curProj;
			}
		}
		
		return null;
	}
	
	@Override
	public FRCProject findChildDeep(String fullyQualifiedName){
		return findChild(fullyQualifiedName);
	}
	
	@Override
	public boolean contains(String fullyQualifiedName){
		for(FRCProject curProj : children){
			if(curProj.getFullyQualifiedName().equals(fullyQualifiedName)){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean containsDeep(String fullyQualifiedName){
		//No second level to search through as FRCProjects do not contain 
		//FRCProjects
		return contains(fullyQualifiedName);
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
	public void enableEventLogging(){logEvents = true;}
	
	@Override
	public void disableEventLogging(){logEvents = false;}
	
	@Override
	public void clearEventLog(){loggedEvents.clear();}
	
	@Override
	public boolean isDisposing(){return disposing;}
	
	@Override
	public void receiveEvent(FRCModelEvent event){
		//never notify listeners from here wholesale; most listeners will be 
		//indirectly sending events up to here, which will create an infinite 
		//loop
		if(event.getType() == FRCModelEvent.FT_DISPOSED){
			if(contains(event.getNotifier().getFullyQualifiedName())){
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

	
	/**
	 * Creates an FRCProject from the given IJavaProject if possible.  The
	 * IJavaProject must have the FRCNature define by the core plug-ins
	 * @param javaProject The IJavaProject to build from
	 * @return The created FRCProject, or null if the project could not be
	 * created
	 */
	public static FRCProject createFRCProject(IJavaProject javaProject){
		if(FRCProject.meetsRequirements(javaProject)){
			return new FRCProject(javaProject);
		}
		
		return null;
	}
	
	private static IJavaElementDelta getPackageFragmentDelta(IJavaElementDelta source){
		if(source == null){return null;}
		
		if(source.getElement().getElementType() == IJavaElement.PACKAGE_FRAGMENT){
			return source;
		}
		
		for(IJavaElementDelta current : source.getAffectedChildren()){
			IJavaElementDelta temp = getPackageFragmentDelta(current);
			
			if(temp != null){return temp;}
		}
		
		return null;
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		PlatformUI.getWorkbench().removeWindowListener(this);
		delayListenerAdded = false;
		initializeListeners();
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {}

	@Override
	public void windowClosed(IWorkbenchWindow window) {}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		PlatformUI.getWorkbench().removeWindowListener(this);
		delayListenerAdded = false;
		initializeListeners();
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		class ElementChangedRunnable implements Runnable{
			private IJavaElement changedElement;
			private ASTNode changedAST;
			
			protected ElementChangedRunnable(IJavaElement element, ASTNode ast){
				changedElement = element;
				changedAST = ast;
			}

			@Override
			public void run() {
				//Update current model
				if(getActiveProject() != null){
					getActiveProject().update(changedElement, changedAST);
				}
			}
		}
		
		IJavaElementDelta delta = event.getDelta();
		
		if((delta != null)
				&&(delta.getElement() != null)
				&&(delta.getKind() == IJavaElementDelta.CHANGED)
				&&((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0)
				&&(delta.getElement().getElementType() == IJavaElement.JAVA_MODEL)){
			delta = getPackageFragmentDelta(delta);
		}
		
		if((delta != null)
				&&(delta.getElement() != null)
				&&(delta.getKind() == IJavaElementDelta.CHANGED)
				&&((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0)
				&&(delta.getElement().getElementType() == IJavaElement.PACKAGE_FRAGMENT)){
			IJavaElementDelta[] changedDeltas = delta.getChangedChildren();
			IJavaElementDelta[] addedDeltas = delta.getAddedChildren();
			IJavaElementDelta[] removedDeltas = delta.getRemovedChildren();
			
			if((changedDeltas.length == 1)&&(removedDeltas.length == 1)){
				IJavaElementDelta removed = removedDeltas[0];
				IJavaElementDelta changed = changedDeltas[0];
				
				if((removed != null)&&(changed != null)){
					if(((removed.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0)
							&&((changed.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0)){
						if(refactoredElement != null){
							//CANNOT be run asyncronously because of way this works
							//with events
							refactoredElement.rebuild();
						}
					
						return;
					}
				}
			}else if((removedDeltas.length == 1)&&(addedDeltas.length == 1)){
				IJavaElementDelta removed = removedDeltas[0];
				IJavaElementDelta added = addedDeltas[0];
				
				if((removed != null)&&(added != null)){
					if(((removed.getFlags() & IJavaElementDelta.F_MOVED_TO) != 0)
							&&((added.getFlags() & IJavaElementDelta.F_MOVED_FROM) != 0)){
						try{
							refactoredElement = findElement(((ICompilationUnit)(removed.getElement())).findPrimaryType());
						}catch(Exception e){}
					}
				}
			}
		
		}
		
		if(delta != null){
			spawnTask(new ElementChangedRunnable(delta.getElement(), 
					delta.getCompilationUnitAST()));
		}
	}

	@Override
	public void propertyChanged(Object source, int propId) {
		class propertyChangedRunnable implements Runnable{
			private EditorPart source;
			
			protected propertyChangedRunnable(EditorPart eP){
				source = eP;
			}
			
			@Override
			public void run() {
				//Update current model
				if(getActiveProject() != null){
					IJavaElement changed = JavaCore.create(getActiveProject()
							.getJavaProject().getProject()
							.getFile(source.getPartName()));
					getActiveProject().update(changed, null);
				}
			}
			
		}
		
		if((propId == EditorPart.PROP_DIRTY)&&(source instanceof EditorPart)){
			EditorPart eP = (EditorPart)source;
			
			if(eP.getPartName().indexOf(".java") != -1){
				spawnTask(new propertyChangedRunnable(eP));
			}
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		class partActivatedRunnable implements Runnable{
			private FRCModel model;
			
			protected partActivatedRunnable(FRCModel inputModel){
				model = inputModel;
			}

			@Override
			public void run() {
				//Update active project reference
				updateActiveProject();
				model.notifyListeners(new FRCModelEvent(
						model,
						FRCModelEvent.FT_DATA_CHANGE,
						FRCModelEvent.FK_ACTIVE_PAGE_CHANGE,
						"Page activated"));
			}
			
		}
		
		if(partRef.getPartName().indexOf(".java") != -1){
			updateActiveCompUnit();
			partRef.addPropertyListener(this);
			spawnTask(new partActivatedRunnable(this));
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if(partRef.getPartName().indexOf(".java") != -1){
			updateActiveCompUnit();
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		partRef.removePropertyListener(this);
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if(partRef.getPartName().indexOf(".java") != -1){
			updateActiveCompUnit();
		}
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		partRef.removePropertyListener(this);
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		class partActivatedRunnable implements Runnable{
			FRCModel model;
			
			protected partActivatedRunnable(FRCModel inputModel){
				model = inputModel;
			}

			@Override
			public void run() {
				updateActiveProject();
				model.notifyListeners(new FRCModelEvent(
						model,
						FRCModelEvent.FT_DATA_CHANGE,
						FRCModelEvent.FK_ACTIVE_PAGE_CHANGE,
						"Page input changed"));
			}
			
		}
		
		partRef.addPropertyListener(this);
		
		if(partRef.getPartName().indexOf(".java") != -1){
			updateActiveCompUnit();
			spawnTask(new partActivatedRunnable(this));
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		//Deals with project events
		
		class ProjectDeltaVisitor implements IResourceDeltaVisitor{
			protected IProject foundProj;
			boolean add, openState;
			
			protected ProjectDeltaVisitor(){
				foundProj = null;
				add = openState = false;
			}
			
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource res = delta.getResource();
				
				if(res instanceof IProject){
					foundProj = (IProject)res;
					openState = (delta.getFlags()&IResourceDelta.OPEN) > 0;
					switch(delta.getKind()){
						case IResourceDelta.ADDED:
							add = true;
							break;
						default:
							//Do Nothing
							break;
					}
					
					return false;
				}
				
				return true;
			}
			
			/** @return The IProject the delta pertains to, or null if none */
			public IProject getProject(){return foundProj;}
			
			/** @return true if this delta represents an add event, false otherwise */
			public boolean isAddDelta(){return add;}
			
			/** @return true if this delta indicates the project opened or closed */
			public boolean isOpenStateChangeDelta(){return openState;}
			
		}
		
		class PreProjectRemoval implements Runnable{
			IJavaProject jProj;
			
			public PreProjectRemoval(IJavaProject project){jProj = project;}
			
			@Override
			public void run() {
				boolean wasActive = true;
				FRCProject frcActive = getActiveProject();
				
				if((frcActive == null)
						||(jProj == null)
						||(frcActive.equals(getFRCProject(jProj)))){
					wasActive = false;
				}
				
				if(wasActive){
					FRCProject candidate = getFRCProject(getMostRecentProject());
					
					if(candidate.equals(frcActive)){
						FRCProject[] projects = getChildren();
						
						if((projects != null) &&(projects.length > 1)){
							for(FRCProject current : projects){
								if(!current.equals(frcActive)){
									candidate = current;
									break;
								}
							}
							
							candidate = null;
						}
					}
					
					frcActive = null;
					frcActive = candidate;
				}
				
				if(jProj != null){
					getFRCProject(jProj).dispose();
				}
				
				if(wasActive){
					updateActiveProject(frcActive);
					updateActiveCompUnit();
				}
			}
		}
		
		class PostProjectActivation implements Runnable{
			IJavaProject jProj;
			
			public PostProjectActivation(IJavaProject project){jProj = project;}
			
			@Override
			public void run() {
				if(jProj != null){
					updateActiveProject(getFRCProject(jProj));
					updateActiveCompUnit();
				}
			}
			
		}
		
		try{

			//Find the project which the change took place in
			IProject project = null;
			Runnable runTask = null;
			
			IResource resource = event.getResource();
			ProjectDeltaVisitor projectSearch = new ProjectDeltaVisitor();
			
			if(event.getDelta() != null){event.getDelta().accept(projectSearch);}
			
			if((resource != null)&&(resource instanceof IProject)){
				project = (IProject)resource;
			}else if(event.getDelta() != null){
				project = projectSearch.getProject();
			}
			
			//Process the project change
			if(project != null){
				if(project.exists()){
					if(project.isOpen() 
							&&project.hasNature(FRCProjectNature.FRC_PROJECT_NATURE)){
						IJavaProject javaProject = JavaCore.create(project);
						if((event.getType() == IResourceChangeEvent.PRE_CLOSE)
								||(event.getType() == IResourceChangeEvent.PRE_DELETE)){
							//Pre-project-removal (check if active project, store answer)
							//(switch to different project if it was the active one)
							//(delete associated project)
							runTask = new PreProjectRemoval(javaProject);
						}else if(event.getType() == IResourceChangeEvent.POST_CHANGE){
							if(projectSearch.isAddDelta()
									||(projectSearch.isOpenStateChangeDelta()
											&&project.isOpen())){
								//Post creation/project open processing 
								//(make this active project)
								runTask = new PostProjectActivation(javaProject);
							}
						}
					}
				}
			}
			
			if(runTask != null){spawnTask(runTask);}
		
		}catch(Exception e){e.printStackTrace();}
		
	}

	@Override
	public void installComplete() {
		for(FRCProject current : children){
			if(current.getRobot() == null){
				current.rebuild();
			}
		}
		
		if(activeProject == null){
			updateActiveProject();
		}
		
		if(activeProject != null){
			robotView.reconcile(activeProject.getRobotViewModel());
			paletteView.reconcile(activeProject.getLibraryViewModel());
		}
	}
	
}
