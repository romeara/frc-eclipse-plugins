package edu.wpi.first.javadev.builder.editor.graphics;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public class Focus {
	FocusManager	manager;

	public Focus(FocusManager manager) {
		setFocusManager(manager);
	}

	public boolean isFocus() {
		return manager.getCurrentFocus() == this;
	}

	public void setFocusManager(FocusManager manager) {
		if (manager == null) throw new IllegalArgumentException("null is not a valid argument");
		if (manager == this.manager) return;

		if (this.manager != null) this.manager.focusables.remove(this);

		this.manager = manager;

		if (manager.focusables.isEmpty()) setFocus();
		manager.focusables.add(this);
	}

	public void focusLost() {}

	public void focusGained() {}

	public void setFocus() {
		manager.setFocus(this);
	}

	private boolean	disposed;

	public void dispose() {
		if (isDisposed()) return;

		disposed = true;

		if (!manager.isDisposed()) {
			manager.focusables.remove(this);
		}
	}

	public boolean isDisposed() {
		return disposed;
	}

	public FocusManager getFocusManager() {
		return manager;
	}
}
