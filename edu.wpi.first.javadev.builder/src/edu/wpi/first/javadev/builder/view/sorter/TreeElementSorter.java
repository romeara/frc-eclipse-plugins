package edu.wpi.first.javadev.builder.view.sorter;

import org.eclipse.jface.viewers.ViewerSorter;

import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Sorts the tree elements such that they appear in the desired order,
 * using the CodeModelType ordering
 * 
 * @author Ryan O'Meara
 */
public class TreeElementSorter extends ViewerSorter {
	
	/**
	 * Arranges elements such that TreeParents appear before Tree Objects
	 */
	public int category(Object element) {
		if((element != null)&&(element instanceof FRCVElement)){
			return ((FRCVElement)element).getElementType().getOrder();
		}

		return ModelElementType.LASTORDERTYPE.getOrder();
	}

}
