package edu.wpi.first.javadev.builder.workspace.model.robot;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import edu.wpi.first.codedev.output.FRCDialog;
import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.util.CircularRefNode;
import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.util.SourceModification;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.IComplexRename;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Represents a device in the robot model.  Can contain devices and capabilities
 * 
 * @author Ryan O'Meara
 */
public class FRCRDevice extends FRCRParent implements IComplexRename{
	/**
	 * Runnable to add new elements to the mechanism during construction.  Allows
	 * multi-threading the construction process.  Specific to Device as it does
	 * not allow the addition of Mechanisms
	 * 
	 * @author Ryan O'Meara
	 */
	private class AddElementToDevice implements Runnable{
		private IJavaElement addition;
		
		protected AddElementToDevice(IJavaElement add){addition = add;}
		
		@Override
		public void run(){
			FRCRElement curREl;
			if(((curREl = FRCRobot.createFRCRElement(addition)) != null)
					&&(!(curREl instanceof FRCRMechanism))){
					add(curREl);
				}
		}
	}
	
	/**
	 * Runnable to add a state machine to the device.  Allows for multi-threading
	 * 
	 * @author Ryan O'Meara
	 */
	protected class AddStateMachine implements Runnable{
		private IField smField;
		
		protected AddStateMachine(IField add){smField = add;}
		
		@Override
		public void run(){
			add(FRCRobot.createFRCRStateMachine(smField));
		}
	}
	
	protected IField deviceField;
	protected IType deviceType;
	
	protected FRCRDevice(){super();	}
	
	protected FRCRDevice(IField deviceDeclaration){
		super();
		deviceField = deviceDeclaration;
		deviceType = ModelBuilderUtil.createIFieldType(deviceField);

		if(deviceType != null){
			CircularRefNode node = FRCModel.getReferenceNode(deviceField, 
					new IJavaElement[]{deviceType});
			constructFromIType(deviceType, node);
		}
	}
	
	/**
	 * Performs constructing operations using an IType created from the input
	 * IField
	 * @param fieldType The type associated with the input IField
	 */
	protected void constructFromIType(IType fieldType, CircularRefNode thisDev){
		ArrayList<Thread> threads = new ArrayList<Thread>();
		disableEventPassing();
		try{
			IJavaElement[] javaChildren = fieldType.getChildren();
			
			boolean circularRef = false;
			
			for(IJavaElement currentElement : javaChildren){
				circularRef = false;
				if(FRCRDevice.meetsRequirements(currentElement)){
					FRCModel.createCircularReferenceNode(currentElement, 
							new IJavaElement[]{ModelBuilderUtil.createIFieldType(
									(IField)currentElement)}, 
							thisDev);
					circularRef = circularReference((IField)currentElement, thisDev);
					if(circularRef){
						add(new FRCRDevicePlaceholder((IField)currentElement));
						continue;
					}
				}
				
				if(currentElement instanceof IMethod){
					if(!methodUsable((IMethod)currentElement)){continue;}
				}
				
				if(!circularRef){
					if((fieldType.getCompilationUnit() != null)
							||(!(currentElement instanceof IMember))
							||(Flags.isPublic(((IMember)currentElement).getFlags()))){
						threads.add(new Thread(new AddElementToDevice(currentElement)));
					}
				}
			}
			
			if(FRCRStateMachine.meetsRequirements(deviceField)){
				threads.add(new Thread(new AddStateMachine(deviceField)));
			}
			
			for(Thread current : threads){current.start();}
			
			for(Thread current : threads){current.join();}
			
		}catch(Exception e){
			e.printStackTrace();
			remove(getChildren());
		}
		enableEventPassing();
	}
	
