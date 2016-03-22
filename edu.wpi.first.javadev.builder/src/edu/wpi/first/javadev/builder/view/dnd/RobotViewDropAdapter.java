package edu.wpi.first.javadev.builder.view.dnd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import edu.wpi.first.codedev.output.FRCDialog;
import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.view.views.RoboCentricView;
import edu.wpi.first.javadev.builder.workspace.model.library.FRCLElement;

/** 
 * Enables a display tree to support drop operations on it
 * 
 * @author Ryan O'Meara
 */
public class RobotViewDropAdapter extends ViewerDropAdapter{
	RoboCentricView robotView;
	
	public RobotViewDropAdapter(TreeViewer viewer, RoboCentricView roboView) {
		super(viewer);
		robotView = roboView;
	}

	@Override
	public boolean performDrop(Object data) {
		//get string from the data
		if((data == null)||(!(data instanceof String[]))){return false;}

		String[] ids = (String[])data;
		
		//find the associated palette model object
		FRCLElement[] elements = new FRCLElement[ids.length];
		
		for(int i = 0; i < ids.length; i++){
			elements[i] = (FRCLElement)CodeViewerPlugin.getFRCModel().findElement(ids[i]);
		}
		
		boolean retVal = robotView.handleDrop(elements, getCurrentTarget());
		
		if(!retVal){
			FRCDialog.createErrorDialog("Drag and Drop Error", 
					new Status(IStatus.ERROR,
							CodeViewerPlugin.PLUGIN_ID,
							"handleDrop returned false"));
		}
		
		return retVal;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return PaletteToRobotTransfer.getInstance().isSupportedType(transferType);
	}

}
