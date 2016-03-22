package edu.wpi.first.codedev.output;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Creates and displays dialog windows in the Eclipse workspace.  Dialog type is determined
 * by the Status used to create the dialog
 * 
 * @author Ryan O'Meara
 */
public class FRCDialog {
	/**
	 * Opens an error dialog with the given message and status
	 * @param message The information message to display
	 * @param status The status which represents the state of the program to convey
	 */
	public static void createErrorDialog(String message, IStatus status){
		ErrorDialog.openError(Display.getDefault().getActiveShell(),
				message,
				null,
				status);
	}
}
