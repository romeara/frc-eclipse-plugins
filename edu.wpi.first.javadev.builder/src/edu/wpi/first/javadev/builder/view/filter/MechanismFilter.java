package edu.wpi.first.javadev.builder.view.filter;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCView;

/** 
 * Filter that hides any non-mechanisms in the tree
 * 
 * @author Ryan O'Meara
 */
public class MechanismFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return (element instanceof FRCView)
		||((element instanceof FRCVElement)
				&&((((FRCVElement)element).getElementType().equals(ModelElementType.FRCRMECHANISM))
						||(((FRCVElement)element).getElementType().equals(ModelElementType.FRCROBOT))));
	}

}


