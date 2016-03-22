package edu.wpi.first.javadev.builder.workspace.model;

/**
 * Represents details about a particular type of code model object.  This includes
 * ordering, category naming, and which icon to use
 * 
 * @author Ryan O'Meara
 */
public class ModelElementType {
	protected String name;  //What would be displayed in a "container" parent above a set of that type
	protected String iconPath;
	protected String addIconPath;
	protected String disabledIconPath;
	protected int order;
	
	public static final String DEFAULTOBJECTICONPATH = "Default.png";
	public static final String DEFAULTPARENTICONPATH = "Root.png";
	public static final int LASTORDER = 100;
	
	/* Pre-defined types */
	public static final ModelElementType FRCREVENT = new ModelElementType("Events", "Event.png", "Event-Add.png", "Event-Disabled.png", 15);
	public static final ModelElementType FRCRSTATE = new ModelElementType("States", "State.png", "State-Add.png", "State-Disabled.png", 16);
	public static final ModelElementType FRCRSTATEMACHINE = new ModelElementType("State Machines", "StateMachine.png", "StateMachine-Add.png", "StateMachine-Disabled.png", 14);
	public static final ModelElementType FRCRCAPABILITY = new ModelElementType("Capabilities", "Capability.png", "Capability-Add.png", "Capability-Disabled.png", 13);
	public static final ModelElementType FRCRDEVICE = new ModelElementType("Devices", "Device.png", "Device-Add.png", "Device-Disabled.png", 12);
	public static final ModelElementType FRCRTRANSITION = new ModelElementType("Transitions", "Transition.png", "Transition-Add.png", "Transition-Disabled.png", 17);
	public static final ModelElementType FRCRMECHANISM = new ModelElementType("Mechanisms", "Mechanism.png", "Mechanism-Add.png", "Mechanism-Disabled.png", 11);
	public static final ModelElementType FRCROBOT = new ModelElementType("Robot", "firsticon.png", 10);
	
	public static final ModelElementType FRCLDEVICE = new ModelElementType("Devices", "Device.png", 11);
	public static final ModelElementType FRCLVARIABLE = new ModelElementType("Fields", "Field.png", 12);
	public static final ModelElementType FRCLMETHOD = new ModelElementType("Methods", "Capability.png", 13);
	public static final ModelElementType FRCLIBRARY = new ModelElementType("Roots", "Root.png", 10);
	
	public static final ModelElementType LASTORDERTYPE = new ModelElementType("LASTORDER", "Default.png", LASTORDER);
	
	public ModelElementType(String typeName, String typeIconPath, int typeOrder){
		this(typeName, typeIconPath, typeIconPath, typeIconPath, typeOrder);
	}
	
	public ModelElementType(String typeName, String typeIconPath, String typeAddIconPath, String typeDisabledIconPath, int typeOrder){
		name = typeName;
		order = typeOrder;
		iconPath = typeIconPath;
		addIconPath = typeAddIconPath;
		disabledIconPath = typeDisabledIconPath;
		
		if(iconPath == null){iconPath = DEFAULTOBJECTICONPATH;}
		if(name == null){name = "";}
	}
	
	public static ModelElementType getParentType(String name, int order){
		return new ModelElementType(name, "Root.png", "Root.png", 
				"Root-Disabled.png", order);
	}
	
	/** Returns the numeric ordering for a view - lower numbers are higher in
	 * the view */
	public int getOrder(){
		return order;
	}
	
	/** Get the name that should be displayed as a label for a group of these objects */
	public String getTypeName(){
		return name;
	}
	
	/** Get the path to the icon to use relative to the icons folder in the plug-in */
	public String getIconPath(){
		return iconPath;
	}
	
	/** Get the path to the icon to use relative to the icons folder in the plug-in */
	public String getAddIconPath(){
		return addIconPath;
	}
	
	/** Get the path to the icon to use relative to the icons folder in the plug-in */
	public String getDisabledIconPath(){
		return disabledIconPath;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof ModelElementType){
			ModelElementType comp = (ModelElementType)obj;
			
			if((name.equals(comp.getTypeName()))&&(order == comp.getOrder())){
				if(iconPath.equals(comp.getIconPath())){
					if(addIconPath.equals(comp.getAddIconPath())){
						if(disabledIconPath.equals(comp.getDisabledIconPath())){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public int hashCode(){
		return order + name.hashCode() + iconPath.hashCode() 
		+ addIconPath.hashCode() + disabledIconPath.hashCode();
	}
	
}
