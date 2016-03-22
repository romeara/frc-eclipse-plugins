package edu.wpi.first.javadev.builder.view.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import edu.wpi.first.codedev.output.FRCDialog;
import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.view.dnd.PaletteToRobotTransfer;
import edu.wpi.first.javadev.builder.view.dnd.RobotViewDropAdapter;
import edu.wpi.first.javadev.builder.view.event.FieldWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.FieldWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.view.event.ITypeWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.ITypeWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.view.event.MethodWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.MethodWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.view.event.RenameWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.RenameWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.view.filter.CapabilityFilter;
import edu.wpi.first.javadev.builder.view.filter.DeviceFilter;
import edu.wpi.first.javadev.builder.view.filter.MechanismFilter;
import edu.wpi.first.javadev.builder.wizard.wizards.NewCapabilityWizard;
import edu.wpi.first.javadev.builder.wizard.wizards.NewFRCRElementWizard;
import edu.wpi.first.javadev.builder.wizard.wizards.NewFieldWizard;
import edu.wpi.first.javadev.builder.wizard.wizards.RenameWizard;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.FRCProject;
import edu.wpi.first.javadev.builder.workspace.model.IComplexRename;
import edu.wpi.first.javadev.builder.workspace.model.IStandardRename;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.library.FRCLElement;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRDevice;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRElement;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRMechanism;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRParent;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRStateMachine;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRobot;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCView;

/** 
 * Represents the robot code view in the eclipse workspace
 * 
 * @author Ryan O'Meara
 */
public class RoboCentricView extends BaseCodeView {
	public static final String ID = "edu.wpi.first.javadev.builder.RobotModelView";
	
	//Model
	protected FRCView treeMod;
	//Filters 
	protected ViewerFilter mechFilter;
	protected ViewerFilter devFilter;
	protected ViewerFilter capFilter;
	//DND
	protected RobotViewDropAdapter dropAdapter;  
	//Filters 
	protected Action mechFilterAction;
	protected Action devFilterAction;
	protected Action capFilterAction;
	//Tree Mods
	protected Action removeAction;
	protected Action addCapAction;		
	protected Action addDevAction;
	protected Action addEventAction;
	protected Action addMechAction;
	protected Action addStateAction;
	protected Action standardRename;
	protected Action fieldRename;
	protected Action typeRename;
	
	public RoboCentricView(){
		super();
		treeMod = CodeViewerPlugin.getFRCModel().getRobotViewModel();
	}
	
	/** Adds Drop Support to the view */ 
	protected void addDropSupport() {
	    int ops = DND.DROP_MOVE | DND.DROP_COPY;
	    Transfer[] transfers = new Transfer[] {PaletteToRobotTransfer.getInstance()};
	    dropAdapter = new RobotViewDropAdapter(treeViewer, this);
	    treeViewer.addDropSupport(ops, transfers, dropAdapter);
	}
	
	/** Handles a drop event */
	public boolean handleDrop(FRCLElement[] palElement, Object target){
		FRCProject frcProject = CodeViewerPlugin.getFRCModel().getActiveProject();
		
		if(frcProject == null){return false;}
		
		FRCRElement frcRTarget = frcProject.getRobot();
		
		if((target != null)&&(target instanceof FRCVElement)){
			FRCElement element = ((FRCVElement)target).getModelElement();
			
			if(element instanceof FRCRElement){
				frcRTarget = (FRCRElement)element;
			}else if((element == null)&&(((FRCVElement)target).getParent() != null)){
				element = ((FRCVParent)((FRCVElement)target).getParent()).getModelElement();
				
				if(element instanceof FRCRElement){
					frcRTarget = (FRCRElement)element;
				}
			}
		}
		
		if(palElement == null){return false;}
		
		//add to model
		FRCRDevice parent = null;
		
		if(!(frcRTarget instanceof FRCRDevice)){
			parent = frcRTarget.getParentDevice();
		}else{
			parent = (FRCRDevice)frcRTarget;
		}
			
		for(FRCLElement current : palElement){
			if((current != null)
					&&(current.getElementType().equals(ModelElementType.FRCLDEVICE))){
					addToRobot(new FRCRDevice[]{parent}, current.getType(), "device");
			}
		}
			
		return true;
		
	}
	
