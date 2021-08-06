package dk.yadaw.widgets;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import dk.yadaw.audio.Asio;
import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.YadawDataModel;

/**
 * Displays ASIO related audio parameters
 * @author tkn
 *
 */
public class ViewAudioParmsDlg {
	private final int DLG_WIDTH = 300;
	private final int DLG_HEIGHT = 250;
	private YadawDataModel model;
	
	public ViewAudioParmsDlg( JFrame owner ) {
		model = DataModelInstance.getModelInstance();
		Asio asio = model.getAsio();
		CenteredDialog dlg = new CenteredDialog( owner, "Audio Settings" );
		dlg.setLayout(null);
		
		JLabel driverNameLabel = new JLabel( "Driver:" );
		JLabel driverName = new JLabel( model.getAsioDriverName() );
		
		JLabel sampleRateLabel = new JLabel( "Sample rate:" );
		JLabel sampleRate = new JLabel( String.valueOf(asio.getSamplerate()) );
		
		JLabel numberOfInputsLabel = new JLabel( "Number of inputs:");
		JLabel numberOfInputs = new JLabel( String.valueOf( asio.getNofInputs()));
		
		JLabel numberOfOutputsLabel = new JLabel( "Number of outputs:" );
		JLabel numberOfOutputs = new JLabel( String.valueOf( asio.getNofOutputs()));
		
		JLabel bufferSizeLabel = new JLabel( "Buffer size:" );
		JLabel bufferSizeValue = new JLabel( String.valueOf( asio.getBufferSize()) );
		
		JLabel outputLatencyLabel = new JLabel( "Output latency:");
		JLabel outputLatencyValue = new JLabel( String.valueOf(  (1000.0 * asio.getOutputLatency()) / asio.getSamplerate() ) + "ms" );
		
		JLabel inputLatencyLabel = new JLabel( "Input latency:" );
		JLabel inputLatencyValue = new JLabel( String.valueOf(  (1000.0 * asio.getInputLatency()) / asio.getSamplerate() ) + "ms" );
		
		dlg.add( driverNameLabel );
		dlg.add( driverName );
		dlg.add( sampleRateLabel );
		dlg.add( sampleRate );
		dlg.add( numberOfInputsLabel );
		dlg.add( numberOfInputs );
		dlg.add( numberOfOutputsLabel );
		dlg.add( numberOfOutputs );
		dlg.add( bufferSizeLabel );
		dlg.add( bufferSizeValue );
		dlg.add( outputLatencyLabel );
		dlg.add( outputLatencyValue );
		dlg.add( inputLatencyLabel );
		dlg.add( inputLatencyValue );
		
		dlg.setSize( DLG_WIDTH, DLG_HEIGHT );
		dlg.setResizable( false );
		
		dlg.locateToOwnerCenter();
		locateLabel( driverNameLabel, driverName, 20 );
		locateLabel( sampleRateLabel, sampleRate, 40 );
		locateLabel( numberOfInputsLabel, numberOfInputs, 60 );
		locateLabel( numberOfOutputsLabel, numberOfOutputs, 80 );
		locateLabel( bufferSizeLabel, bufferSizeValue, 100 );
		locateLabel( outputLatencyLabel, outputLatencyValue, 120 );
		locateLabel( inputLatencyLabel, inputLatencyValue, 140 );
		dlg.setVisible(true);
	}
	
	private void locateLabel( JLabel label, JLabel value, int ypos ) {
		Dimension labelDim = label.getPreferredSize();
		Dimension valueDim = value.getPreferredSize();
		label.setBounds( DLG_WIDTH / 2 - labelDim.width - 3, ypos, labelDim.width, labelDim.height  );
		value.setBounds( DLG_WIDTH / 2 + 3, ypos, valueDim.width, valueDim.height  );		
	}
	
}
