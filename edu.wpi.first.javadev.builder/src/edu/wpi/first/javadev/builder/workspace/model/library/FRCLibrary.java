package edu.wpi.first.javadev.builder.workspace.model.library;

import java.util.ArrayList;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;
import edu.wpi.first.javadev.projects.nature.FRCProjectNature;

/**
 * Represents a library linked to and FRC Project.  Contains devices
 * 
 * @author Ryan O'Meara
 */
public class FRCLibrary extends FRCLParent {
	/**
	 * Class to add devices to the library in a multi-threaded way
	 * 
	 * @author Ryan O'Meara
	 */
	protected class AddLibraryDevices implements Runnable{
		private IType devType;
		
		protected AddLibraryDevices(IType constructFrom){
			devType = constructFrom;
		}
		
		@Override
		public void run(){
			FRCLDevice dev;
			if((dev = createFRCLDevice(devType)) != null){
				add(dev);
			}
		}
	}
	
	
	protected IJavaProject libraryProject;
	protected ArrayList<IType> dynamicTypes;
	
	public FRCLibrary(IJavaProject robotProject){
		super();
		libraryProject = robotProject;
		dynamicTypes = new ArrayList<IType>();
		
		disableEventPassing();
		IType[] allTypes = getProjectTypes(libraryProject);
		if(allTypes != null){
			processDynamicTypes(allTypes);
			constructLibrary(allTypes);
		}
		
		enableEventPassing();
	}
	
	/**
	 * Processes all interfaces which extend iDevice for view model use
	 * @param allLibraryTypes
	 */
	protected void processDynamicTypes(IType[] allLibraryTypes){
		dynamicTypes = new ArrayList<IType>();
		//Constructs all types of elements
		for(IType current : allLibraryTypes){
			try{
				if(current.isInterface()){
					if(!current.getFullyQualifiedName().equalsIgnoreCase(ParseConstants.DEVICE)){
						if(ModelBuilderUtil.isInterface(current, ParseConstants.DEVICE)){
							dynamicTypes.add(current);
						}
					}
				}
			}catch(Exception e){}
		}
		
		
	}
	
	/**
	 * Creates the library by adding all devices
	 * @param allLibraryTypes
	 */
	protected void constructLibrary(IType[] allLibraryTypes){
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for(IType current : allLibraryTypes){
			threads.add(new Thread(new AddLibraryDevices(current)));
		}
		
		for(Thread current : threads){current.start();}
		
		for(Thread current : threads){try{current.join();}catch(Exception e){}}
	}
	