	/** Adds a mechanism to the primary selected parent */
	protected void addMech(final FRCRParent[] addto){
		class MechListener implements ITypeWizardFinishedEventListener{
			private FRCRMechanism[] targets;
			
			public MechListener(FRCRMechanism[] targs){
				targets = targs;
			}
			
			@Override
			public void receiveEvent(ITypeWizardFinishEvent event) {
				if(event.getFinished()){
					addToRobot(targets, event.getCreatedType(), "mechanism");
				}
			}
		}
		
		ArrayList<FRCRMechanism> targets = new ArrayList<FRCRMechanism>();
		
		for(FRCElement current : addto){
			if(current instanceof FRCRMechanism){
				targets.add((FRCRMechanism)current);
			}
		}
		
		if(targets.size() > 0){
			NewFRCRElementWizard wizard = 
				new NewFRCRElementWizard(ParseConstants.MECHANISM, 
						new MechListener(targets.toArray(
								new FRCRMechanism[targets.size()])));
	        WizardDialog dialog = 
	        	new WizardDialog(
	        			CodeViewerPlugin.getDefault().getWorkbench().getDisplay()
	        			.getActiveShell(), wizard);
	        dialog.create();
	        dialog.open();
		}
	}

	/** Adds a device to the primary selected parent */
	protected void addDev(final FRCRParent[] addto){
		class DevListener implements ITypeWizardFinishedEventListener{
			private FRCRDevice[] targets;
			
			public DevListener(FRCRDevice[] targs){
				targets = targs;
			}
			
			@Override
			public void receiveEvent(ITypeWizardFinishEvent event) {
				if(event.getFinished()){
					addToRobot(targets, event.getCreatedType(), "device");
				}
			}
		}
		
		ArrayList<FRCRDevice> targets = new ArrayList<FRCRDevice>();
		
		for(FRCElement current : addto){
			if(current instanceof FRCRDevice){
				targets.add((FRCRDevice)current);
			}
		}
		
		
		
		if(targets.size() > 0){
			NewFRCRElementWizard wizard = 
				new NewFRCRElementWizard(ParseConstants.DEVICE, 
						new DevListener(targets.toArray(
								new FRCRDevice[targets.size()])));
	        WizardDialog dialog = 
	        	new WizardDialog(CodeViewerPlugin.getDefault().getWorkbench()
	        			.getDisplay().getActiveShell(), wizard);
	        dialog.create();
	        dialog.open();
		}
	}
	
	/** Adds a capability to the primary selected parent */
	protected void addCap(final FRCRParent[] addto){
		class CapListener implements MethodWizardFinishedEventListener{		
			private FRCRDevice[] targets;
			
			public CapListener(FRCRDevice[] targs){
				targets = targs;
			}
			
			@Override
			public void receiveEvent(MethodWizardFinishEvent event) {
				if(event.getFinished()){
					addToRobot(targets, 
							event.getVisibility(), event.getReturnType(), 
							event.getMethodName(), event.getParameters());
				}
			}
		}
		
		ArrayList<FRCRDevice> targets = new ArrayList<FRCRDevice>();
		
		for(FRCElement current : addto){
			if(current instanceof FRCRDevice){
				targets.add((FRCRDevice)current);
			}
		}
		
		if(targets.size() > 0){
			NewCapabilityWizard wizard = 
				new NewCapabilityWizard(
						new CapListener(targets.toArray(
								new FRCRDevice[targets.size()])));
			WizardDialog dialog = 
				new WizardDialog(CodeViewerPlugin.getDefault().getWorkbench()
						.getDisplay().getActiveShell(), wizard);
	        dialog.create();
	        dialog.open();
		}
	}
	
	/** Adds an event to the primary selected parent */
	protected void addEvent(final FRCRParent[] addto){
		//TODO add event action - pop-up dialog, class create
	}
	
