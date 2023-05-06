package dk.yadaw.widgets;

import java.awt.FlowLayout;

import javax.swing.JPanel;

public class TrackPanel extends JPanel {
	private Potentiometer volume;
	private Potentiometer pan;
	private Potentiometer send1;
	private Potentiometer send2;
	private VUMeter vuIn;
	private VUMeter vuOutLeft;
	private VUMeter vuOutRight;
	private InputControls inCtrl;
	private TrackView trackView;
	private int height;
	
	public TrackPanel( int channel ) {
		volume = new Potentiometer( 50, "Vol" );
		volume.setMin( 0 );
		volume.setMax( 40 );
		
		pan = new Potentiometer( 50, "Pan" );
		pan.setMin( -20 );
		pan.setMax( 20 );
		
		send1 = new Potentiometer( 50, "Send 1" );
		send1.setMin( 0 );
		send1.setMax( 40 );
		
		send2 = new Potentiometer( 50, "Send 2");
		send2.setMin( 0 );
		send2.setMax( 40 );
		
		vuIn = new VUMeter( 12, 60, "In" );
		vuOutLeft = new VUMeter( 8, 60, "L" );
		vuOutRight = new VUMeter( 8, 60, "R" );
		
		inCtrl = new InputControls( 40, 60, channel );
		
		trackView = new TrackView( 60 );
		
		setLayout( new FlowLayout() );
		add( inCtrl );
		add( vuIn );
		add( trackView );
		add( send1 );
		add( send2 );
		add( pan );
		add( volume );
		add( vuOutLeft );
		add( vuOutRight );
	}
	
	

}