	/** Returns all the types contained in a project, including referenced libraries
	 * Filters WPILibJ to not include the squawk library, and not double count classes
	 * because of the jme2classes folder */
	protected static IType[] getProjectTypes(IJavaProject project){
		ArrayList<IType> types = new ArrayList<IType>();
		
		try {
			if(project == null){return null;}
			for (IPackageFragmentRoot current : project.getPackageFragmentRoots()) {
				//Optimize scanning, skip jar known to not be used
				if(!current.getElementName().equals("squawk_device.jar")){
					ICompilationUnit[] cus = ModelBuilderUtil.findCompilationUnits(current);
					IClassFile[] cfs = ModelBuilderUtil.findClassFiles(current);
					if(cus != null){
						for (ICompilationUnit currentcu : cus) {
								types.add(currentcu.findPrimaryType());
						}
					}
					
					if(cfs != null){
						for (IClassFile currentcf : cfs) {
							//Prevent doubling
							if(currentcf.findPrimaryType() != null){
								if(currentcf.findPrimaryType().getFullyQualifiedName().indexOf("j2meclasses") == -1){
									types.add(currentcf.findPrimaryType());
								}
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return types.toArray(new IType[types.size()]);
	}
	
	@Override
	public FRCLibrary getLibrary(){return this;}
	
	/**
	 * @return Array of all the library devices in this library
	 */
	public FRCLElement[] getDevices(){
		ArrayList<FRCLElement> devs = new ArrayList<FRCLElement>();
		
		for(FRCLElement child : children){
			if(child.getElementType().equals(ModelElementType.FRCLDEVICE)){
				devs.add((FRCLElement)child);
			}
		}
		
		return devs.toArray(new FRCLElement[devs.size()]);
	}
	
	@Override
	public String getElementName(){
		if(libraryProject != null){
			return "{LIBRARY:" + libraryProject.getElementName() + "}";
		}
		
		return "{LIBRARY:NULLPROJECT}";
	}
	
	@Override
	public String getDisplayName(){
		if(libraryProject != null){
			return libraryProject.getElementName() + " Library";
		}
		
		return "NULLPROJECT Library";
	}
	
	@Override
	public boolean canModify(){return true;}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCLIBRARY;
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		reconcile(new FRCLibrary(getFRCProject().getJavaProject()));
		
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
		//The project is the only thing defined by a java project; this
		//library is not itself defined by a java element
		return false;
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		if((libraryProject == null)||(!libraryProject.exists())){
			rebuild();
			return;
		}
		
		if(!(element instanceof ICompilationUnit)){
			reconcile(new FRCLibrary(libraryProject));
			return;
		}
		
		for(FRCLElement child : children){child.update(element, node);}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			disableEventPassing();
			clearEventLog();
			enableEventLogging();
			
			if(super.runReconcile(updateTo)){
				FRCLibrary lib = (FRCLibrary)updateTo;
				ArrayList<IType> dTHolder = new ArrayList<IType>();
				if(dynamicTypes != null){dTHolder.addAll(dynamicTypes);}
				libraryProject = null;
				libraryProject = lib.libraryProject;
				dynamicTypes.clear();
				
				for(IType current : lib.dynamicTypes){
					dynamicTypes.add(current);
				}
				
				disableEventLogging();
				
				ArrayList<FRCModelEvent> childEvents = new ArrayList<FRCModelEvent>();
				
				for(FRCModelEvent current : loggedEvents){
					if(current.getKind() == FRCModelEvent.FK_RECONCILE){
						if(current.getChildEvents() != null){
							for(FRCModelEvent recChild : current.getChildEvents()){
								childEvents.add(recChild);
							}
						}
					}else{
						childEvents.add(current);
					}
				}
				
				
				
				FRCModelEvent reconcileEvent = new FRCModelEvent(this, 
						FRCModelEvent.FT_DATA_CHANGE,
						FRCModelEvent.FK_RECONCILE,
						getElementName() + " reconciled",
						childEvents.toArray(new FRCModelEvent[childEvents.size()]));
				
				enableEventPassing();
				
				notifyListeners(reconcileEvent);
				
				clearEventLog();
			}else{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){return false;}
	
	@Override
	public String getCodeFragment(){return null;}
	
	@Override
	public FRCVParent getViewModel(){
		FRCVParent rootVP = new FRCVParent(this);
		
		//Create arraylist of all devices
		ArrayList<FRCLElement> allDevs = new ArrayList<FRCLElement>();
		
		for(FRCLElement current : getDevices()){
			allDevs.add(current);
		}
		
		for(IType current : dynamicTypes){
			FRCVParent temp = new FRCVParent(current.getElementName().substring(1),
					new ModelElementType(current.getElementName().substring(1) + "s",
							ModelElementType.FRCLDEVICE.getIconPath(), 
							ModelElementType.FRCLDEVICE.getAddIconPath(), 
							ModelElementType.FRCLDEVICE.getDisabledIconPath(), 
							ModelElementType.FRCLDEVICE.getOrder()-1));
			ArrayList<FRCLElement> rem = new ArrayList<FRCLElement>();
			for(FRCLElement curDev : allDevs){
				//parse each type
				if((curDev instanceof FRCLDevice)
						&&(ModelBuilderUtil.isInterface(
								((FRCLDevice)curDev).getType(), 
								current.getFullyQualifiedName()))){
					//remove from overall array list as "sorted"
					//Cannot be multi-threaded currently as there is a runnable lock
					rem.add(curDev);
					temp.add(curDev.getViewModel());
				}
			
			}
			
			rootVP.add(temp);
			//remove devices that have been sorted
			for(FRCLElement cur : rem){
				allDevs.remove(cur);
			}
		}
		
		FRCVParent temp = new FRCVParent("Devices", 
				ModelElementType.FRCLDEVICE);
		
		
		for(FRCLElement remaining : allDevs){
			temp.add(remaining.getViewModel());
		}
		
		rootVP.add(temp);
		
		return rootVP;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		libraryProject = null;
		dynamicTypes.clear();
	}
	
	@Override
	public boolean equals(Object obj){
		if(super.equals(obj)&&(obj instanceof FRCLibrary)){
			boolean libraryEq = false;
			boolean dynamicEq = true;
			
			FRCLibrary lib = (FRCLibrary)obj;
			
			if((libraryProject == null)&&(lib.libraryProject == null)){
				libraryEq = true;
			}
			
			if((libraryProject != null)&&(lib.libraryProject != null)){
				libraryEq = libraryProject.equals(lib.libraryProject);
			}
			
			if((dynamicTypes == null)||(lib.dynamicTypes == null)){
				if(!((dynamicTypes == null)&&(lib.dynamicTypes == null))){
					dynamicEq = false;
				}
			}else if(dynamicTypes.size() != lib.dynamicTypes.size()){
				dynamicEq = false;
			}else{
				for(IType type : dynamicTypes){
					if(!lib.dynamicTypes.contains(type)){
						dynamicEq = false;
						break;
					}
				}
			}
			
			return dynamicEq && libraryEq;	
		}
		
		return false;
	}
	
	@Override
	public void receiveEvent(FRCModelEvent event){
		if((event.getType() == FRCModelEvent.FT_ADDED_NEW_ELEMENT)
				||(event.getType() == FRCModelEvent.FT_REMOVED_ELEMENT)){
			reconcile(new FRCLibrary(libraryProject));
		}else{
			super.receiveEvent(event);
		}
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(libraryProject != null){hash = libraryProject.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCLibrary safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCLibrary
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
	 * Creates the best fit element for the given IJavaElement.
	 * @param element The element to construct from
	 * @return Corresponding FRCLElement, or null if there is no
	 * valid corresponding element
	 */
	public static FRCLElement createFRCLElement(IJavaElement element){
		if(element instanceof IType){
			return createFRCLDevice((IType)element);
		}else if(element instanceof IMethod){
			return createFRCLMethod((IMethod)element);
		}else if(element instanceof IField){
			return createFRCLVariable((IField)element);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCLDevice from the given IType if possible.  The
	 * IType must implement the proper device interface
	 * @param deviceType The IType to build from
	 * @return The created FRCLDevice, or null if the device could not be
	 * created
	 */
	public static FRCLDevice createFRCLDevice(IType deviceType){
		if(FRCLDevice.meetsRequirements(deviceType)){
			return new FRCLDevice(deviceType);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCLMethod from the given IMethod if possible.  The
	 * IMethod must have the proper modifiers
	 * @param method The IMethod to build from
	 * @return The created FRCLMethod, or null if the method could not be
	 * created
	 */
	public static FRCLMethod createFRCLMethod(IMethod method){
		if(FRCLMethod.meetsRequirements(method)){
			return new FRCLMethod(method);
		}
		
		return null;
	}
	
	/**
	 * Creates an FRCLVariable from the given IField if possible.  The
	 * IField must have the proper modifiers
	 * @param variableField The IField to build from
	 * @return The created FRCLVariable, or null if the variable could not be
	 * created
	 */
	public static FRCLVariable createFRCLVariable(IField variableField){
		if(FRCLVariable.meetsRequirements(variableField)){
			return new FRCLVariable(variableField);
		}
		
		return null;
	}
	
}
