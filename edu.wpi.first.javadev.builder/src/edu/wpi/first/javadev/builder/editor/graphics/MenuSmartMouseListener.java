package edu.wpi.first.javadev.builder.editor.graphics;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

/**
 * This class will act like a mouse listener that is aware of when a mouse press
 * actually opens a menu.
 * 
 * @author Joe Grinstead
 */
public class MenuSmartMouseListener implements MouseListener, MenuDetectListener {
	protected boolean	isMenuAction	= false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		mouseDoubleClick(e, isMenuAction);
	}

	public void mouseDoubleClick(MouseEvent e, boolean isMenuAction) {}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		mouseDown(e, isMenuAction);
		isMenuAction = false;
	}

	public void mouseDown(MouseEvent e, boolean isMenuAction) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MenuDetectListener#menuDetected(org.eclipse.swt
	 * .events.MenuDetectEvent)
	 */
	@Override
	public void menuDetected(MenuDetectEvent e) {
		isMenuAction = true;
	}

}
