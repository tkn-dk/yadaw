package dk.yadaw.widgets;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.YadawDataModel;

public class SelectAsioDlg {
	private CenteredDialog dlg;
	private YadawDataModel dmodel;
	
	public SelectAsioDlg( JFrame frame, YadawDataModel model ) {
		dmodel = model;
		dlg = new CenteredDialog( frame, "Select ASIO driver");
		dlg.setLayout( new FlowLayout() );
		JLabel label = new JLabel( "Detected ASIO drivers: ");
		
		JComboBox<String> asioDrivers = new JComboBox<String>( );		
		Collection<String> asioDriverStrings = dmodel.getAsio().getDrivers();
		for( String s : asioDriverStrings ) {
			asioDrivers.addItem( s );
		}
		
		JButton okButton = new JButton( "Ok" );
		okButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dmodel.setAsioDriverName( (String) asioDrivers.getSelectedItem() );
				System.out.println( "Selected driver is: " + dmodel.getAsioDriverName() );
				dlg.dispose();
			}
		});
		
		
		JButton cancelButton = new JButton( "Cancel");
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dlg.dispose();
			}			
		});
		
		dlg.add(label );
		dlg.add(asioDrivers);
		dlg.add(okButton);
		dlg.add(cancelButton);
		dlg.setSize(250, 125);
		dlg.setResizable(false);
		dlg.locateToOwnerCenter();
		dlg.setVisible(true);		
	}
	
}
