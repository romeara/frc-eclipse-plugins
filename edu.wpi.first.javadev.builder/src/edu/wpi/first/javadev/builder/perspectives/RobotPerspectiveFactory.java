package edu.wpi.first.javadev.builder.perspectives;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import edu.wpi.first.javadev.builder.view.views.DevicePaletteView;
import edu.wpi.first.javadev.builder.view.views.RoboCentricView;

/**
 * This class generates the Robot Perspective that will be switched to when the
 * user begins a new Robot
 * 
 * @author Joe Grinstead
 */
public class RobotPerspectiveFactory implements IPerspectiveFactory {

	public static final String ID = "edu.wpi.first.javadev.builder.RobotPerspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective");
		
		IFolderLayout folder = layout.createFolder("edu.wpi.first.javadev.builder.folder.left", IPageLayout.LEFT, .2f, layout.getEditorArea());
		folder.addView(RoboCentricView.ID);
		folder.addView(JavaUI.ID_PACKAGES);
		
		layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, .8f, layout.getEditorArea());
		
		layout.addView(DevicePaletteView.ID, IPageLayout.RIGHT, .8f, layout.getEditorArea());
	}

}
