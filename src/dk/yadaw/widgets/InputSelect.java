package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class InputSelect extends Component {
	private static final long serialVersionUID = 1L;
	private int channel;
	private Font font;

	public InputSelect(int channel, Font font) {
		this.channel = channel;
		this.font = font;
		Dimension dim = new Dimension( 40, 20 );
		setPreferredSize( dim );
	}

	@Override
	public void paint(Graphics g) {
		int height = getHeight();
		int width = getWidth();
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.BLACK);
		g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		g2.setFont(font);
		String chString = Integer.toString(channel);
		int tw = g2.getFontMetrics().stringWidth(chString);
		int y = (height - g2.getFontMetrics().getHeight() / 2);
		int x = (width -  tw) / 2;
		System.out.println( "InputSelect x:" + x + " y:" + y + " tw:" + tw + " width:" + width );
		g2.drawString(chString, x, y);
	}

}
