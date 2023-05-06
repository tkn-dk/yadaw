package dk.yadaw.widgets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Potentiometer extends java.awt.Component implements MouseWheelListener {
    private final float startAngle = 30;
    private final float endAngle = 330;
    private final float pi = ( float )3.14159265359;
    private final BasicStroke stroke = new BasicStroke(1);
    private final Font font = new Font( "arial", Font.PLAIN, 10);
    private final int yOffset = 12;
    private final int xOffset = 2;
    private int value = 0;
    private int min = 0;
    private int max = 40;
    private int sizeX, sizeY;
    private String label;

    public Potentiometer( int width, String label ) {
        this.sizeX = width;
        this.sizeY = width + 10;
        setPreferredSize(new Dimension(sizeX, sizeY + 1));
        addMouseWheelListener(this);
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        repaint();
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    
    private void drawArc( Graphics2D g ) {
    	int ly = -1;
    	int lx = -1;
    	int r = (sizeX - 4)/2;
    	float arcPoints = 2 * pi * r * (endAngle - startAngle ) / 360 ;
    	float startRad = (2 * pi * startAngle) / 360;
    	float endRad = (2 * pi * endAngle) / 360;
    	float dRad = ( endRad - startRad ) / arcPoints;
    	while( startRad < endRad ) {
    		int x = ( int )( r * Math.sin( startRad )) + r + xOffset;
    		int y = ( int )( r * Math.cos( startRad )) + r + yOffset;
    		if( ly > 0 ) {
    			g.drawLine( lx, ly, x, y);
    		}
    		else {
    			g.drawLine(x, y, x, y);
    		}
    		ly = y;
    		lx = x;
    		startRad += dRad;
    	}
    }
    
    private void drawArrow( Graphics2D g ) {
    	final int asize = 9;
    	final int gsize = 5;
    	int r = (sizeX - 4)/2;
    	float angle = ( ( max - value ) * (endAngle - startAngle))/(max - min ) + startAngle;
    	float rad = ( angle / 360 ) * 2 * pi;
    	int x1 = ( int )( (r-asize) * Math.sin( rad )) + r + xOffset;
    	int y1 = ( int )( (r-asize) * Math.cos( rad )) + r + yOffset;
    	int x2 = ( int )( r * Math.sin( rad )) + r + xOffset;
    	int y2 = ( int )( r* Math.cos( rad )) + r + yOffset;
    	int x3 = ( int )( gsize * Math.sin( rad - pi/2)) + x1;
    	int y3 = ( int )( gsize * Math.cos( rad - pi/2)) + y1;
    	g.drawLine(x1 - (x3-x1), y1 - (y3-y1), x2, y2 );
    	g.drawLine(x1 - (x3-x1), y1 - (y3-y1), x3, y3 );
    	g.drawLine(x3, y3, x2, y2);
    	
    	g.drawString( Integer.toString(value), sizeX/2 - 4, sizeY/2 + 10 );
    }
    
    @Override
    public void paint(Graphics g) {
    	Graphics2D g2d = ( Graphics2D )g;
        g2d.setColor(Color.BLACK);
        g2d.setFont( font );
        g2d.setStroke(stroke);
        g2d.drawRect(0, 0, sizeX - 1, sizeY - 1 );
        g2d.drawString( label, 2, 10 );
        drawArc( g2d );
        drawArrow( g2d );
		super.paint(g);
    }

    @Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		value += e.getWheelRotation();
		if( value < min ) {
			value = min;
		}
		else if( value > max ) {
			value = max;
		}
		repaint();
	}
}
