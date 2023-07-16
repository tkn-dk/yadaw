package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.YadawDataModel;

public class InputLabel extends Component implements MouseListener {
	private static final long serialVersionUID = 1L;
	private String label;
	private Color labelColor;
	private Font labelFont;
	private YadawDataModel yaModel;

	public InputLabel(String label, Color color, Font font) {
		this.label = label;
		labelColor = color;
		labelFont = font;
		yaModel = DataModelInstance.getModelInstance();
		
		Dimension dim = new Dimension( 100, 20 );
		setPreferredSize( dim );
		addMouseListener( this );
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
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		yaModel.mixerMouseClick(e, this);
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
