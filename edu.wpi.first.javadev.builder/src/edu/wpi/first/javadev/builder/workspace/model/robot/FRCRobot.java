package edu.wpi.first.javadev.builder.workspace.model.robot;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import edu.wpi.first.javadev.builder.util.CircularRefNode;
import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.IStandardRename;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;
import edu.wpi.first.javadev.projects.nature.FRCProjectNature;
import edu.wpi.first.javadev.projects.refactoring.ManifestRename;

/**
 * Represents a robot model in an FRC project.  Can contain capabilities,
 * devices, mechanisms, and events
 * 
 * @author Ryan O'Meara
 */
public class FRCRobot extends FRCRMechanism implements IStandardRename{
	//Will only use type, no field
	
	public FRCRobot(IJavaProject robotProject){
		super();
		disableEventPassing();
		deviceField = null;

		deviceType = findRobotType(robotProject);
		
		if(deviceType != null){
			if(deviceType != null){
				CircularRefNode node = FRCModel.createCircularReferenceNode(deviceType, null, null);
				constructFromIType(deviceType, node);
				
				try{
					IJavaElement[] javaChildren = deviceType.getChildren();
					
					for(IJavaElement currentElement : javaChildren){
						FRCRElement curREl;
						
						if((FRCREvent.meetsRequirements(currentElement))
								&&(curREl = FRCRobot
										.createFRCRElement(currentElement)) != null){
							add(curREl);
						}
					}
					
				}catch(Exception e){
					e.printStackTrace();
					remove(getChildren());
				}
				
				FRCREvent[] events = findRobotEvents(robotProject);
				if(events != null){add(events);}
			}
			
			
		}
		
		enableEventPassing();
		FRCModel.clearCircularReference();
	}
	
	/**
	 * Finds the IType which represents the robot in this project
	 * @param project IJavaProject to search
	 * @return IType representation of the robot class in this project
	 */
	protected static IType findRobotType(IJavaProject project){
		ICompilationUnit[] compUnits = ModelBuilderUtil.findCompilationUnits(project);

		if (compUnits != null) {
			for (ICompilationUnit curCompUnit : compUnits) {
				try {
					IType[] cuTypes = curCompUnit.getTypes();

					for (IType curType : cuTypes) {
						if (FRCRobot.meetsRequirements(curType)) {
							return curType;
						}
					}
				} catch (Exception e) {}
			}
		}

		String[] currentNames = ParseConstants.getRobotQualifiedNames(project);
		
		if(!ParseConstants.getRobotQualifiedNames(project).equals(currentNames)){
			return findRobotType(project);
		}
		
		return null;
	}
	
	/**
	 * Returns all the events that are present in the project
	 * @param project 
	 * @return
	 */
	protected static FRCREvent[] findRobotEvents(IJavaProject project){
		ArrayList<FRCREvent> events = new ArrayList<FRCREvent>();
		
		ICompilationUnit[] compUnits = ModelBuilderUtil.findCompilationUnits(project);

		if (compUnits != null) {
			for (ICompilationUnit curCompUnit : compUnits) {
				try {
					IType[] cuTypes = curCompUnit.getTypes();

					for (IType curType : cuTypes) {
						if (FRCREvent.meetsRequirements(curType)) {
							events.add(new FRCREvent(curType));
						}
					}
				} catch (Exception e) {return null;}
			}
		}
		
		return events.toArray(new FRCREvent[events.size()]);
	}
	
	/**
	 * @return An array of FRCREvents contained in this robot project
	 */
	public FRCREvent[] getEvents(){
		ArrayList<FRCREvent> events = new ArrayList<FRCREvent>();
		
		for(FRCRElement child : children){
			if(child instanceof FRCREvent){
				events.add((FRCREvent)child);
			}
		}
		
		return events.toArray(new FRCREvent[events.size()]);
	}
	
	/**
	 * @return The IPackageFragment of the robot class, or null if none
	 */
	public IPackageFragment getRobotPackageFragment(){
		if(deviceType == null){return null;}
		
		return deviceType.getPackageFragment();
	}
	
