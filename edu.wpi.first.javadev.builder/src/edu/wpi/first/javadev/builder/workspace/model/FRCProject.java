package edu.wpi.first.javadev.builder.workspace.model;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.library.FRCLibrary;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRobot;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCView;
import edu.wpi.first.javadev.projects.nature.FRCProjectNature;

/**
 * Element which represents an project in the workspace which has the FRC 
 * nature (which indicates a robot project).  Contains a model for the 
 * attached library and the contained robot
 * 
 * @author Ryan O'Meara
 */
public class FRCProject extends FRCElement implements IFRCElementContainer{
	private boolean passEvents;
	private boolean isDisposing;
	private boolean logEvents;
	
	private ArrayList<FRCModelEvent> loggedEvents;
	
	protected FRCRobot projectRobot;
	protected FRCLibrary projectLibrary;
	protected IJavaProject project;
	
	/**
	 * Class which allows for multi-threading of model construction
	 * 
	 * @author Ryan O'Meara
	 */
	protected class CreateRobot implements Runnable{
		private IJavaProject robotProject;
		
		protected CreateRobot(IJavaProject project){
			robotProject = project;
		}
		
		public void run(){
			projectRobot = createFRCRobot(robotProject);
		}
	}
	
	/**
	 * Class which allows for multi-threading of model construction
	 * 
	 * @author Ryan O'Meara
	 */
	protected class CreateLibrary implements Runnable{
		private IJavaProject robotProject;
		
		protected CreateLibrary(IJavaProject project){
			robotProject = project;
		}
		
		public void run(){
			projectLibrary = createFRCLibrary(robotProject);
		}
	}
	
	protected FRCProject(IJavaProject robotProject){
		super();
		ParseConstants.getRobotQualifiedNames(robotProject);
		ParseConstants.parseRobotNames(robotProject);
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		threads.add(new Thread(new CreateRobot(robotProject)));
		threads.add(new Thread(new CreateLibrary(robotProject)));
		
		for(Thread current : threads){current.start();}
		
		for(Thread current : threads){try{current.join();}catch(Exception e){}}
		
		if(projectRobot == null){projectLibrary = null;}
		
		if(projectRobot != null){
			projectRobot.setParent(this);
			projectRobot.addListener(this);
		}
		
		if(projectLibrary != null){
			projectLibrary.setParent(this);
			projectLibrary.addListener(this);
		}
		
		if((projectRobot != null)&&(projectLibrary != null)){
			projectRobot.addListener(projectLibrary);
		}
		
		project = robotProject;
		passEvents = true;
		isDisposing = false;
		logEvents = false;
		
		loggedEvents = new ArrayList<FRCModelEvent>();
	}
	
	/**
	 * @return The FRCRobot contained in this project
	 */
	public FRCRobot getRobot(){return projectRobot;}
	
	/**
	 * @return The FRCLibrary contained in this project
	 */
	public FRCLibrary getLibrary(){return projectLibrary;}
	
	/**
	 * @return The java project that this FRC project is based on
	 */
	public IJavaProject getJavaProject(){return project;}
	
	/**
	 * Constructs and returns the view model for this project's robot
	 * @return FRCView that contains the robot view
	 */
	public FRCView getRobotViewModel(){
		if(getRobot() != null){
			FRCView robotView = new FRCView(FRCModel.ROBOT_VIEW_NAME);
			robotView.add(getRobot().getViewModel());
			
			return robotView;
		}
		
		return null;
	}
	
	/**
	 * Constructs and returns the view model for this project's library
	 * @return FRCView that contains the library view
	 */
	public FRCView getLibraryViewModel(){
		if(getLibrary() != null){
			FRCView libraryView = new FRCView(FRCModel.PALETTE_VIEW_NAME);
			libraryView.add(getLibrary().getViewModel());
			
			return libraryView;
		}
		
		return null;
	}
	
	@Override
	public FRCElement findElement(IJavaElement element){
		FRCElement found = null;
		
		if(definedByElement(element)){return this;}
		
		if((getRobot() != null)
				&&((found = getRobot().findElement(element)) != null)){
			return found;
		}
		
		if((getLibrary() != null)
				&&((found = getLibrary().findElement(element)) != null)){
			return found;
		}
		
		return null;
	}
	
	@Override
	public FRCElement findElement(String fullyQualifiedName){
		FRCElement found = null;
		
		if(fullyQualifiedName.indexOf(getFullyQualifiedName()) == -1){
			return null;
		}
		
		if(getFullyQualifiedName().equals(fullyQualifiedName)){return this;}
		
		if((getRobot() != null)
				&&((found = getRobot().findElement(fullyQualifiedName)) != null)){
			return found;
		}
		
		if((getLibrary() != null)
				&&((found = getLibrary().findElement(fullyQualifiedName)) != null)){
			return found;
		}
		
		return null;
	}
	
