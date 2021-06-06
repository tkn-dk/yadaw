package dk.yadaw.utils;

public class PeakTimer extends Thread {
	
	private boolean stopped;
	private boolean timerIsSet;
	private boolean eventIsProcessing;
	private int sleepTime;
	
	public PeakTimer() {
		stopped = false;
		timerIsSet = false;
		sleepTime = 1000000;
		start();
	}

	@Override
	public void run() {
		while( !stopped ) {
			try {
				sleep( sleepTime );
				if( timerIsSet ) {
					timerIsSet = false;
					eventIsProcessing = true;
					timerEvent();
				}
			} catch (InterruptedException e) {
			}
		}
	}
	
	/*
	 * Override in subclass
	 */
	public void timerEvent() {
	}
	
	public void setTimer( int ms ) {
		sleepTime = ms;
		timerIsSet = true;
		if( !eventIsProcessing ) {
			interrupt();
		}
	}
	
	public void stopTimer() {
		stopped = true;
		interrupt();
	}
}
