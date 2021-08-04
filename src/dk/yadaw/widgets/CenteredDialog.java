package dk.yadaw.widgets;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Window;

import javax.swing.JDialog;

public class CenteredDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public CenteredDialog(Dialog owner, boolean modal) {
		super(owner, modal);
	}

	public CenteredDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
	}

	public CenteredDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public CenteredDialog(Dialog owner, String title) {
		super(owner, title);
	}

	public CenteredDialog(Dialog owner) {
		super(owner);
	}

	public CenteredDialog(Frame owner, boolean modal) {
		super(owner, modal);
	}

	public CenteredDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
	}

	public CenteredDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
	}

	public CenteredDialog(Frame owner, String title) {
		super(owner, title);
	}

	public CenteredDialog(Frame owner) {
		super(owner);
	}

	public CenteredDialog(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
	}

	public CenteredDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
	}

	public CenteredDialog(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
	}

	public CenteredDialog(Window owner) {
		super(owner);
	}

	public void locateToOwnerCenter() {
		 Window owner = this.getOwner();
		 if( owner != null ) {
			Dimension pdim = owner.getSize();
			int px = owner.getX();
			int py = owner.getY();
			Dimension ddim = getSize();
			Point pp = new Point( px + pdim.width / 2 - ddim.width / 2, py + pdim.height / 2 - ddim.height / 2 );
			setLocation( pp );
		 }
	}

}
