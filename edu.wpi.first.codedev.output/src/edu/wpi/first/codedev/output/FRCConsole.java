package edu.wpi.first.codedev.output;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Works with the Eclipse console view to display output to the workspace.  Uses the 
 * default console view, but a custom console (different consoles can be selected from
 * the console view)
 * 
 * @author Ryan O'Meara
 */
public class FRCConsole {
	/** The name of the console always used by the FRC plug-ins to output to the workspace */
	public static final String FRC_CONSOLE_NAME = "FRC Output Console";
	
	/**
	 * Retrieve the FRC console for output to the workbench
	 * @return The MessageConsole instance for the FRC console
	 */
	private static MessageConsole findConsole(){
		 ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (FRC_CONSOLE_NAME.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(FRC_CONSOLE_NAME, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	}
	
	/** Shows the console in the workbench view */
	private static void showConsole(){
		try{
			IWorkbenchPage active = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if(active != null){active.showView(FRC_CONSOLE_NAME);}
		}catch(Exception e){}
	}
	
	/**
	 * Write a message to the console view in the workbench
	 * @param message The String to write to the console
	 */
	public static void writeToConsole(String message){
		if(message == null){return;}
		MessageConsole myConsole = findConsole();
		showConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(message);
		try {out.close();} catch (Exception e) {}
	}
}
