package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class TrackView extends Component {
	private int height;
	private int width;
	
	public TrackView( int height ) {
		this.height = height;
		width = 800;
		Dimension dim = new Dimension( width, height );
		setPreferredSize( dim );
	}
	
	public void loadTrack( String trackFileName ) {
		
	}

	@Override
	public void paint(Graphics g) {
		g.setColor( Color.BLACK );
		g.drawRect( 0,  0,  width - 1, height - 1 );
		super.paint(g);
	}
	
	
	
}
