package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TrackColorChooser extends Component implements MouseListener {
	private static final long serialVersionUID = 1L;
	private final int verticals = 5;
	private final int horizontals = 5;
	private Color selectedColor;

	Color[][] colors = new Color[][] {
			{ new Color(120, 30, 30), new Color(140, 60, 60), new Color(180, 80, 80), new Color(200, 100, 100),
					new Color(250, 130, 130) },
			{ new Color(30, 120, 30), new Color(60, 140, 60), new Color(80, 180, 80), new Color(100, 200, 100),
					new Color(130, 250, 130) },
			{ new Color(30, 30, 120), new Color(60, 60, 140), new Color(80, 80, 180), new Color(100, 100, 200),
					new Color(130, 130, 250) },
			{ new Color(120, 120, 30), new Color(140, 140, 60), new Color(180, 180, 80), new Color(200, 200, 100),
					new Color(250, 250, 130) },
			{ new Color(30, 120, 120), new Color(60, 140, 140), new Color(80, 180, 180), new Color(100, 200, 200),
					new Color(130, 250, 250) } };

	public TrackColorChooser( Color initialColor ) {
		setSelectedColor( initialColor );
		setPreferredSize( new Dimension( 100, 100 ));
		addMouseListener(this);
	}

	public Color getSelectedColor() {
		return selectedColor;
	}
	
	/**
	 * Find closest matching preset color and update selected Color
	 */
	public void setSelectedColor( Color set ) {
		double minDist = 1e10;
		int hxMin = 0;
		int vxMin = 0;
		
		for( int v = 0; v < verticals; v++ ) {
			for( int h = 0; h < horizontals; h++ ) {
				double dist = getColorDifference( set, colors[v][h] );
				if( dist < minDist ) {
					hxMin = h;
					vxMin = v;
					minDist = dist;
				}
			}
		}
		
		selectedColor = colors[vxMin][hxMin];
	}
	
    private double getColorDifference(Color color1, Color color2) {
        float[] lab1 = color1.getRGBColorComponents(null);
        float[] lab2 = color2.getRGBColorComponents(null);
        
        double deltaL = lab2[0] - lab1[0];
        double deltaA = lab2[1] - lab1[1];
        double deltaB = lab2[2] - lab1[2];
        
        double deltaE = Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
        
        return deltaE;
    }	

	@Override
	public void paint(Graphics g) {
		int hwidth = getWidth() / horizontals;
		int vheight = getHeight() / verticals;
		for (int v = 0; v < verticals; v++) {
			for (int h = 0; h < horizontals; h++) {
				g.setColor(colors[v][h]);
				g.fillRect(h * hwidth, v * vheight, hwidth, vheight);
				if (colors[v][h] == selectedColor) {
					g.setColor(Color.black);
					g.drawRect(h * hwidth, v * vheight, hwidth-1, vheight-1);
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int hwidth = getWidth() / horizontals;
		int vheight = getHeight() / verticals;
		int clickedButton = e.getButton();
		switch (clickedButton) {
		case MouseEvent.BUTTON1:
			int hx = e.getX() / hwidth ;
			int vy = e.getY() / vheight;
			selectedColor = colors[vy][hx];
			repaint();
			break;
			
		default:
			// Not handled
			break;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