	/** Adds a state to the primary selected parent */
	protected void addState(final FRCRParent[] addto){
		//TODO add state action - pop-up dialog, class create
	}
	
	/** Removes selected elements */ 
	protected void removeSelected(final FRCRElement[] remove) {
		for(FRCRElement current : remove){
			current.removeFromCode();
		}
	}
	
	protected void renameSelected(final FRCRElement[] rename){
		class renameFinished implements RenameWizardFinishedEventListener{
			@Override
			public void receiveEvent(RenameWizardFinishEvent event) {
				if((event.getFinished())&&(event.getNewName() != null)){
					((IStandardRename)rename[0]).rename(
							Display.getDefault().getActiveShell(), 
							event.getNewName());
				}
			}	
		}
		
		//Robot, Capability, event
		if((rename.length == 1)
				&&(rename[0] instanceof IStandardRename)){
			IStandardRename subject = (IStandardRename)rename[0];
			
			
			
			RenameWizard wizard = new RenameWizard(new renameFinished(),
					subject.getName(),
					subject.getRenameType());
			WizardDialog dialog = 
				new WizardDialog(CodeViewerPlugin.getDefault().getWorkbench()
						.getDisplay().getActiveShell(), wizard);
	        dialog.create();
	        dialog.open();
		}else{
			FRCDialog.createErrorDialog("Cannot rename more than one element at a time",
					new Status(IStatus.ERROR,
							CodeViewerPlugin.PLUGIN_ID,
							"Batch renaming is not allowed"));
		}
	}
	
	protected void renameSelectedField(final FRCRElement[] rename){
		class renameFinished implements RenameWizardFinishedEventListener{
			@Override
			public void receiveEvent(RenameWizardFinishEvent event) {
				if((event.getFinished())&&(event.getNewName() != null)){
					((IComplexRename)rename[0]).renameField(
							Display.getDefault().getActiveShell(), 
							event.getNewName());
				}
			}	
		}
		
		//Device, state
		if((rename.length == 1)
				&&(rename[0] instanceof IComplexRename)){
			IComplexRename subject = (IComplexRename)rename[0];
			
			RenameWizard wizard = new RenameWizard(new renameFinished(),
					subject.getFieldName(),
					FRCModel.FIELD_RENAME);
			WizardDialog dialog = 
				new WizardDialog(CodeViewerPlugin.getDefault().getWorkbench()
						.getDisplay().getActiveShell(), wizard);
	        dialog.create();
	        dialog.open();
		}else{
			FRCDialog.createErrorDialog("Cannot rename more than one element at a time",
					new Status(IStatus.ERROR,
							CodeViewerPlugin.PLUGIN_ID,
							"Batch renaming is not allowed"));
		}
	}
	
	protected void renameSelectedType(final FRCRElement[] rename){
		class renameFinished implements RenameWizardFinishedEventListener{
			@Override
			public void receiveEvent(RenameWizardFinishEvent event) {
				if((event.getFinished())&&(event.getNewName() != null)){
					((IComplexRename)rename[0]).renameClass(
							Display.getDefault().getActiveShell(), 
							event.getNewName());
				}
			}	
		}
		
		//Device, state
		if((rename.length == 1)
				&&(rename[0] instanceof IComplexRename)){
			IComplexRename subject = (IComplexRename)rename[0];
			
			
			
			RenameWizard wizard = new RenameWizard(new renameFinished(),
					subject.getClassName(),
					FRCModel.TYPE_RENAME);
			WizardDialog dialog = 
				new WizardDialog(CodeViewerPlugin.getDefault().getWorkbench()
						.getDisplay().getActiveShell(), wizard);
	        dialog.create();
	        dialog.open();
		}else{
			FRCDialog.createErrorDialog("Cannot rename more than one element at a time",
					new Status(IStatus.ERROR,
							CodeViewerPlugin.PLUGIN_ID,
							"Batch renaming is not allowed"));
		}
	}
	