	@Override
	public boolean canModify(){return true;}
	
	@Override
	public boolean rename(Shell shell, String newName){
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(deviceType, newName, conditions);

			if (!rename.preCheck().isOK()) return false;

			String oldElementName = getElementName();
			String oldReplaceName = deviceType.getElementName();
			
			rename.perform(shell, new ProgressMonitorDialog(null));
			
			ManifestRename.renameInManifest(deviceType, oldReplaceName, newName);
			
			notifyListeners(new FRCModelEvent(
					this,
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RENAME,
					oldElementName + " was renamed to " + getElementName()));

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public String getName() {
		if(deviceType != null){
			return deviceType.getElementName();
		}
		
		return null;
	}

	@Override
	public int getRenameType() {return FRCModel.TYPE_RENAME;}
	
	@Override
	public void removeFromCode(){}
	
	@Override
	public String getCapabilityCodeFragment(FRCRCapability cap){
		if(!containsDeep(cap.getFullyQualifiedName())){return null;}
		
		if((deviceType != null)
				&&ModelBuilderUtil.isSameCompilationUnit(FRCModel.getActiveJavaFile(),
				deviceType.getCompilationUnit())
				&&(contains(cap.getFullyQualifiedName()))){
			return "";
		}
			
		String addition = null;
				
		for(FRCRElement cur : children){
			if(cur instanceof FRCRParent){
				if((addition = 
					((FRCRParent)cur).getCapabilityCodeFragment(cap)) != null){
					return addition;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public FRCRobot getRobot(){return this;}
	
	@Override
	public FRCRDevice getParentDevice(){return this;}
	
	@Override
	public FRCRMechanism getParentMechanism(){return this;}
	
	@Override
	public String getElementName(){
		if(deviceType != null){
			return "{R:" + deviceType.getElementName() + "}";
		}
	
		return "{R:NULLROBOT}";
	}
	
	@Override
	public String getDisplayName(){
		String retName = "NULL_ROBOT";
		
		if(deviceType != null){
			retName = deviceType.getElementName();
		}
		
		if(getFRCProject() != null){
			retName +=  " : " + getFRCProject().getDisplayName();
		}
		
		return retName;
	}
	
	@Override
	public ModelElementType getElementType(){return ModelElementType.FRCROBOT;}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		reconcile(new FRCRobot(getFRCProject().getJavaProject()));
		
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
		//Only need to check against types comp unit, since fields are handled
		//by parent
		if((deviceType == null)||(!deviceType.exists())){
			rebuild();
			return;
		}
		
		if(element != null){
			if(element instanceof ICompilationUnit){
				ICompilationUnit unit = (ICompilationUnit)element;
				if((deviceType != null)&&(deviceType.getCompilationUnit() != null)){
					if(ModelBuilderUtil.isSameCompilationUnit(unit,
							deviceType.getCompilationUnit())){
						//Use field, since children are still processed, 
						//and keeps proper associations
						FRCRobot recMech = new FRCRobot(deviceType.getJavaProject());
						reconcile(recMech);
						return;
					}
				}
			}
			
			super.runUpdate(element, node);
		}
	}
	
	@Override
	public FRCVParent getViewModel(){
		FRCVParent robotDisplay = super.getViewModel();
		
		FRCREvent[] events = getEvents();
		
		if(events.length > 0){
			FRCVParent eventRoot = 
				new FRCVParent("Events", ModelElementType.FRCREVENT);
			
			for(FRCREvent currentEvent : events){
				eventRoot.add(currentEvent.getViewModel());
			}
			
			robotDisplay.add(eventRoot);
		}
		
		return robotDisplay;
	}
	
	@Override
	public void receiveRobotEvent(FRCModelEvent event){notifyRobot(event);}
	
	@Override
	public void notifyRobot(FRCModelEvent event){
		//In the case of the robot, "notify robot" means let everyone in the robot
		//know that the event occurred
		//Don't have to check for equals, as there is only one robot ever
		for(FRCRElement child : children){
			if(child instanceof FRCRParent){
				((FRCRParent)child).receiveRobotEvent(event);
			}
		}
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCRobot safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCRobot
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IType){
			IType typeTest = (IType)candidate;
			
			return ModelBuilderUtil.isClass(typeTest, ParseConstants.getRobotQualifiedNames(typeTest.getJavaProject()));
		}else if(candidate instanceof IJavaProject){
			try{
				IJavaProject proj = (IJavaProject)candidate;
				if(proj.getProject().hasNature(FRCProjectNature.FRC_PROJECT_NATURE)){
					if(findRobotType(proj) != null){return true;}
				}
			}catch(Exception e){}
		}
		
		return false;
	}
	
	/**
	 * Creates the best fit FRCRElement for the given object
	 * @param element The element to create from
	 * @return Corresponding FRCRElement, or null if none exists 
	 */
	public static FRCRElement createFRCRElement(Object element){
		if(element instanceof IMethod){
			return createFRCRCapability((IMethod)element);
		}else if(element instanceof IType){
			return createFRCREvent((IType)element);
		}else if(element instanceof IField){
			FRCRElement attempt = null;
			IField field = (IField)element;
			
			attempt = createFRCRMechanism(field);
			
			if(attempt != null){return attempt;}
			
			attempt = createFRCRDevice(field);
			
			if(attempt != null){return attempt;}
			
			attempt = createFRCRStateMachine(field);
			
			if(attempt != null){return attempt;}
			
			attempt = createFRCRState(field);
			
			if(attempt != null){return attempt;}
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCRCapability from the given IMethod if possible.  The
	 * IMethod must have the proper modifiers if it comes from a class file
	 * @param capabilityMethod The IMethod to build from
	 * @return The created FRCRCapability, or null if the capability could not be
	 * created
	 */
	public static FRCRCapability createFRCRCapability(IMethod capabilityMethod){
		if(FRCRCapability.meetsRequirements(capabilityMethod)){
			return new FRCRCapability(capabilityMethod);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCRDevice from the given IField if possible.  The
	 * IField must implement the proper device interface
	 * @param deviceField The IField to build from
	 * @return The created FRCRDevice, or null if the device could not be
	 * created
	 */
	public static FRCRDevice createFRCRDevice(IField deviceField){
		if(FRCRDevice.meetsRequirements(deviceField)){
			return new FRCRDevice(deviceField);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCREvent from the given Object if possible.  The
	 * Object must extend the event base class
	 * @param eventType The type to build from
	 * @return The created FRCREvent, or null if the event could not be
	 * created
	 */
	public static FRCREvent createFRCREvent(IType eventType){
		if(FRCREvent.meetsRequirements(eventType)){
			return new FRCREvent(eventType);
		}
		
		return null;
	}
	
	
	
	/**
	 * Creates an FRCRMechanism from the given IField if possible.  The
	 * IField must implement the proper mechanism interface
	 * @param mechansimField The IField to build from
	 * @return The created FRCRMechansim, or null if the mechanism could not be
	 * created
	 */
	public static FRCRDevice createFRCRMechanism(IField mechanismField){
		if(FRCRMechanism.meetsRequirements(mechanismField)){
			return new FRCRMechanism(mechanismField);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCRState from the given Object if possible.  The
	 * Object must extend the base state class
	 * @param stateField The field to build from
	 * @return The created FRCRState, or null if the state could not be
	 * created
	 */
	public static FRCRState createFRCRState(IField stateField){
		if(FRCRState.meetsRequirements(stateField)){
			return new FRCRState(stateField);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCRStateMachine from the given Object if possible.  The
	 * Object must extend the base state machine class
	 * @param smField The Object to build from
	 * @return The created FRCRStateMachine, or null if the machine could not be
	 * created
	 */
	public static FRCRStateMachine createFRCRStateMachine(IField smField){
		if(FRCRStateMachine.meetsRequirements(smField)){
			return new FRCRStateMachine(smField, null);
		}
		
		return null;
	}
	
}
