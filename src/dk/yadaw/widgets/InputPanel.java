package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class InputPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Color trackColor;
	private String label;
	private int inputChannel;
	private RecordButton recordButton;
	private InputLabel inputLabel;
	private InputSelect inputSelect;

	public InputPanel(String label, Color trackColor, Font font, int inChannel) {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		recordButton = new RecordButton();
		inputLabel = new InputLabel(label, trackColor, font);
		inputSelect = new InputSelect(inChannel, font);

		this.trackColor = trackColor;
		this.label = label;
		this.inputChannel = inChannel;
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.set(2, 0, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		add(inputLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.ipady = 0;
		add( recordButton, gbc );
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		add( inputSelect, gbc );
	}
	
	public String getTrackName() {
		return label;
	}
	
	public void setTrackName( String newName ) {
		label = newName;
		inputLabel.setLabel(newName);
	}
	
	public Color getTrackColor() {
		return trackColor;
	}
	
	public void setTrackColor( Color newColor ) {
		trackColor = newColor;
		inputLabel.setLabelColor(newColor);
		
	}
	
	public int getInputChannel() {
		return inputChannel;
	}
	
	public RecordButton getRecordButton() {
		return recordButton;
	}
	
	public InputLabel getInputLabel() {
		return inputLabel;
	}

	public InputSelect getInputSelect() {
		return inputSelect;
	}
}
