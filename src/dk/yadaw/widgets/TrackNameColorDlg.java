package dk.yadaw.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

public class TrackNameColorDlg extends CenteredDialog {
	private static final long serialVersionUID = 1L;
	TrackColorChooser colorChooser;
	JTextField trackNameEdit;
	TrackNameColorListener updateListener;

	public TrackNameColorDlg(Frame owner, String currentName, Color currentColor, TrackNameColorListener updateListener ) {
		super(owner, "Track name & Color");
		setLayout( new FlowLayout() );
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
		
		
		add(trackNameEdit);
		add(colorChooser);
		add( okButton );
		add( cancelButton );
		setSize( 320, 280 );
		locateToOwnerCenter();
		setVisible(true);

	}

}
