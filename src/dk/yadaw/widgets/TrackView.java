package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class TrackView extends Component {
	
	public TrackView( int height ) {
		int width = 800;
		Dimension dim = new Dimension( width, height );
		setPreferredSize( dim );
	}
	
	public void loadTrack( String trackFileName ) {
		
	}

	@Override
	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		g.setColor( Color.BLACK );
		g.drawRect( 0,  0,  width - 1, height - 1 );
		super.paint(g);
	}
	
	
	
}
