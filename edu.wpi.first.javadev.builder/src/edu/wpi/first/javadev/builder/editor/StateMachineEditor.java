package edu.wpi.first.javadev.builder.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.editor.graphics.statemachine.StateMachineControl;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRStateMachine;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRTransition;

public class StateMachineEditor extends EditorPart implements IFRCModelEventListener {

	// Editing Code
	protected ASTParser					parser;
	protected CompilationUnit			root;
	protected ICompilationUnit			unit;
	protected IElementChangedListener	jmListener;

	// Display Information
	protected FRCRStateMachine			model;
	protected Runnable					runnableCompute;
	protected StateMachineControl		control;

	public StateMachineEditor() {
		super();
		runnableCompute = new Runnable() {
			@Override
			public void run() {
				control.compute();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// Default stuff
		setSite(site);
		setInput(input);

		// Create the working copy buffer
		this.unit = (ICompilationUnit) JavaUI.getEditorInputJavaElement(input);
		try {
			unit.becomeWorkingCopy(null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// Create the control
		control = new StateMachineControl(parent);
		
		// Find the machine
		Object machine = CodeViewerPlugin.getFRCModel().findElement(unit.findPrimaryType());
		if (machine != null && machine instanceof FRCRStateMachine) {
			setModel((FRCRStateMachine) machine);
		} else {
			// Register to the model and get the machine as soon as possible
			CodeViewerPlugin.getFRCModel().addListener(this);
		}
	}
	
	/**
	 * This will attach this view to the given model. It will register and
	 * process events.
	 * 
	 * @param model
	 *            the model
	 */
	public synchronized void setModel(FRCRStateMachine model) {
		System.out.println("Setting Editor Model To: " + model);
		this.model = model;
		control.setModel(this.model);
		IFile file;
		try {
			file = getFile(unit);
			control.setFile(file);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		control.compute();
	}
	
	public void focusOn(FRCRState state) {
		control.focusOn(state);
	}
	
	public void focusOn(FRCRTransition transition) {
		control.focusOn(transition);
	}

	/**
	 * @return the model
	 */
	public synchronized FRCRStateMachine getModel() {
		return model;
	}

	public static IFile getFile(ICompilationUnit unit) throws JavaModelException {
		return (IFile) unit.getCorrespondingResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			unit.reconcile(AST.JLS3, false, null, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		control.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		CodeViewerPlugin.getFRCModel().removeListener(this);
		try {
			unit.discardWorkingCopy();
		} catch (JavaModelException e) {}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see edu.wpi.first.javadev.builder.model.event.IModelListener#receiveChangeEvent(edu.wpi.first.javadev.builder.model.event.ModelChangeEvent)
	 */
	@Override
	public void receiveEvent(FRCModelEvent event) {
		if (model == null) {
			Object machine = CodeViewerPlugin.getFRCModel().findElement(unit.findPrimaryType());
			if (machine != null && machine instanceof FRCRStateMachine) {
				setModel((FRCRStateMachine) machine);
				CodeViewerPlugin.getFRCModel().removeListener(this);
			}
		}
	}
}
