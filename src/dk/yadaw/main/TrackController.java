package dk.yadaw.main;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JFrame;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AudioStream;
import dk.yadaw.audio.AudioTrack;
import dk.yadaw.audio.MixerChannel;
import dk.yadaw.audio.SyncListener;
import dk.yadaw.utils.TrackViewData;
import dk.yadaw.widgets.Potentiometer;
import dk.yadaw.widgets.PotentiometerListener;
import dk.yadaw.widgets.TrackNameColorDlg;
import dk.yadaw.widgets.TrackNameColorListener;
import dk.yadaw.widgets.TrackPanel;
import dk.yadaw.widgets.TrackView;
import dk.yadaw.widgets.VUMeter;

/**
 * Controller for on track/mixerchannel.
 * 
 * @author tkn
 *
 */
public class TrackController implements SyncListener, PotentiometerListener, TrackNameColorListener {
	private MixerChannel mixerChannel;
	private TrackPanel trackPanel;
	private AudioTrack audioTrack;
	private long samplePos;
	private AudioStream inStream;
	private AudioStream trackSplit;
	private AudioStream mixerSplit;
	private String trackName;
	private boolean isRecording;
	private long lastPeakPos;
	private TrackViewData trackViewData;
	private JFrame owner;

	public TrackController(JFrame owner, String trackName, MixerChannel channel, TrackPanel panel) {
		this.owner = owner;
		mixerChannel = channel;
		trackPanel = panel;
		this.trackName = trackName;
		audioTrack = new AudioTrack();
		inStream = new AudioStream("TrackPanel " + trackName + " inStream ");
		trackSplit = new AudioStream("TrackPanel " + trackName + " trackSplit ");
		mixerSplit = new AudioStream("TrackPanel " + trackName + " mixerSplit ");
		panel.getVolume().addPotentiometerListener(this);
		panel.getPan().addPotentiometerListener(this);

		trackViewData = new TrackViewData(trackName);
		TrackView tView = trackPanel.getTrackView();
		int viewEndPos = 2 * 60 * 48000; // Initially set to 4 minutes
		tView.setViewWindow(0, viewEndPos);
		long nofSamples = trackViewData.getNofSamples();
		int[] trackPeaks = trackViewData.getPeakArray(0, nofSamples, viewEndPos / 800);
		if (trackPeaks != null) {
			tView.setTrackPeaks(0, nofSamples, trackPeaks);
		}
	}

	public void setPlaybackOrRecord(Asio asio) {
		isRecording = trackPanel.getRecordState();
		mixerChannel.setIn(inStream);
		if (isRecording) {
			mixerChannel.setIn(mixerSplit);
			audioTrack.setInput(trackSplit);
			try {
				audioTrack.recordStart(trackName + ".raw");
			} catch (IOException e) {
				System.out.println("Could not record to " + trackName);
			}
			inStream.addSyncListener(this);
			asio.connectOutput(mixerChannel.getChannelNumber(), inStream);
		} else {
			audioTrack.setInput(null);
			audioTrack.setOutput(inStream);
			try {
				audioTrack.playbackStart(trackName + ".raw", false);
			} catch (IOException e) {
				System.out.println("Could not playback from " + trackName);
			}
		}
	}

	public boolean isRecording() {
		return isRecording;
	}

	public TrackPanel getPanel() {
		return trackPanel;
	}

	public AudioTrack getTrack() {
		return audioTrack;
	}

	public MixerChannel getMixerChannel() {
		return mixerChannel;
	}

	public AudioStream getInpuStream() {
		return inStream;
	}

	public String getTrackName() {
		return trackName;
	}

	public Color getTrackColor() {
		return trackPanel.getTrackColor();
	}

	public void inputLabelClick() {
		TrackNameColorDlg tncDlg = new TrackNameColorDlg(owner, trackName, trackPanel.getTrackColor(), this);
	}

	@Override
	public void audioSync(long newSamplePos) {
		int deltaPos = (int) (newSamplePos - samplePos);
		samplePos = newSamplePos;
		if (isRecording) {
			for (int n = 0; n < deltaPos; n++) {
				int sample = inStream.read();
				trackSplit.write(sample);
				mixerSplit.write(sample);
			}

			trackSplit.sync(newSamplePos);
			mixerSplit.sync(newSamplePos);
		} else {
			inStream.sync(newSamplePos);
			audioTrack.audioSync(newSamplePos);
			mixerChannel.audioSync(newSamplePos);
		}

		// VU meter update
		int deltaPeakPos = (int) (newSamplePos - lastPeakPos);
		if (deltaPeakPos > 2400) {
			vuUpdate(inStream, trackPanel.getInVUMeter(), deltaPeakPos);
			vuUpdate(mixerChannel.getMasterLeft(), trackPanel.getOutLeftVUMeter(), deltaPeakPos);
			vuUpdate(mixerChannel.getMasterRight(), trackPanel.getOutRightVUMeter(), deltaPeakPos);

			trackPanel.getTrackView().setSamplePos(newSamplePos);
			lastPeakPos = newSamplePos;
		}

	}

	private void vuUpdate(AudioStream stream, VUMeter vum, int peakLength) {
		double peakLvl = stream.peak(peakLength);
		double vuLevel = vum.getMin();
		double logArg = peakLvl / 0x7fffff00;
		if (logArg > 0.001) {
			vuLevel = 20 * Math.log(logArg) + vum.getMax();
		}
		vum.setVal((int) vuLevel);
	}

	@Override
	public void potentiometerUpdate(Potentiometer pot) {
		int value = pot.getValue();
		switch (pot.getLabel()) {
		case "Vol":
			mixerChannel.setMasterGain((0x7fff0000 / pot.getMax()) * value);
			break;

		case "Pan":
			mixerChannel.setPan(value, pot.getMax());
			break;

		default:
			break;

		}

	}

	@Override
	public void trackColorUpdated(Color newColor) {
		trackPanel.setTrackColor(newColor);
	}

	@Override
	public void trackNameUpdated(String newName) {
		trackPanel.setTrackName(newName);
	}
}
