package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.SwingUtilities;

public class TrackView extends Component {
	private static final long serialVersionUID = 1L;
	private Color trackColor;
	private int[] trackPeaks;
	private long peaksPosStart;
	private long peaksPosEnd;
	private long samplePos;
	private long viewPosStart;
	private long viewPosEnd;

	public TrackView(int height, Color trackColor) {
		this.trackColor = trackColor;
		peaksPosStart = 0;
		Dimension dim = new Dimension(800, height);
		setPreferredSize(dim);
	}

	public void setTrackPeaks(long startPos, long endPos, int[] trackPeaks) {
		peaksPosStart = startPos;
		peaksPosEnd = endPos;
		this.trackPeaks = trackPeaks;
		SwingUtilities.invokeLater(() -> repaint());
	}

	public void setViewWindow(long start, long end) {
		viewPosStart = start;
		viewPosEnd = end;
	}

	public void setSamplePos(long samplePos) {
		int nx = getSamplePosX(samplePos);
		int ox = getSamplePosX(this.samplePos);
		if (nx != ox) {
			this.samplePos = samplePos;
			SwingUtilities.invokeLater(() -> repaint(ox, 0, nx - ox + 1, getHeight()));
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		int width = getWidth();
		int height = getHeight();

		g.setColor(trackColor);
		g.fillRect(0, 0, width - 1, height - 1);
		drawTrackPeaks(g);
		drawSamplePos(g);
	}

	private void drawTrackPeaks(Graphics g) {
		if (trackPeaks != null) {
			int width = getWidth();
			int halfHeight = getHeight() / 2;
			int x = getSamplePosX(viewPosStart >= peaksPosStart ? viewPosStart : peaksPosStart);
			int y;
			int pinx = 0;

			g.setColor(Color.BLACK);
			while (x < width - 1 && pinx < trackPeaks.length) {
				y = (int) ((halfHeight * ( double )trackPeaks[pinx++]) / 0x7fffffff);				
//				g.drawLine(x, halfHeight - y, x, halfHeight + y);
				g.fillRect(x, halfHeight - y, 1, 2 * y + 1);
				x++;
			}
		}
	}

	private void drawSamplePos(Graphics g) {
		if (posIsVisible(samplePos)) {
			int x = getSamplePosX(samplePos);
			g.setColor(Color.BLACK);
			g.drawLine(x, 0, x, getHeight() - 2);
		}
	}

	private int getSamplePosX(long pos) {
		return (int) ((double) getWidth() * (pos - viewPosStart) / (viewPosEnd - viewPosStart));
	}

	private boolean posIsVisible(long pos) {
		return (pos > viewPosStart && pos <= viewPosEnd);
	}

}