	/**
	 * Tests if the given field is a circular reference, direct or otherwise (is a
	 * class which references this class at some point)
	 * @param test The field to test
	 * @return The element the field is a circular reference to, or null if none
	 */
	public boolean circularReference(IField test, CircularRefNode node){
		IType fieldType = ModelBuilderUtil.createIFieldType(test);
		
		if(fieldType == null){return false;}
		
		if(node.isCircularReference(fieldType)){return true;}
		
		FRCElement parentCandidate = CodeViewerPlugin.getFRCModel().findElement(deviceType);
		FRCRDevice parent;
		
		if(parentCandidate instanceof FRCRDevice){
			parent = (FRCRDevice)CodeViewerPlugin.getFRCModel().findElement(deviceType);
		}else{
			parent = null;
		}
		
		while((parent != null)&&(!(parent instanceof FRCRobot))){
			if((parent.deviceType != null)&&(parent.deviceType.equals(fieldType))){
				return true;
			}
			
			parent = parent.getParentDevice();
		}
		
		if((parent != null)
				&&(parent.deviceType != null)
				&&(parent.deviceType.equals(fieldType))){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if a method is usable as a capability.  A method is NOT 
	 * usable when it is private or protected and part of a class file 
	 * (as opposed to a compilation unit)
	 * @param method The IMethod to check
	 * @return true if usable, false otherwise
	 */
	protected boolean methodUsable(IMethod method){
		if(method == null){return false;}
		
		if(method.getClassFile() != null){
			try{
				int flags = method.getFlags();
				
				if(Flags.isPrivate(flags)||Flags.isProtected(flags)){
					return false;
				}
			}catch(Exception e){return false;}
		}
		
		return true;
	}
	
	/**
	 * Determines if this device can be modified.  A device can only be modified if
	 * it is derived from a compilation unit.
	 * @return true if the device can be modified, false otherwise
	 */
	public boolean canModifyType(){
		if(deviceType != null){
			return (deviceType.getCompilationUnit() != null);
		}
		
		return false;
	}
	
	/**
	 * @return An array of FRCRCapabilitys contained in this device.
	 * Could contain place holders to prevent circular references
	 */
	public FRCRCapability[] getCapabilities(){
		ArrayList<FRCRCapability> caps = new ArrayList<FRCRCapability>();
		
		for(FRCRElement child : children){
			if(child instanceof FRCRCapability){
				caps.add((FRCRCapability)child);
			}
		}
		
		return caps.toArray(new FRCRCapability[caps.size()]);
	}
	
	/**
	 * @return An array of FRCRDevices contained in this device.  
	 * Could contain place holders to prevent circular references, since
	 * devices can contain devices
	 */
	public FRCRElement[] getDevices(){
		ArrayList<FRCRElement> devs = new ArrayList<FRCRElement>();
		
		for(FRCRElement child : children){
			if(child.getElementType().equals(ModelElementType.FRCRDEVICE)){
				devs.add(child);
			}
		}
		
		return devs.toArray(new FRCRElement[devs.size()]);
	}
	
	/**
	 * @return The FRCRStateMachine contained in this device, or null if none.
	 */
	public FRCRStateMachine getStateMachine(){
		for(FRCRElement child : children){
			if(child instanceof FRCRStateMachine){
				return (FRCRStateMachine)child;
			}
		}
		
		return null;
	}
	
	/**
	 * Adds a field with the given parameters to this device, which is 
	 * usually a device or mechanism
	 * @return true if added successfully, false otherwise
	 */
	public boolean addField(String visibility, String name, IType fieldClass){
		if(!meetsDevTypeRequirements(fieldClass)){return false;}
		
		disableOutsideUpdate();
		IField newField = addDeviceField(visibility, name, fieldClass);
		
		if(newField == null){
			enableOutsideUpdate();
			FRCDialog.createErrorDialog("Adding element field to Java type failed",
					new Status(IStatus.ERROR,
							CodeViewerPlugin.PLUGIN_ID,
							"Could not create IField in provided IType"));
			return false;
		}
		
		add(FRCRobot.createFRCRDevice(newField));
		
		notifyListeners(new FRCModelEvent(this,
				FRCModelEvent.FT_ADDED_NEW_ELEMENT,
				"Added new field"));
		
		enableOutsideUpdate();
		
		return false;
	}
	
	/**
	 * Adds a field to this device of the specified type and with the
	 * specified variable name
	 * @param name The name of the field to create
	 * @param fieldClass The type of variable to add
	 * @return A reference to the field which was created
	 */
	protected IField addDeviceField(String visibility, String name, IType fieldClass){
		return SourceModification.addField(deviceType, 
				fieldClass,
				visibility,
				name);
	}
	
	/**
	 * Adds a method with the given parameters to this device
	 * @return true if added successfully, false otherwise
	 */
	public boolean addMethod(String visibility, String returnType, 
			String name, String parameters){
		disableOutsideUpdate();
		
		if(deviceType == null){return false;}
		
		IMethod javaMethod = SourceModification.addMethod(
				deviceType, visibility, returnType, name, parameters);
		
		//Error in null case is handled by SourceModification
		if(javaMethod != null){add(FRCRobot.createFRCRCapability(javaMethod));}
		
		
		enableOutsideUpdate();
		
		return true;
	}
	
	@Override
	public boolean renameField(Shell shell, String newName){
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(deviceField, newName, conditions);

			if (!rename.preCheck().isOK()) return false;

			String oldElementName = getElementName();
			
			rename.perform(shell, new ProgressMonitorDialog(null));
			
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
	public String getFieldName(){
		if(deviceField != null){
			return deviceField.getElementName();
		}
		
		return null;
	}
	
	@Override
	public String getClassName(){
		if(deviceType != null){
			return deviceType.getElementName();
		}
		
		return null;
	}
	
	@Override
	public boolean renameClass(Shell shell, String newName){
		if((deviceType != null)
				&&(deviceType.getCompilationUnit() == null)){return false;}
		try {
			int conditions = 0;
			conditions = RenameSupport.UPDATE_GETTER_METHOD;
			conditions |= RenameSupport.UPDATE_REFERENCES;
			conditions |= RenameSupport.UPDATE_SETTER_METHOD;
			conditions |= RenameSupport.UPDATE_TEXTUAL_MATCHES;
			RenameSupport rename = RenameSupport.create(deviceType, newName, conditions);

			if (!rename.preCheck().isOK()) return false;

			String oldElementName = getElementName();
			
			rename.perform(shell, new ProgressMonitorDialog(null));
			
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
	public void removeFromCode(){
		try{
			if(deviceField != null){
				IJavaModel jm = deviceField.getJavaModel();
				jm.delete(new IJavaElement[]{deviceField}, false, null);
			}
		}catch(Exception e){}
		
		dispose();
	}
	
	@Override
	public String getCapabilityCodeFragment(FRCRCapability cap){
		if(!containsDeep(cap.getFullyQualifiedName())){return null;}
		
		if(deviceField != null){
			if(contains(cap.getFullyQualifiedName())){
				return deviceField.getElementName();
			}
			
			String frag = deviceField.getElementName() + ".";
			
			String addition = null;
			
			for(FRCRElement cur : children){
				if(cur instanceof FRCRParent){
					if((addition = 
						((FRCRParent)cur).getCapabilityCodeFragment(cap)) != null){
						if((deviceType != null)
								&&ModelBuilderUtil.isSameCompilationUnit(
										deviceType.getCompilationUnit(),
										FRCModel.getActiveJavaFile())){
							return addition;
						}
						
						return frag + addition;
					}
				}
			}

		}
		
		return null;
	}
	
	@Override
	public boolean isInstanceOfSame(FRCRParent cadidate){
		if(cadidate instanceof FRCRDevice){
			FRCRDevice devCand = (FRCRDevice)cadidate;
			
			if((deviceType == null)||(devCand.deviceType == null)){
				return false;
			}
			
			return deviceType.equals(devCand.deviceType);
		}
		
		return false;
	}
	
	@Override
	public boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled){
		//Visible if:  unit is its or its type's compilation unit, field is public
		//and its parent is enabled, or field is public and static
		if(compUnit != null){
			if((deviceField != null)&&(deviceField.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit,
						deviceField.getCompilationUnit())){
					return true;
				}
			}
			
			if((deviceType != null)&&(deviceType.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit,
						deviceType.getCompilationUnit())){
					return true;
				}
			}
		}
		
		if(deviceField != null){
			try{
				int flags = deviceField.getFlags();
				
				if(Flags.isPublic(flags)){
					return parentEnabled || Flags.isStatic(flags);
				}
			}catch(Exception e){return false;}
		}
		return false;
	}
	
	@Override
	public String getElementName(){
		String name = "{D:";
		
		if(deviceType != null){
			name += deviceType.getElementName() + ":";
		}else{
			name += "NULLTYPE:";
		}
		
		if(deviceField != null){
			name += deviceField.getElementName() + "}";
		}else{
			name += "NULLFIELD}";
		}
		
		return name;
			
	}
	
	@Override
	public String getDisplayName(){
		String name = "";
		
		if(deviceField != null){name = deviceField.getElementName();}
		
		if(deviceType != null){name += " : " + deviceType.getElementName();}
		
		return name;
	}
	
	@Override
	public boolean canModify(){
		if((deviceField != null)&&(deviceField.getCompilationUnit() != null)){
			return true;
		}
		
		return false;
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCRDEVICE;
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		if((deviceField == null)||(!deviceField.exists())){
			((FRCElement)getParent()).rebuild();
		}else{
			reconcile(new FRCRDevice(deviceField));
		}
		
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
		if((deviceType == null)&&(deviceField == null)){return false;}
		
		if(deviceType != null){
			if(element.equals(deviceType)){return true;}
		}
		
		if(deviceField != null){
			return element.equals(deviceField);
		}
		
		return false;
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
						if(deviceField != null){
							//Use field, since children are still processed, 
							//and keeps proper associations
							FRCRDevice recDev = new FRCRDevice(deviceField);
							reconcile(recDev);
							return;
						}
					}
				}
			}
			
