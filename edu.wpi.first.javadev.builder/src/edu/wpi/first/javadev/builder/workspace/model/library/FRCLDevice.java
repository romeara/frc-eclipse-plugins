package edu.wpi.first.javadev.builder.workspace.model.library;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Represents a device in the library.  Cna contain devices, methods, and
 * variables
 * 
 * @author Ryan O'Meara
 */
public class FRCLDevice extends FRCLParent {
	protected IType deviceType;
	
	protected FRCLDevice(IType inputType){
		super();
		deviceType = inputType;
		
		if(deviceType != null){constructFromIType(deviceType);}
	}
	
	/**
	 * Performs constructing operations using an IType created from the input
	 * IField
	 * @param fieldType The type associated with the input IField
	 */
	protected void constructFromIType(IType fieldType){
		disableEventPassing();
		try{
			IJavaElement[] javaChildren = deviceType.getChildren();
			
			for(IJavaElement currentElement : javaChildren){
				if(FRCLDevice.meetsRequirements(currentElement)){
					FRCLParent circularRef = 
						circularReference((IField)currentElement);
					if(circularRef != null){
						add(new FRCLPlaceHolder(circularRef));
						continue;
					}
				}
				
				FRCLElement curLEl;
				
				if((curLEl = FRCLibrary.createFRCLElement(currentElement)) != null){
					add(curLEl);
				}
			}
			
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
	protected FRCLParent circularReference(IField test){
		FRCLParent currentParent = this;
		IType fieldType = ModelBuilderUtil.createIFieldType(test);
		
		while(!(currentParent instanceof FRCLibrary)){
			if(currentParent instanceof FRCLDevice){
				if(((FRCLDevice)currentParent).references(fieldType)){
					return currentParent;
				}
			}
			currentParent = (FRCLParent)currentParent.getParent();
			if(currentParent == null){return null;}
		}
		
		return null;
	}
	
	/**
	 * Determines if this element reference the given type
	 * @param ref The type to test against
	 * @return true if it is referenced, false otherwise
	 */
	public boolean references(IType ref){
		if((deviceType != null) && (deviceType.equals(ref))){return true;}
		
		return false;
	}	
	
	/**
	 * @return An array of the devices contained within this device.  Could
	 * include place holders to prevent circular references
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
	
	/**
	 * @return An array of the library methods in this device
	 */
	public FRCLMethod[] getMethods(){
		ArrayList<FRCLMethod> methods = new ArrayList<FRCLMethod>();
		
		for(FRCLElement child : children){
			if(child instanceof FRCLMethod){
				methods.add((FRCLMethod)child);
			}
		}
		
		return methods.toArray(new FRCLMethod[methods.size()]);
	}
	
	/**
	 * @return An array of the library variables within this device
	 */
	public FRCLVariable[] getVariables(){
		ArrayList<FRCLVariable> caps = new ArrayList<FRCLVariable>();
		
		for(FRCLElement child : children){
			if(child instanceof FRCLVariable){
				caps.add((FRCLVariable)child);
			}
		}
		
		return caps.toArray(new FRCLVariable[caps.size()]);
	}
	
	@Override
	public IType getType(){return deviceType;}
	
	@Override
	public String getElementName(){
		if(deviceType != null){
			return "{D:" + deviceType.getElementName() + "}";
		}
		
		return "{D:NULLDEVICE}";
	}
	
	@Override
	public String getDisplayName(){
		if(deviceType != null){return deviceType.getElementName();}
		
		return null;
	}
	
	@Override
	public boolean canModify(){
		return ((deviceType != null)&&(deviceType.getCompilationUnit() != null));
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCLDEVICE;
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		if((deviceType != null)&&(deviceType.exists())){
			reconcile(new FRCLDevice(deviceType));
		}else{
			((FRCElement)getParent()).rebuild();
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
		if(deviceType == null){return false;}
		
		return element.equals(deviceType);
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
							FRCLDevice recDev = new FRCLDevice(deviceType);
							reconcile(recDev);
							return;
					}
				}
			}
			
			super.runUpdate(element, node);
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCLDevice dev = (FRCLDevice)updateTo;
			IType typeHolder = deviceType;
			deviceType = null;
			deviceType = dev.deviceType;
			
			if(!super.runReconcile(updateTo)){
				deviceType = null;
				deviceType = typeHolder;
				return false;
			}
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((deviceType != null)&&(deviceType.getClassFile() == null)){
			try {
				JavaUI.openInEditor(deviceType);
				return true;
			} catch (Exception e) {return false;}
			}
		return false;
	}
	
	@Override
	public String getCodeFragment(){
		if(deviceType != null){
			return "protected " + deviceType.getElementName() + " " +
				deviceType.getElementName().toLowerCase() + ";";
		}
		
		return null;
	}
	
	@Override
	public FRCVParent getViewModel(){
		FRCVParent devDisplay = new FRCVParent(this);
		
		FRCLElement[] devs = getDevices();
		FRCLMethod[] methods = getMethods();
		FRCLVariable[] vars = getVariables();
		
		if(devs.length > 0){
			FRCVParent devRoot = 
				new FRCVParent("Devices", ModelElementType.FRCLDEVICE);
			
			for(FRCLElement currentDev : devs){
				devRoot.add(currentDev.getViewModel());
			}
			
			devDisplay.add(devRoot);
		}
		
		if(methods.length > 0){
			FRCVParent methRoot = 
				new FRCVParent("Public Static Methods", ModelElementType.FRCLMETHOD);
			
			for(FRCLMethod currentCap : methods){
				methRoot.add(currentCap.getViewModel());
			}
			
			devDisplay.add(methRoot);
		}
		
		if(vars.length > 0){
			FRCVParent varRoot = 
				new FRCVParent("Public Static Final Fields", ModelElementType.FRCLVARIABLE);
			
			for(FRCLVariable currentVar : vars){
				varRoot.add(currentVar.getViewModel());
			}
			
			devDisplay.add(varRoot);
		}
		
		return devDisplay;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		deviceType = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if((obj instanceof FRCLDevice)&&super.equals(obj)){
			FRCLDevice dev = (FRCLDevice)obj;
			
			if((deviceType == null)&&(dev.deviceType == null)){
				return true;
			}
			
			if((deviceType != null)&&(dev.deviceType != null)){
				return deviceType.equals(dev.deviceType);
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 0;
		
		if(deviceType != null){hash += deviceType.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCLDevice safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCLDevice
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IType){
			IType typeToTest = (IType)candidate;
			
			try{
				if(typeToTest.isInterface()){return false;}
				
				String[] interfaces = typeToTest.getSuperInterfaceTypeSignatures();
				
				for(String curInterface : interfaces){
					IType testing = ModelBuilderUtil.getJavaElement(typeToTest, 
							curInterface);
					if((testing != null)&&(testing.getFullyQualifiedName()
							.equalsIgnoreCase(ParseConstants.DEVICE))){return true;}
					if(ModelBuilderUtil.isInterface(testing, 
							ParseConstants.DEVICE)){return true;}
				}
			}catch(Exception e){return false;}
		}
		
		return false;
	}
	
}
