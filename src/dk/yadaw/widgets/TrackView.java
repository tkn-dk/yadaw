package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.SwingUtilities;

public class TrackView extends Component {
	private static final long serialVersionUID = 1L;
	private Color trackColor;
	private int[] trackPeaks;
	private long samplePosStart;
	private long samplePosEnd;
	private long samplePos;
	
	public TrackView( int height, Color trackColor ) {
		this.trackColor = trackColor;
		samplePosStart = 0;
		samplePosEnd = 120 * 48000;
		Dimension dim = new Dimension( 800, height );
		setPreferredSize( dim );
	}
	
	public void setTrackPeaks( long startPos, long endPos, int[] trackPeaks ) {
		samplePosStart = startPos;
		samplePosEnd = endPos;
		this.trackPeaks = trackPeaks;
		repaint();
	}
	
	public void setSamplePos(long samplePos) {
		int nx = getSamplePosX(samplePos);
		int ox = getSamplePosX(this.samplePos);
		if (nx != ox) {
			this.samplePos = samplePos;
			SwingUtilities.invokeLater(() -> {
				repaint(ox, 0, nx - ox + 1, getHeight());
			});
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int width = getWidth();
		int height = getHeight();

		g.setColor(trackColor);
		g.fillRect(0, 0, width - 1, height - 1);
		drawTrackPeaks( g );
		drawSamplePos( g );
	}	
	
	private void drawTrackPeaks( Graphics g ) {
		
	}
	
	private void drawSamplePos( Graphics g ) {
		if( posIsVisible( samplePos )) {
			int x = getSamplePosX( samplePos );
			g.setColor( Color.BLACK );
			g.drawLine( x, 0, x, getHeight() - 2 );
		}		
	}
	
	private int getSamplePosX( long pos ) {
		return ( int )(( double ) getWidth() * (pos - samplePosStart ) / (samplePosEnd - samplePosStart ) );
	}
	
	private boolean posIsVisible( long pos ) {
		return ( pos > samplePosStart && pos <= samplePosEnd ); 
	}
	
}
