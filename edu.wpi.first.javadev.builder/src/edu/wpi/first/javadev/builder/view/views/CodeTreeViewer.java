package edu.wpi.first.javadev.builder.view.views;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Provides methods to automatically refresh the tree without changing the expanded state
 * of the tree from the user's point of view
 * @author Ryan O'Meara
 */
public class CodeTreeViewer extends TreeViewer {
	public CodeTreeViewer(Tree tree) {
		super(tree);
	}

	public CodeTreeViewer(Composite parent, int style) {
		super(parent, style);
	}
	
	public CodeTreeViewer(Composite parent){
		super(parent);
	}
	
	/** Adds the given object to the given parent and displays that change */
	public void addToModelandView(FRCVParent dParent, FRCVElement addition){
		dParent.add(addition);
		add(dParent, addition);
		refreshTree(dParent);
	}
	
	/** Adds the given array fo objects to the given parent and displays that change */
	public void addToModelandView(FRCVParent dParent, FRCVElement[] addition){
		dParent.add(addition);
		add(dParent, addition);
		refreshTree(dParent);
	}

	/**
	 * Refresh the root of the tree if no specific element if given
	 */
	public void refreshTree(){
		refreshTree(getRoot()); 
	}
	
	/**
	 * Refreshes given element and its children while keeping the tree in its current
	 * expanded state
	 * @param element The element to refresh
	 */
	private void refreshTree(Object element){
		try{
			Object[] elements = getExpandedElements();
			TreePath[] treePaths = this.getExpandedTreePaths();
			refresh(element, true);
			setExpandedElements(elements);
			setExpandedTreePaths(treePaths);
		}catch(Exception e){}
	}
}
