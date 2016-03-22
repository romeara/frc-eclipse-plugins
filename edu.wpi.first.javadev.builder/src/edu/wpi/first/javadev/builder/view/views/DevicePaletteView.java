package edu.wpi.first.javadev.builder.view.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.view.dnd.PaletteToRobotTransfer;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCView;

/**
 * Represents the device palette view in the eclipse workspace
 * 
 * @author Ryan O'Meara
 */
public class DevicePaletteView extends BaseCodeView {

	public static final String ID = "edu.wpi.first.javadev.builder.devicePaletteView";
	
	protected FRCView treeMod;
	
	public DevicePaletteView(){
		super();
		treeMod = CodeViewerPlugin.getFRCModel().getPaletteViewModel();
	}
	
	
	@Override
	protected void addAdditionalDNDSupport() {}

	@Override
	protected void addToolbarButtons() {
		// TODO Device Palette: implement addToolbarButtons
	}

	@Override
	protected void createActions() {
		// TODO Device Palette: implement createActions
	}

	@Override
	protected void createFilters() {
		// TODO Device Palette: implement createFilters
	}

	@Override
	protected void fillContextMenuElements(IMenuManager manager) {
		// TODO Device Palette: implement fillContextMenuElements
	}

	@Override
	protected void fillFilterMenu(IMenuManager rootMenuManager) {
		// TODO Device Palette: implement fillFilterMenu
	}

	@Override
	protected FRCVParent getDisplayTreeRoot() {
		return treeMod;
	}

	@Override
	protected void updateFilters(Action action) {
		// TODO Device Palette: implement updateFilters
	}

	@Override
	protected int getInitialLevel() {
		return 3;
	}
	
	@Override
	protected Transfer[] getTransferTypes() {
		return new Transfer[] {TextTransfer.getInstance(), PaletteToRobotTransfer.getInstance()};
	}

}
