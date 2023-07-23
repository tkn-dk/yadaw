package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class RecordButton extends Component implements MouseListener {
	private static final long serialVersionUID = 1L;
	private boolean record;

	public RecordButton() {
		addMouseListener( this );
		Dimension dim = new Dimension( 40, 20 );
		setPreferredSize( dim );
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = ( Graphics2D )g;
		int w = getWidth();
		int h = getHeight();
		
		g2d.drawRect( 0,  0, w - 1 , h - 1 );
		if( record ) {
			g2d.setColor( Color.red );
			g2d.fillRect( 1,  1,  w - 2,  h - 2 );
		}
		else {
			g2d.setColor( g2d.getBackground() );
			g2d.fillRect( 1,  1, w - 2, h - 2 );
		}
		g2d.setColor( Color.black );
		g2d.fillOval( w/2 - 6, h/2 - 6, 12, 12 );
		
		super.paint(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println( "Mouse clicked - record: " + record );
		record = record == true ? false : true;
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isRecording() {
		return record;
	}

}
