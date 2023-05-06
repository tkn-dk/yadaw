package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import dk.yadaw.utils.PeakTimer;

public class VUMeter extends Component {
	private static final long serialVersionUID = 1L;
	private int peakVal;
	private int newVal;
	private int max;
	private int min;
	private int zero;
	private int width;
	private int height;
	private int vumHeight;
	private String label;
	private final Color normColor = new Color( 0, 200, 0 );
	private final Color warnColor = new Color( 200, 200, 0 );
	private final Color overColor = new Color( 200, 0, 0 );

	private PeakTimer peakTimer;
	
	public VUMeter( int width, int height, String label ) {
		this.width = width;
		this.height = height;
		this.label = label;
		vumHeight = height - 2;
		max = 12;
		min = -40;
		zero = 0;
		newVal = min;
		peakVal = min;
		
		peakTimer = new PeakTimer() {
			public void timerEvent() {
				peakVal = min;
				repaint();
			}
		};
		
		peakTimer.setTimer( 1000 );
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension dim;
		dim =  new Dimension( width, height );			
		return dim;
	}
	
	@Override
	public void paint( Graphics g ) {
		Graphics2D g2d = ( Graphics2D ) g;
		Color lowPart = normColor;
		Color highPart = warnColor;
		
		if( newVal == max ) {
			lowPart = overColor;
			highPart = overColor;
		}
		
		vumRect( g2d, Color.BLACK, 0, 0,  width, height);
		
		int nlvl = valToPixel( newVal );
		int plvl = valToPixel( peakVal );
		int zlvl = valToPixel( zero );
		
		if( newVal > zero ) {
			vumRect( g2d, lowPart, 2, zlvl, width - 4, vumHeight - zlvl);
			vumRect( g2d, highPart, 2, nlvl, width - 4, zlvl - nlvl );
			
			if( peakVal > newVal ) {
				g2d.drawLine( 2,  plvl,  width - 4, plvl );
			}
		}
		else if( newVal > min ){
			vumRect( g2d, lowPart, 2, nlvl, width - 4, vumHeight - nlvl );
			if( peakVal > newVal ) {
				g2d.drawLine( 2, plvl, width - 4, plvl );							
			}
		} else if( peakVal > min ) {
			g2d.setBackground( lowPart );
			g2d.drawLine( 2, plvl, width - 4, plvl );			
		}
	}
		
	public void setVal( int val ) {
		newVal = val;
		System.out.println( "val: " + val + "  pxlVal: " + valToPixel( val ));
		if( val >= peakVal ) {
			peakVal = val;
			peakTimer.setTimer( 1000 );
		}
		repaint();
	}
	
	public int getVal() {
		return newVal;
	}
	
	public void setMax( int max ) {
		this.max = max;
	}
	
	public int getMax() {
		return max;
	}
	
	public void setMin( int min ) {
		this.min = min;
	}
	
	public int getMin() {
		return min;
	}
	
	/**
	 * Set for which values the VU meter color will change from green to orange.
	 * @param zero The value between min and max.
	 */
	public void setZero( int zero ) {
		this.zero = zero;
	}
	
	public int getZero() {
		return zero;
	}
	
	private int valToPixel( int val ) {
		return vumHeight - ( (val - min) * height )/( max - min ) + 2;
	}
	
	private void vumRect( Graphics2D g2d, Color col, int x, int y, int width, int height ) {
//		System.out.println( "vumRect: x: " + x + ", y: " + y + ", width: " + width + ", height: + " + height );
		g2d.setColor(col);
		g2d.fillRect(x, y, width, height);	
	}
}

