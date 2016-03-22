package edu.wpi.first.javadev.builder.editor.graphics;

import java.util.HashSet;

/**
 * This class is where the user's focus may be held
 * 
 * @author Joe Grinstead
 */
public class FocusManager {
	protected HashSet<Focus>	focusables	= new HashSet<Focus>();
	protected Focus				current;
	private boolean				disposed	= false;

	public Focus getCurrentFocus() {
		return current;
	}

	public boolean setFocus(Focus focus) {
		if (focus == null) swap(focus);

		if (focus.getFocusManager() != this)
			throw new IllegalArgumentException("Given focus does not belong to this manager");

		return focus.equals(current) ? false : swap(focus);
	}

	protected boolean swap(Focus focus) {
		Focus previous = current;
		current = focus;
		if (previous != null) previous.focusLost();
		if (current != null) current.focusGained();
		return true;
	}

	public boolean isDisposed() {
		return disposed;
	}

	public void dispose() {
		if (!isDisposed()) {
			disposed = true;
			for (Focus focus : focusables) {
				focus.dispose();
			}
		}
	}
}
