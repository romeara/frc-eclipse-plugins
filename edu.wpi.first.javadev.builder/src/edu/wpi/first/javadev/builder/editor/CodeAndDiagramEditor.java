package edu.wpi.first.javadev.builder.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import edu.wpi.first.codedev.output.FRCDialog;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRTransition;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
@SuppressWarnings("restriction")
public class CodeAndDiagramEditor extends MultiPageEditorPart implements IResourceChangeListener {

	/** The text editor used to show the code */
	protected CompilationUnitEditor	code;
	/** The editor displaying the diagram */
	protected StateMachineEditor	diagram;
	/** The editor that is currently displayed */
	protected IEditorPart			activeEditor;

	/**
	 * Creates a multi-page editor example.
	 */
	public CodeAndDiagramEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates the code tab of the editor.
	 */
	void createCodePage() {
		try {
			IEditorInput input = getEditorInput();
			setPartName(input.getName());
			code = new CompilationUnitEditor();
			int index = addPage(code, input);
			setPageText(index, "Source");
		} catch (PartInitException e) {
			FRCDialog.createErrorDialog("Error creating nested text editor", e.getStatus());
		}
	}

	/**
	 * Creates the page containing the diagram.
	 */
	void createDiagramPage() {
		try {
			diagram = new StateMachineEditor();
			setPageText(addPage(diagram, getEditorInput()), "Diagram");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createCodePage();
		createDiagramPage();
		setActivePage(1);
		activeEditor = diagram;
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		code.doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		code.doSaveAs();
		setPageText(1, code.getTitle());
		setInput(code.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Changes the active editor when the page changes
	 */
	protected void pageChange(int newPageIndex) {
		switch (newPageIndex) {
		case 0:
			activeEditor = code;
			break;
		case 1:
			activeEditor = diagram;
		}
		super.pageChange(newPageIndex);
	}

	public void focusOn(FRCRState state) {
		if (activeEditor == diagram) {
			diagram.focusOn(state);
		}
	}

	public void focusOn(FRCRTransition transition) {
		if (activeEditor == diagram) {
			diagram.focusOn(transition);
		} else {
			ISourceRange srcRange = transition.getSourceRange();
			if (srcRange != null) {
				code.setHighlightRange(srcRange.getOffset(), srcRange.getLength(), true);
			}
		}
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) code.getEditorInput()).getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(code.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}
}
