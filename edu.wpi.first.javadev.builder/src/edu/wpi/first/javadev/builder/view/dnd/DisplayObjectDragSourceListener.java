package edu.wpi.first.javadev.builder.view.dnd;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.builder.workspace.model.library.FRCLDevice;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/** 
 * Allows a display tree to have drag actions performed on it
 * 
 * @author Ryan O'Meara
 */
public class DisplayObjectDragSourceListener implements DragSourceListener{
	protected TreeViewer viewer;
	protected IType dragged;
	
	public DisplayObjectDragSourceListener(TreeViewer viewer){
		this.viewer = viewer;
		dragged = null;
	}
	
	@Override
	public void dragFinished(DragSourceEvent event) {
		if(dragged != null){
			ICompilationUnit cu = FRCModel.getActiveJavaFile();
			
			if(cu != null){
				IImportContainer ic = cu.getImportContainer();
				
				if(!ic.getImport(dragged.getFullyQualifiedName()).exists()){
					try {
						cu.createImport(dragged.getFullyQualifiedName(), null, null);
					} catch (Exception e) {e.printStackTrace();}
				}
			}
		}
		dragged = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dragSetData(DragSourceEvent event) {
		dragged = null;
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		FRCVElement[] dObjs = (FRCVElement[])selection.toList().toArray(new FRCVElement[selection.size()]);
		ArrayList<String> transferID = new ArrayList<String>();
		
		for(FRCVElement current : dObjs){
			if(current.getModelElement().getFullyQualifiedName() != null){
				transferID.add(current.getModelElement().getFullyQualifiedName());
			}
		}
		
		if(PaletteToRobotTransfer.getInstance().isSupportedType(event.dataType)){
			event.data = transferID.toArray(new String[transferID.size()]);
		}else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			if(((FRCVElement)((ITreeSelection)viewer.getSelection()).getPaths()[0].getLastSegment()).isDraggableIntoEditor()){
				event.data = ((FRCVElement)((ITreeSelection)viewer.getSelection()).getPaths()[0].getLastSegment()).getCodeFragment() + "\n";
				if(((FRCVElement)((ITreeSelection)viewer.getSelection()).getPaths()[0].getLastSegment()).getModelElement() != null){
					if(((FRCVElement)((ITreeSelection)viewer.getSelection()).getPaths()[0].getLastSegment()).getModelElement() instanceof FRCLDevice){
						dragged = ((FRCLDevice)((FRCVElement)((ITreeSelection)viewer.getSelection()).getPaths()[0].getLastSegment()).getModelElement()).getType();
					}
				}
				return;	
			}
			
			//event.doit = false;
			event.data = " ";	
		}
	}
	
	@Override
	public void dragStart(DragSourceEvent event) {
		dragged = null;
		// Only start the drag if it is allowed
		try{
			if(viewer.getSelection() instanceof ITreeSelection){
				event.doit = ((FRCVElement)((ITreeSelection)viewer.getSelection()).getPaths()[0].getLastSegment()).isDraggable();
			}
		}catch(Exception e){
			e.printStackTrace();
			event.doit = false;
		}
	}

}