	/**
	 * Adds a field, either mechanism or device, to the given devices
	 * @param addTo The array of FRCRDevices to add to
	 * @param classToAdd The IType of the field to add
	 */
	protected void addToRobot(FRCRDevice[] addTo, final IType classToAdd, String elementType){
		class FieldListener implements FieldWizardFinishedEventListener{		
			private FRCRDevice[] targets;
			
			public FieldListener(FRCRDevice[] targs){
				targets = targs;
			}
			
			@Override
			public void receiveEvent(FieldWizardFinishEvent event) {
				if(event.getFinished()){
					String name = event.getFieldName();
					String vis = event.getVisibility();
					for(FRCRDevice curDev : targets){curDev.addField(vis, name, classToAdd);}
				}
			}
		}
		
		if((addTo == null)||(addTo.length <= 0)||(classToAdd == null)){return;}
		
		NewFieldWizard wizard = new NewFieldWizard(new FieldListener(addTo), 
				elementType, classToAdd.getElementName().toLowerCase());
		WizardDialog dialog = 
			new WizardDialog(CodeViewerPlugin.getDefault().getWorkbench()
					.getDisplay().getActiveShell(), wizard);
        dialog.create();
        dialog.open();
	}
	
	/**
	 * Adds a method with the given parameters to the given device
	 * @param addTo The array of devices to add to
	 * @param visibility The visibility of the method to create
	 * @param returnType The return type of the method to create
	 * @param name The name of the method to create
	 * @param parameters The parameters of the method to create
	 */
	protected void addToRobot(FRCRDevice[] addTo, String visibility, 
			String returnType, String name, String parameters){
		if((addTo == null)||(addTo.length <= 0)){return;}
		
		for(FRCRDevice curDev : addTo){
			curDev.addMethod(visibility, returnType, name, parameters);
		}
	}
	
	/**
	 * Adds an event type to the given robot
	 * @param addto The robot to add to
	 * @param eventToAdd The event type to add to the robot project
	 */
	protected void addToRobot(FRCRobot addto, IType eventToAdd){
		//TODO RoboCentricView: addToRobot: Method Stub
	}
	
	/**
	 * Adds a state to the given state machine
	 * @param addto The state machine to add to
	 * @param stateToAdd The state to add
	 */
	protected void addToRobot(FRCRStateMachine addto, IType stateToAdd){
		//TODO RoboCentricView: addToRobot: Method Stub
	}
	
	/**
	 * Creates an array which is all mechanisms - any element sent in lower than
	 * a mechanism is replaced by its parent mechanism
	 * @param selection The original selection
	 * @return The selection of mechanisms, no duplicates
	 */
	protected FRCRMechanism[] mechanismSelection(FRCRElement[] selection){
		ArrayList<FRCRMechanism> parents = new ArrayList<FRCRMechanism>();
		FRCRMechanism parent;
		
		for(FRCRElement element : selection){
			if(element instanceof FRCRMechanism){
				if(!parents.contains((FRCRMechanism)element)){
					parents.add((FRCRMechanism)element);
				}
			}else if(!parents.contains((parent = ((FRCRElement)element).getParentMechanism()))){
				parents.add(parent);
			}
		}
		
		return parents.toArray(new FRCRMechanism[parents.size()]);
	}
	
	/**
	 * Creates an array which is all modifiable devices - any element sent in lower than
	 * a device, or isn't modifiable, is replaced by its parent device, or if not modifiable
	 * ignored
	 * @param selection The original selection
	 * @return The selection of devices, no duplicates or unmodifiable devices
	 */
	protected FRCRDevice[] deviceSelection(FRCRElement[] selection){
		ArrayList<FRCRDevice> parents = new ArrayList<FRCRDevice>();
		FRCRDevice parent;
		
		for(FRCRElement element : selection){
			if(element instanceof FRCRDevice){
				if((!parents.contains((FRCRDevice)element))
						&&(((FRCRDevice)element).canModifyType())){
					parents.add((FRCRDevice)element);
				}
			}else if(!parents.contains((parent = ((FRCRElement)element).getParentDevice()))){
				if(parent.canModifyType()){parents.add(parent);}
			}
		}
		
		return parents.toArray(new FRCRDevice[parents.size()]);
	}
	
