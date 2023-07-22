package dk.yadaw.widgets;

import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

public class TrackPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Potentiometer volume;
	private Potentiometer pan;
	private Potentiometer send1;
	private Potentiometer send2;
	private VUMeter vuIn;
	private VUMeter vuOutLeft;
	private VUMeter vuOutRight;
	private InputPanel inputPanel;
	private TrackView trackView;
	private Collection<Component> panelComponents;
	private String trackName;
	private Color trackColor;

	public TrackPanel(String trackName, Color trackColor, int inputChannel, Font font) {
		this.trackName = trackName;
		this.trackColor = trackColor;
		panelComponents = new ArrayList<Component>();
		volume = new Potentiometer(50, "Vol");
		volume.setMin(0);
		volume.setMax(40);
		volume.setValue(35);

		pan = new Potentiometer(50, "Pan");
		pan.setMin(-20);
		pan.setMax(20);

		send1 = new Potentiometer(50, "Send 1");
		send1.setMin(0);
		send1.setMax(40);

		send2 = new Potentiometer(50, "Send 2");
		send2.setMin(0);
		send2.setMax(40);

		vuIn = new VUMeter(12, 60, "In");
		vuOutLeft = new VUMeter(8, 60, "L");
		vuOutRight = new VUMeter(8, 60, "R");

		inputPanel = new InputPanel(trackName, trackColor, font, inputChannel);

		trackView = new TrackView(60, trackColor);

		setLayout(new FlowLayout());

		panelComponents.add(inputPanel);
		panelComponents.add(vuIn);
		panelComponents.add(trackView);
		panelComponents.add(send1);
		panelComponents.add(send2);
		panelComponents.add(pan);
		panelComponents.add(volume);
		panelComponents.add(vuOutLeft);
		panelComponents.add(vuOutRight);

		for (Component c : panelComponents) {
			add(c);
		}

	}

	public void resizeTrackView() {
		int pWidth = getWidth();
		int tvWidth = pWidth - 60;
		for (Component c : panelComponents) {
			if (c != trackView) {
				tvWidth -= c.getWidth();
			}
		}
		int tvHeight = trackView.getHeight();
		Dimension newDim = new Dimension(tvWidth, tvHeight);
		trackView.setPreferredSize(newDim);
	}

	public void setTrackName(String name) {
		this.trackName = name;
		inputPanel.setTrackName(name);
	}

	public String getTrackName() {
		return this.trackName;
	}

	public VUMeter getInVUMeter() {
		return vuIn;
	}

	public VUMeter getOutLeftVUMeter() {
		return vuOutLeft;
	}

	public VUMeter getOutRightVUMeter() {
		return vuOutRight;
	}

	public boolean getRecordState() {
		return inputPanel.getRecordButton().getRecordState();
	}

	public Potentiometer getVolume() {
		return volume;
	}

	public Potentiometer getPan() {
		return pan;
	}

	public TrackView getTrackView() {
		return trackView;
	}

	public InputPanel getInputPanel() {
		return inputPanel;
	}

	public Color getTrackColor() {
		return trackColor;
	}

	public void setTrackColor(Color newColor) {
		trackColor = newColor;
		inputPanel.setTrackColor(newColor);
		trackView.setTrackColor(newColor);
	}

}
