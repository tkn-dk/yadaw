package dk.yadaw.widgets;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.awt.Component;
import java.awt.Dimension;

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
	private Collection<Component> panelComponents;
	
	public TrackPanel( int channel ) {
		panelComponents = new ArrayList<Component>();
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
				
		panelComponents.add( inCtrl );
		panelComponents.add( vuIn );
		panelComponents.add( trackView );
		panelComponents.add( send1 );
		panelComponents.add( send2 );
		panelComponents.add( pan );
		panelComponents.add( volume );
		panelComponents.add( vuOutLeft );
		panelComponents.add( vuOutRight );
		
		for( Component c : panelComponents ) {
			add( c );
		}
		
	}
	
	public void resizeTrackView() {
		int pWidth = getWidth();
		int tvWidth = pWidth - 70;
		for( Component c : panelComponents ) {
			if( c != trackView ) {
				tvWidth -= c.getWidth();
			}
		}
		int tvHeight = trackView.getHeight();
		
		System.out.println( "pWidth: " + pWidth + ", tvHeight: " + tvHeight + ", tvWidth: " + tvWidth );
		Dimension newDim = new Dimension( tvWidth, tvHeight );
		trackView.setPreferredSize( newDim );
	}
	
	

}