	/**
	 * Creates an array which is all devices - any element sent in lower than
	 * a device is replaced by its parent device. This should only be used when
	 * it is known for certain that devices which are not modifiable can be operated on
	 * @param selection The original selection
	 * @return The selection of devices, no duplicates
	 */
	protected FRCRDevice[] fullDeviceSelection(FRCRElement[] selection){
		ArrayList<FRCRDevice> parents = new ArrayList<FRCRDevice>();
		FRCRDevice parent;
		
		for(FRCRElement element : selection){
			if(element instanceof FRCRDevice){
				if((!parents.contains((FRCRDevice)element))){
					parents.add((FRCRDevice)element);
				}
			}else if(!parents.contains((parent = ((FRCRElement)element).getParentDevice()))){
				if(parent.canModifyType()){parents.add(parent);}
			}
		}
		
		return parents.toArray(new FRCRDevice[parents.size()]);
	}
	
	/**
	 * @return A array of selected elements which are FRCRElements and can be modified
	 */
	protected FRCRElement[] modifiableComponentSelection(boolean includeContainers){
		FRCElement[] selected = getSelected(includeContainers);
		
		if(selected == null){return null;}
		
		ArrayList<FRCRElement> components = new ArrayList<FRCRElement>();
		
		for(FRCElement element : selected){
			if((element instanceof FRCRElement)&&(element.canModify())){
				components.add((FRCRElement)element);
			}
		}
		
		return components.toArray(new FRCRElement[components.size()]);
	}
	
	/**
	 * @return A array of selected elements which are FRCRElements
	 */
	protected FRCRElement[] componentSelection(boolean includeContainers){
		FRCElement[] selected = getSelected(includeContainers);
		
		if(selected == null){return null;}
		
		ArrayList<FRCRElement> components = new ArrayList<FRCRElement>();
		
		for(FRCElement element : selected){
			if(element instanceof FRCRElement){
				components.add((FRCRElement)element);
			}
		}
		
		return components.toArray(new FRCRElement[components.size()]);
	}
	
	
	@Override
	protected void addAdditionalDNDSupport(){
		addDropSupport();
	}
	
