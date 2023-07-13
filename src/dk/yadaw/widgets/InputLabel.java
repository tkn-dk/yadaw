package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class InputLabel extends Component {
	private static final long serialVersionUID = 1L;
	private String label;
	private Color labelColor;
	private Font labelFont;

	public InputLabel(String label, Color color, Font font) {
		this.label = label;
		labelColor = color;
		labelFont = font;
		
		Dimension dim = new Dimension( 100, 20 );
		setPreferredSize( dim );
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel( String label ) {
		this.label = label;
	}
	
	public Color getLabelColor() {
		return labelColor;
	}
	
	public void setLabelColor( Color labelColor ) {
		this.labelColor = labelColor;
	}
	
	@Override
	public void paint(Graphics g) {
		int height = getHeight();
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(labelColor);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.setColor(Color.BLACK);
		g2.setFont(labelFont);
		int y = ( height - g2.getFontMetrics().getHeight()/2);
		g2.drawString( label, y, y );
	}

}
