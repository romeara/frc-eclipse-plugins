package edu.wpi.first.javadev.builder.workspace.model.robot;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.JavaUI;

import edu.wpi.first.javadev.builder.util.CircularRefNode;
import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Represents a mechanism in the robot model. Can contain capabilities,
 * devices, and mechanisms
 * 
 * @author Ryan O'Meara
 */
public class FRCRMechanism extends FRCRDevice {
	/**
	 * Runnable to add new elements to the mechanism during construction.  Allows
	 * multi-threading the construction process
	 * 
	 * @author Ryan O'Meara
	 */
	protected class AddElement implements Runnable{
		private IJavaElement addition;
		
		protected AddElement(IJavaElement add){addition = add;}
		
		@Override
		public void run(){
			FRCRElement curREl;
			if((curREl = FRCRobot.createFRCRElement(addition)) != null){
				add(curREl);
			}
		}
	}
	
	protected FRCRMechanism(){super();}
	
	protected FRCRMechanism(IField mechanismDeclaration){
		super();
		deviceField = mechanismDeclaration;
		deviceType = ModelBuilderUtil.createIFieldType(deviceField);
		
		if(deviceType != null){
			CircularRefNode node = FRCModel.getReferenceNode(deviceField, 
					new IJavaElement[]{deviceType});
			constructFromIType(deviceType, node);
		}
	}
	
	@Override
	protected void constructFromIType(IType fieldType, CircularRefNode thisMech){
		ArrayList<Thread> threads = new ArrayList<Thread>();
		disableEventPassing();
		try{
			IJavaElement[] javaChildren = fieldType.getChildren();
			boolean circularRefDev = false;
			boolean circularRefMech = false;
			
			
			for(IJavaElement currentElement : javaChildren){
				circularRefDev = false;
				circularRefMech = false;
				
				if(FRCRMechanism.meetsRequirements(currentElement)){
					FRCModel.createCircularReferenceNode(currentElement, 
							new IJavaElement[]{ModelBuilderUtil.createIFieldType(
									(IField)currentElement)}, 
							thisMech);
					circularRefMech = 
						circularReference((IField)currentElement, thisMech);
					if(circularRefMech){
						add(new FRCRMechanismPlaceholder((IField)currentElement));
						continue;
					}
				}else if(FRCRDevice.meetsRequirements(currentElement)){
					FRCModel.createCircularReferenceNode(currentElement, 
							new IJavaElement[]{ModelBuilderUtil.createIFieldType(
									(IField)currentElement)}, 
							thisMech);
					circularRefDev = 
						circularReference((IField)currentElement, thisMech);
					if(circularRefDev){
						add(new FRCRDevicePlaceholder((IField)currentElement));
						continue;
					}
				}
				
				
				
				if(currentElement instanceof IMethod){
					if(!methodUsable((IMethod)currentElement)){continue;}
				}
				
				if(!(circularRefDev||circularRefMech)){
					threads.add(new Thread(new AddElement(currentElement)));
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
	 * @return An array of FRCRMechanisms contained in this mechanism
	 */
	public FRCRElement[] getMechanisms(){
		ArrayList<FRCRElement> mechs = new ArrayList<FRCRElement>();
		
		for(FRCRElement child : children){
			if(child.getElementType().equals(ModelElementType.FRCRMECHANISM)){
				mechs.add(child);
			}
		}
		
		return mechs.toArray(new FRCRElement[mechs.size()]);
	}
	
	@Override
	public boolean addField(String visibility, String name, IType fieldClass){
		if(!meetsMechTypeRequirements(fieldClass)){
			return super.addField(visibility, name, fieldClass);
		}
		
		disableOutsideUpdate();
		IField newField = addDeviceField(visibility, name, fieldClass);
		
		if(newField == null){
			enableOutsideUpdate();
			return false;
		}
		
		add(FRCRobot.createFRCRMechanism(newField));
		
		enableOutsideUpdate();
		
		
		return false;
	}

	@Override
	public String getElementName(){
		String name = "{M:";
		
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
	public ModelElementType getElementType(){
		return ModelElementType.FRCRMECHANISM;
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
							FRCRMechanism recMech = new FRCRMechanism(deviceField);
							reconcile(recMech);
							return;
						}
					}
				}
			}
			
			super.runUpdate(element, node);
		}
	}
	
	@Override
	public boolean openInEditor(){
		try {
			JavaUI.openInEditor(deviceType);
			return true;
		} catch (Exception e) {return false;}
	}
	
	@Override
	public FRCVParent getViewModel(){
		ArrayList<AddViewModelElement> threads = new ArrayList<AddViewModelElement>();
		FRCVParent mechDisplay = super.getViewModel();
		
		FRCRElement[] mechs = getMechanisms();
		
		if(mechs.length > 0){
			FRCVParent mechRoot = 
				new FRCVParent("Mechanisms", ModelElementType.FRCRMECHANISM);
			
			for(FRCRElement currentMech : mechs){
				threads.add(new AddViewModelElement(currentMech));
			}
			
			for(Thread current : threads){current.start();}
			
			for(AddViewModelElement current : threads){
				try{
					mechRoot.add(current.joinAndReturn());
				}catch(Exception e){}
			}
			
			mechDisplay.add(mechRoot);
		}
		
		return mechDisplay;
	}
	
	@Override
	public boolean equals(Object obj){
		if(super.equals(obj)&&(obj instanceof FRCRMechanism)){return true;}
		
		return false;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCRMechanism safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCRMechanism
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(!(candidate instanceof IField)){return false;}
		
		IType typeToTest = ModelBuilderUtil.createIFieldType((IField)candidate);
		
		if(typeToTest == null){return false;}
		
		try{
			String[] interfaces = typeToTest.getSuperInterfaceTypeSignatures();
			
			for(String curInterface : interfaces){
				IType testing = ModelBuilderUtil.getJavaElement(typeToTest, 
						curInterface);
				if((testing != null)&&(testing.getFullyQualifiedName()
						.equalsIgnoreCase(ParseConstants.MECHANISM))){return true;}
				if(ModelBuilderUtil.isInterface(testing, 
						ParseConstants.MECHANISM)){return true;}
			}
		}catch(Exception e){return false;}
		
		return false;
	}
	
	/**
	 * Determines if the given IType is a class which can be considered a 
	 * mechanism
	 * @param candidate The IType to test against the mechanism definition
	 * @return true if the IType is a device, false otherwise
	 */
	protected static boolean meetsMechTypeRequirements(IType candidate){
		if(candidate == null){return false;}
		
		try{
			String[] interfaces = candidate.getSuperInterfaceTypeSignatures();
			
			for(String curInterface : interfaces){
				IType testing = ModelBuilderUtil.getJavaElement(candidate, 
						curInterface);
				if((testing != null)&&(testing.getFullyQualifiedName()
						.equalsIgnoreCase(ParseConstants.MECHANISM))){return true;}
				if(ModelBuilderUtil.isInterface(testing, 
						ParseConstants.MECHANISM)){return true;}
			}
		}catch(Exception e){return false;}
		
		return false;
	}
}
