package edu.wpi.first.javadev.builder.editor.graphics.cbswt;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public class CBComposite extends CBControl {

	/**
	 * @param parent
	 * @param style
	 */
	public CBComposite(CBControl parent, int style, int depth) {
		super(parent, style);

		parent.addCanvasPainter(depth, this);
	}

	@Override
	public CBFrame getFrame() {
		return getParent().getFrame();
	}

	@Override
	public CBControl getParent() {
		return (CBControl) super.getParent();
	}

	@Override
	public void dispose() {
		getParent().removeCanvasPainter(this);
		super.dispose();
	}

}