	@Override
	public String getElementName(){
		if(project != null){return "{PROJ:" + project.getElementName() + "}";}
		
		return "{PROJ:NULLJAVAPROJECT}";
	}
	
	@Override
	public String getDisplayName(){
		if(project != null){return project.getElementName();}
		
		return "NULLJAVAPROJECT";
	}
	
	@Override
	public boolean canModify(){return true;}
	
	@Override
	public ModelElementType getElementType(){
		return new ModelElementType(
				"FRC Project", 
				ModelElementType.LASTORDERTYPE.getIconPath(), 
				1);
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		reconcile(new FRCProject(project));
		
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
	public boolean definedByElement(IJavaElement element){
		if(project == null){return false;}
		
		return element.equals(project);
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		if((project == null)||(!project.exists())){
			rebuild();
			return;
		}
		
		if(projectRobot == null){projectRobot = createFRCRobot(project);}
		if((projectRobot != null)&&(projectLibrary == null)){projectLibrary = createFRCLibrary(project);}
		
		if((element instanceof ICompilationUnit)&&(projectRobot != null)){
			projectRobot.update(element, node);
		}
		
		if(projectLibrary != null){
			projectLibrary.update(element, node);
		}else{
			projectLibrary = createFRCLibrary(project);
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			disableEventPassing();
			clearEventLog();
			enableEventLogging();
			
			FRCProject proj = (FRCProject)updateTo;
			
			project = null;
			projectRobot = null;
			projectLibrary = null;
			project = proj.project;
			projectRobot = proj.projectRobot;
			projectLibrary = proj.projectLibrary;
			
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
	public FRCProject getFRCProject(){return this;}
	
	@Override
	public FRCModel getFRCModel(){
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
	
	@Override
	public void dispose(){
		super.dispose();
		if(projectRobot != null){projectRobot.dispose();}
		if(projectLibrary != null){projectLibrary.dispose();}
		project = null;
		loggedEvents.clear();
	}
	
	@Override
	public void receiveEvent(FRCModelEvent event){notifyListeners(event);}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof FRCProject){
			FRCProject proj = (FRCProject)obj;
			boolean projectsEq = false;
			
			if((project == null)&&(proj.project == null)){
				projectsEq = true;
			}
			
			if((project != null)&&(proj.project != null)){
				projectsEq = project.equals(proj.project);
			}
			
			boolean rEq = false;
			
			if((projectRobot == null)&&(proj.projectRobot == null)){
				rEq = true;
			}
			
			if((projectRobot != null)&&(proj.projectRobot != null)){
				rEq = projectRobot.equals(proj.projectRobot);
			}
			
			boolean lEq = false;
			
			if((projectLibrary == null)&&(proj.projectLibrary == null)){
				rEq = true;
			}
			
			if((projectLibrary != null)&&(proj.projectLibrary != null)){
				rEq = projectLibrary.equals(proj.projectLibrary);
			}
			
			return projectsEq && rEq && lEq;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(project != null){hash += project.hashCode();}
		
		if(projectRobot != null){hash += projectRobot.hashCode();}
		
		if(projectLibrary != null){hash += projectLibrary.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCProject safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCProject
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IJavaProject){
			try{
				IJavaProject proj = (IJavaProject)candidate;
				if(proj.getProject().hasNature(
						FRCProjectNature.FRC_PROJECT_NATURE)){
					return true;
				}
			}catch(Exception e){}
		}
		
		return false;
	}
	
	/**
	 * Creates an FRCRobot from the given IJavaProject if possible.  The
	 * IJavaProject must have an frc nature and contain a robot class
	 * @param robotProject The project to build from
	 * @return The created FRCRobot, or null if the robot could not be
	 * created
	 */
	public static FRCRobot createFRCRobot(IJavaProject robotProject){
		if(FRCRobot.meetsRequirements(robotProject)){
			return new FRCRobot(robotProject);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCLibrary from the given IJavaProject if possible.  The
	 * IJavaProject must have the FRCNature define by the core plug-ins
	 * @param javaProject The IJavaProject to build from
	 * @return The created FRCLibrary, or null if the library could not be
	 * created
	 */
	public static FRCLibrary createFRCLibrary(IJavaProject javaProject){
		if(FRCLibrary.meetsRequirements(javaProject)){
			return new FRCLibrary(javaProject);
		}
		
		return null;
	}

	@Override
	public void enableEventPassing() {passEvents = true;}

	@Override
	public void disableEventPassing() {passEvents = false;}

	@Override
	public void disposeActionStart() {isDisposing = true;}
	
	@Override
	public void disposeActionFinish() {isDisposing = false;}
	
	@Override
	public void enableEventLogging() {logEvents = true;}
	
	@Override 
	public void disableEventLogging() {logEvents = false;}
	
	@Override
	public void clearEventLog() {loggedEvents.clear();}

	@Override
	public boolean isDisposing() {return isDisposing;}
	
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
	
}
