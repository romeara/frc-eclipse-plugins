package edu.wpi.first.javadev.builder.view.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVElement;

/**
 * Provides icons and text selection for all tree objects
 * @author Ryan O'Meara
 */
public class ModelViewLabelProvider extends LabelProvider {	
	private Map<ImageDescriptor, Image> imageCache = new HashMap<ImageDescriptor, Image>(11);
	
	@Override
	public Image getImage(Object element) {
		ImageDescriptor descriptor = null;
		
		if(element instanceof FRCVElement){
			descriptor = CodeViewerPlugin.getImageDescriptor(((FRCVElement)element).getElementType().getIconPath());
			
			if(!((FRCVElement)element).getEnabled()){
				descriptor = CodeViewerPlugin.getImageDescriptor(((FRCVElement)element).getElementType().getDisabledIconPath());
			}
		}else{
			throw unknownElement(element);
		}

		//obtain the cached image corresponding to the descriptor
		Image image = (Image)imageCache.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			imageCache.put(descriptor, image);
		}
		return image;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof FRCVElement) {
			return ((FRCVElement)element).getDisplayName();
		}
		
		return "";
	}

	@Override
	public void dispose() {
		for (Iterator<Image> i = imageCache.values().iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		imageCache.clear();
	}

	/**
	 * Thrown if an element not of the type TreeObject is encountered
	 * @param element The element encountered
	 * @return The built exception
	 */
	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}

}
