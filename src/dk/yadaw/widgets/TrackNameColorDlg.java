package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TrackNameColorDlg extends CenteredDialog {
	private static final long serialVersionUID = 1L;
	TrackColorChooser colorChooser;
	JTextField trackNameEdit;
	TrackNameColorListener updateListener;

	public TrackNameColorDlg(Frame owner, String currentName, Color currentColor, TrackNameColorListener updateListener ) {
		super(owner, "Track name & Color");
		setLayout( new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		this.updateListener = updateListener;
		
		colorChooser = new TrackColorChooser( currentColor );
		trackNameEdit = new JTextField( currentName );
		trackNameEdit.setPreferredSize( new Dimension( 200, 30 ));
		
		JButton okButton = new JButton( "Ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateListener.trackNameUpdated(trackNameEdit.getText());
				updateListener.trackColorUpdated(colorChooser.getSelectedColor());
				dispose();
			}
		});
		
		
		JButton cancelButton = new JButton( "Cancel");
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}			
		});
		
		JLabel trackNameLabel = new JLabel( "Enter track name:");
		JLabel selectColorLabel = new JLabel( "Select track color:");
		
		Insets ins = new Insets( 0, 0, 20, 0 );
		Insets defIns = gbc.insets;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 3;
		add( trackNameLabel, gbc );
		
		
		gbc.insets = ins;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.gridwidth = 3;
		add(trackNameEdit, gbc);
		
		gbc.insets = defIns;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		add( selectColorLabel, gbc);
		
		gbc.insets = defIns;
		gbc.insets = ins;
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		add(colorChooser, gbc);

		
		gbc.insets = defIns;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		add( okButton, gbc );
		
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		add( cancelButton, gbc );
		
		setSize( 320, 280 );
		locateToOwnerCenter();
		setVisible(true);
	}

}