			super.runUpdate(element, node);
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			if(super.runReconcile(updateTo)){
				FRCRDevice dev = (FRCRDevice)updateTo;
				deviceField = null;
				deviceType = null;
				deviceField = dev.deviceField;
				deviceType = dev.deviceType;
				
				notifyListeners(new FRCModelEvent(this, 
						FRCModelEvent.FT_DATA_CHANGE,
						FRCModelEvent.FK_RECONCILE,
						getElementName() + " reconciled"));
			}else{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((deviceField != null)&&(deviceField.getClassFile() == null)){
			try {
				JavaUI.openInEditor(deviceField);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		if(deviceField != null){return deviceField.getElementName();}
		
		return null;
	}
	
	@Override
	public FRCVParent getViewModel(){
		//TODO
		ArrayList<AddViewModelElement> threads = new ArrayList<AddViewModelElement>();
		FRCVParent devDisplay = new FRCVParent(this);
		
		FRCRElement[] devs = getDevices();
		FRCRCapability[] caps = getCapabilities();
		
		if(devs.length > 0){
			FRCVParent devRoot = 
				new FRCVParent("Devices", ModelElementType.FRCRDEVICE);
			
			for(FRCRElement currentDev : devs){
				threads.add(new AddViewModelElement(currentDev));
			}
			
			for(Thread current : threads){current.start();}
			
			for(AddViewModelElement current : threads){
				try{
					devRoot.add(current.joinAndReturn());
				}catch(Exception e){}
			}
			
			devDisplay.add(devRoot);
			threads.clear();
		}
		
		if(caps.length > 0){
			FRCVParent capRoot = 
				new FRCVParent("Capabilities", ModelElementType.FRCRCAPABILITY);
			
			for(FRCRCapability currentCap : caps){
				//capRoot.add(currentCap.getViewModel());
				threads.add(new AddViewModelElement(currentCap));
			}
			
			for(Thread current : threads){current.start();}
			
			for(AddViewModelElement current : threads){
				try{
					capRoot.add(current.joinAndReturn());
				}catch(Exception e){}
			}
			
			devDisplay.add(capRoot);
			threads.clear();
		}
		
		FRCRElement sm = getStateMachine();
		
		if(sm != null){
			threads.add(new AddViewModelElement(sm));
			
			for(Thread current : threads){current.start();}
			
			for(AddViewModelElement current : threads){
				try{
					devDisplay.add(current.joinAndReturn());
				}catch(Exception e){}
			}
			
			threads.clear();
			//devDisplay.add(sm.getViewModel());
		}
		
		return devDisplay;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		deviceField = null;
		deviceType = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if((super.equals(obj))&&(obj instanceof FRCRDevice)){
			FRCRDevice dev = (FRCRDevice)obj;
			boolean retValF = false;
			boolean retValT = false;
			
			if((dev.deviceField == null)&&(deviceField == null)){
				retValF = true;
			}else if((dev.deviceField != null)&&(deviceField != null)){
				if(dev.deviceField.equals(deviceField)){
					retValF = true;
				}
			}
			
			
			if((dev.deviceType == null)&&(deviceType == null)){
				retValT = true;
			}else if((dev.deviceType != null)&&(deviceType != null)){
				if(dev.deviceType.equals(deviceType)){
					retValT = true;
				}
			}
			
			return retValT && retValF;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(deviceType != null){hash += deviceType.hashCode();}
		if(deviceField != null){hash += deviceField.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCRDevice safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCRDevice
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(!(candidate instanceof IField)){return false;}
		
		IType typeToTest = ModelBuilderUtil.createIFieldType((IField)candidate);
		
		return meetsDevTypeRequirements(typeToTest);
	}
	
	/**
	 * Determines if the given IType is a class which can be considered a 
	 * device
	 * @param candidate The IType to test against the device definition
	 * @return true if the IType is a device, false otherwise
	 */
	protected static boolean meetsDevTypeRequirements(IType candidate){
		if(candidate == null){return false;}
		
		try{
			String[] interfaces = candidate.getSuperInterfaceTypeSignatures();
			
			for(String curInterface : interfaces){
				IType testing = ModelBuilderUtil.getJavaElement(candidate, 
						curInterface);
				if((testing != null)&&(testing.getFullyQualifiedName()
						.equalsIgnoreCase(ParseConstants.DEVICE))){return true;}
				if(ModelBuilderUtil.isInterface(testing, 
						ParseConstants.DEVICE)){return true;}
			}
		}catch(Exception e){return false;}
		
		return false;
	}
}
