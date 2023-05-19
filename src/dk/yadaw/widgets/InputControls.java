package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

public class InputControls extends Component implements MouseListener {
	private boolean record;
	private String label;
    private final Font font = new Font( "arial", Font.PLAIN, 12);
    private int height;
    private int width;

	public InputControls( int width, int height, String label ) {
		this.height = height;
		this.width = width;
		this.label = label;
		Dimension dim = new Dimension( width, height );
		setPreferredSize( dim );
		addMouseListener( this );
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = ( Graphics2D )g;
		Rectangle2D strBounds = g2d.getFontMetrics().getStringBounds( label, g2d );
		g2d.setColor( Color.BLACK );
		g2d.setFont( font );
		g2d.drawRect( 0, 0,  width - 1, height / 2 - 2  );
		g2d.drawString( label, ( float )( width - strBounds.getWidth() )/2, ( float )(( height - strBounds.getHeight())/2) - 5 );

		g2d.drawRect( 0,  height / 2 + 2, width - 1 , height / 2 - 4 );
		if( record ) {
			g2d.setColor( Color.red );
			g2d.fillRect( 1,  height/2 + 3,  width-2,  height / 2 - 5 );
		}
		else {
			g2d.setColor( g2d.getBackground() );
			g2d.fillRect( 1,  height/2 + 3,  width-2,  height / 2 - 5 );
		}
		g2d.setColor( Color.black );
		g2d.fillOval( width/2 - 8, 3 * height / 4 - 8, 16, 16 );
		
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
	
	

}
