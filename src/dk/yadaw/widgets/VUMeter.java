package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import dk.yadaw.utils.PeakTimer;

public class VUMeter extends JPanel {
	private static final long serialVersionUID = 1L;
	private boolean horizontal;
	private int peakVal;
	private int newVal;
	private final int numLeds = 12;
	private final int ledHeight = 8;
	private final int ledWidth = 8;
	private final int ledSpace = 2;
	private final int ledTotal = ledWidth + ledSpace;
	private final int vuMeterSpace = 10;
	private final int vuMeterHeight = numLeds * ledTotal - 2 * ledSpace;
	private final int width = 30;
	private final int height = numLeds * (ledHeight + ledSpace ) + 2 * vuMeterSpace;
	private final int xrect = ( width - ledWidth ) / 2;
	private final int yrect = vuMeterSpace + ( ( numLeds - 1 ) * ledTotal ) + ledSpace;
	private final Color normColor = new Color( 0, 255, 0 );
	private final Color warnColor = new Color( 255, 200, 0 );
	private final Color overColor = new Color( 255, 0, 0 );
	private final Color ledColors[] = { normColor, normColor, normColor, normColor, normColor, normColor, normColor, normColor, normColor, 
										warnColor, warnColor,
										overColor };
	private PeakTimer peakTimer;
	
	public VUMeter( boolean horizontal ) {
		this.horizontal = horizontal;
		setBorder(BorderFactory.createLineBorder(Color.black));
		peakVal = 0;
		newVal = 0;
		
		peakTimer = new PeakTimer() {
			public void timerEvent() {
				peakVal = 0;
				repaint( xrect, vuMeterSpace, ledWidth, height);
				System.out.println( "timerEvent");
			}
		};
		
		peakTimer.setTimer( 1000 );
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension dim;
		
		if( horizontal ) {
			dim =  new Dimension( height, width );
		}
		else {
			dim =  new Dimension( width, height );			
		}
		return dim;
	}
	
	@Override
	public void paintComponent( Graphics g ) {
		super.paintComponent(g);
		
		g.setColor( Color.black );
		g.drawRect( 0, 0, width, height);
		for( int n = 0; n <= 5; n++ ) {
			int my = vuMeterSpace + ( n * ( vuMeterHeight / 5 ));
			g.drawLine(1, my, 5, my );
		}
		
		for( int led = 0; led < numLeds; led++ ) {
			drawLed( g, led, led < newVal );
		}
		
		drawLed( g, peakVal, peakVal > 0 );
	}
	
	public void drawLed( Graphics g, int ledNo, boolean isOn ) {
		if( isOn ) {
			g.setColor( ledColors[ledNo] );
		}
		else {
			g.setColor( Color.black );
		}
		int y = yrect - ( ledNo * ( ledTotal ));
		g.fillRect( xrect, y, ledWidth, ledHeight );
	}
	
	public void setVal( int val ) {
		newVal = val;
		if( val >= peakVal ) {
			peakVal = val;
			peakTimer.setTimer( 1000 );
		}
		repaint( xrect, vuMeterSpace, ledWidth, height);
	}

}