	@Override
	protected void addToolbarButtons() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(addMechAction);
		toolbarManager.add(addDevAction);
		//TODO uncomment these out after addition methods have been tested to work
		toolbarManager.add(addCapAction);
		/*toolbarManager.add(addEventAction);
		toolbarManager.add(addStateAction);*/
		toolbarManager.add(removeAction);
	}
	
	@Override
	protected void createActions() {
		
		mechFilterAction = new Action("Mechanisms") {
			public void run() {
				updateFilters(mechFilterAction);
			}
		};
		mechFilterAction.setChecked(false);

		
		devFilterAction = new Action("Devices") {
			public void run() {
				updateFilters(devFilterAction);
			}
		};
		devFilterAction.setChecked(false);
		
		capFilterAction = new Action("Capabilities") {
			public void run() {
				updateFilters(capFilterAction);
			}
		};
		capFilterAction.setChecked(false);
		
		
		removeAction = new Action("Remove") {
			public void run() {
				removeSelected(modifiableComponentSelection(false));
			}			
		};
		removeAction.setToolTipText("Remove");
		removeAction.setImageDescriptor(CodeViewerPlugin.getImageDescriptor("Remove.png"));	
		
		addCapAction = new Action("Add Capability") {
			public void run() {
				addCap(deviceSelection(modifiableComponentSelection(true)));
			}			
		};
		addCapAction.setToolTipText("Add Capability");
		addCapAction.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				ModelElementType.FRCRCAPABILITY.getAddIconPath()));
		
		addDevAction = new Action("Add Device") {
			public void run() {
				addDev(deviceSelection(modifiableComponentSelection(true)));
			}			
		};
		addDevAction.setToolTipText("Add Device");
		addDevAction.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				ModelElementType.FRCRDEVICE.getAddIconPath()));
		
		addEventAction = new Action("Add Event") {
			public void run() {
				addEvent(mechanismSelection(modifiableComponentSelection(true)));
			}			
		};
		addEventAction.setToolTipText("Add Event");
		addEventAction.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				ModelElementType.FRCREVENT.getAddIconPath()));
		
		addMechAction = new Action("Add Mechanism") {
			public void run() {
				addMech(mechanismSelection(modifiableComponentSelection(true)));
			}			
		};
		addMechAction.setToolTipText("Add Mechanism");
		addMechAction.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				ModelElementType.FRCRMECHANISM.getAddIconPath()));
		
		addStateAction = new Action("Add State") {
			public void run() {
				addState(mechanismSelection(modifiableComponentSelection(true)));
			}			
		};
		addStateAction.setToolTipText("Add State");
		addStateAction.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				ModelElementType.FRCRSTATE.getAddIconPath()));
		
		standardRename = new Action("Rename") {
			public void run() {
				renameSelected(modifiableComponentSelection(true));
			}			
		};
		standardRename.setToolTipText("Rename Element");
		standardRename.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				"Rename.png"));
		
		fieldRename = new Action("Rename Field") {
			public void run() {
				renameSelectedField(fullDeviceSelection(modifiableComponentSelection(true)));
			}			
		};
		fieldRename.setToolTipText("Rename declared variable");
		fieldRename.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				"Rename.png"));
		
		typeRename = new Action("Rename Class") {
			public void run() {
				renameSelectedType(deviceSelection(modifiableComponentSelection(true)));
			}			
		};
		typeRename.setToolTipText("Rename element class");
		typeRename.setImageDescriptor(CodeViewerPlugin.getImageDescriptor(
				"Rename.png"));
		
	}
	
	@Override
	protected void createFilters(){
		mechFilter = new MechanismFilter();
		devFilter = new DeviceFilter();
		capFilter = new CapabilityFilter();
	}
	
	@Override
	protected void fillContextMenuElements(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		FRCVElement object = (FRCVElement)selection.getFirstElement();
		
		if(!object.canModify()){return;}
		
		if(object != null){
			
			
			if(canRenameField(object)){manager.add(fieldRename);}
			
			if(canRenameType(object)){manager.add(typeRename);}
			
			if(canRename(object)){manager.add(standardRename);}
			
			if(canAddMechanism(object)){manager.add(addMechAction);}
			
			if(canAddDevice(object)){manager.add(addDevAction);}
			
			if(canAddCapability(object)){manager.add(addCapAction);}
			
			//TODO Deal with/uncomment these conditionals when they are ready to be used
			/*if(canAddState(object)){//state add}
			if(canAddTransition(object)){//transition add}
			if(canAddEvent(object)){//event add}*/
			if(canRemove(object)){manager.add(removeAction);}
		}
	}
	
	protected boolean canRenameField(FRCVElement target){
		if((target.getModelElement() != null)
				&&(!(target.getModelElement() instanceof FRCRobot))
				&&(target.getModelElement() instanceof IComplexRename)){
			return true;
		}
		
		return false;
	}
	
	protected boolean canRenameType(FRCVElement target){
		if(canRenameField(target)
				&&((!(target.getModelElement() instanceof FRCRDevice))
						||(((FRCRDevice)target.getModelElement()).canModifyType()))){
			return true;
		}
		
		return false;
	}
	
	protected boolean canRename(FRCVElement target){
		if((target.getModelElement() != null)&&(!canRenameField(target))){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if a mechanism can be added to this element correctly, either directly 
	 * or indirectly (container elements will return true for the method checking against
	 * what they contain)
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canAddMechanism(FRCVElement target){
		return target.getElementType().equals(ModelElementType.FRCRMECHANISM)
		||target.getElementType().equals(ModelElementType.FRCROBOT);
	}
	
	/**
	 * Determines if a device can be added to this element correctly, either directly 
	 * or indirectly (container elements will return true for the method checking against
	 * what they contain)
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canAddDevice(FRCVElement target){
		return (canAddMechanism(target)&&(target.getModelElement() != FRCVElement.NO_ASSOC_ELEMENT))
		||(target.getElementType().equals(ModelElementType.FRCRDEVICE)
				&&((target.getModelElement() == FRCVElement.NO_ASSOC_ELEMENT)
				||((target.getModelElement() instanceof FRCRDevice)
						&&(((FRCRDevice)target.getModelElement()).canModifyType()))));
	}
	
	
	
	/**
	 * Determines if a capability can be added to this element correctly, either directly 
	 * or indirectly (container elements will return true for the method checking against
	 * what they contain)
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canAddCapability(FRCVElement target){
		return ((canAddDevice(target))&&(target.getModelElement() != FRCVElement.NO_ASSOC_ELEMENT))
		||((target.getElementType().equals(ModelElementType.FRCRCAPABILITY))
				&&(target.getModelElement() == FRCVElement.NO_ASSOC_ELEMENT));
	}
	
	/**
	 * Determines if a state can be added to this element correctly, either directly 
	 * or indirectly (container elements will return true for the method checking against
	 * what they contain)
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canAddState(FRCVElement target){
		//TODO RoboCentricView: canAddState: Method Stub
		return false;
	}
	
	/**
	 * Determines if a transition can be added to this element correctly, either directly 
	 * or indirectly (container elements will return true for the method checking against
	 * what they contain)
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canAddTransition(FRCVElement target){
		//TODO RoboCentricView: canAddTransition: Method Stub
		return false;
	}
	
	/**
	 * Determines if an event can be added to this element correctly, either directly 
	 * or indirectly (container elements will return true for the method checking against
	 * what they contain)
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canAddEvent(FRCVElement target){
		//TODO RoboCentricView: canAddEvent: Method Stub
		return false;
	}
	
	/**
	 * Determines if the element represents an element which can be removed
	 * @param target The view element being targeted
	 * @return true if the indicated element can be added to the given target
	 */
	protected boolean canRemove(FRCVElement target){
		//TODO Make this allow remove of transitions once they have valid remove code
		return (!target.getElementType().equals(ModelElementType.FRCROBOT))
		&&(!target.getElementType().equals(ModelElementType.FRCRTRANSITION))
		&&(target.getModelElement() != FRCVElement.NO_ASSOC_ELEMENT);
	}
	
	@Override
	protected void fillFilterMenu(IMenuManager rootMenuManager) {
		IMenuManager filterSubmenu = new MenuManager("Filters");
		rootMenuManager.add(filterSubmenu);
		filterSubmenu.add(mechFilterAction);
		filterSubmenu.add(devFilterAction);
		filterSubmenu.add(capFilterAction);
	}
	
	@Override
	protected FRCVParent getDisplayTreeRoot() {
		return treeMod;
	}
	
	@Override
	protected void updateFilters(Action action) {
		
		if(action == mechFilterAction) {
			if(action.isChecked()) {
				treeViewer.addFilter(mechFilter);
				treeViewer.removeFilter(devFilter);
				treeViewer.removeFilter(capFilter);
				devFilterAction.setChecked(false);
				capFilterAction.setChecked(false);
			} else {
				treeViewer.removeFilter(mechFilter);
			}
		} 
		
		if(action == devFilterAction) {
			if(action.isChecked()) {
				treeViewer.addFilter(devFilter);
				treeViewer.removeFilter(mechFilter);
				treeViewer.removeFilter(capFilter);
				mechFilterAction.setChecked(false);
				capFilterAction.setChecked(false);
			} else {
				treeViewer.removeFilter(devFilter);
			}
		}
		
		if(action == capFilterAction) {
			if(action.isChecked()) {
				treeViewer.addFilter(capFilter);
				treeViewer.removeFilter(devFilter);
				treeViewer.removeFilter(mechFilter);
				devFilterAction.setChecked(false);
				mechFilterAction.setChecked(false);
			} else {
				treeViewer.removeFilter(capFilter);
			}
		}
		
		treeViewer.refreshTree();
	}

	@Override
	protected int getInitialLevel() {
		return 3;
	}

	@Override
	protected Transfer[] getTransferTypes() {
		return new Transfer[] {TextTransfer.getInstance()};
	}
}
