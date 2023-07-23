package dk.yadaw.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;

import javax.swing.JFrame;

import dk.yadaw.audio.Asio;
import dk.yadaw.audio.AudioStream;
import dk.yadaw.audio.AudioTrack;
import dk.yadaw.audio.MixerChannel;
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
public class TrackController implements PotentiometerListener, TrackNameColorListener {
	private final int viewEndSamplePos = 2 * 60 * 48000;
	private MixerChannel mixerChannel;
	private TrackPanel trackPanel;
	private AudioTrack audioTrack;
	private long samplePos;
	private AudioStream mixerInStream;
	private AudioStream trackFileStream;
	private AudioStream trackViewRecordStream;
	private AudioStream recordingStream;
	private String trackName;
	private boolean isRecording;
	private long lastPeakPos;
	private TrackViewData trackViewData;
	private JFrame owner;
	private int bufferSize;

	public TrackController(JFrame owner, String trackName, MixerChannel channel, TrackPanel panel) {
		this.owner = owner;
		mixerChannel = channel;
		trackPanel = panel;
		this.trackName = trackName;
		audioTrack = new AudioTrack();
		mixerInStream = new AudioStream("TrackPanel " + trackName + " inStream ");
		trackFileStream = new AudioStream("TrackPanel " + trackName + " trackSplit ");
		recordingStream = new AudioStream("TrackPanel " + trackName + "recordingMixerInStream");
		trackViewRecordStream = new AudioStream("TrackPanel " + trackName + " trackViewRecordStream");
		panel.getVolume().addPotentiometerListener(this);
		panel.getPan().addPotentiometerListener(this);
		trackViewData = new TrackViewData();
		updateTrackView();
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
		return mixerInStream;
	}

	public String getTrackName() {
		return trackName;
	}

	public Color getTrackColor() {
		return trackPanel.getTrackColor();
	}

	public void inputLabelClick() {
		new TrackNameColorDlg(owner, trackName, trackPanel.getTrackColor(), this);
	}

	public void setPlaybackOrRecord(Asio asio) {
		isRecording = trackPanel.isRecording();
		mixerChannel.setIn(mixerInStream);
		bufferSize = asio.getBufferSize();

		if (isRecording) {
			audioTrack.setInput(trackFileStream);
			int trackViewWidth = trackPanel.getTrackView().getWidth();
			if (trackViewWidth > 0) {
				trackViewData.setPeakWindow(0, 0, viewEndSamplePos / trackViewWidth);
				trackViewData.setRecordStream(trackViewRecordStream);
			} else {
				System.out.println("TrackController.setPlaybackOrRecord - Error: trackViewWidth is 0");
			}

			try {
				audioTrack.recordStart(trackName + ".raw");
			} catch (IOException e) {
				System.out.println("Could not record to " + trackName);
			}
			asio.connectOutput(mixerChannel.getChannelNumber(), recordingStream);
		} else {
			audioTrack.setInput(null);
			audioTrack.setOutput(mixerInStream);
			try {
				audioTrack.playbackStart(trackName + ".raw", false);
			} catch (IOException e) {
				System.out.println("Could not playback from " + trackName);
			}
		}
	}

	public void trackSync(long newSamplePos) {
		int deltaPos = (int) (newSamplePos - samplePos);
		samplePos = newSamplePos;
		recordingStream.sync(newSamplePos);
		
		if (isRecording) {
			int toRead = Math.min(deltaPos, 8 * bufferSize);
			while (!recordingStream.isEmpty() && toRead > 0) {
				int sample = recordingStream.read();
				mixerInStream.write(sample);
				trackFileStream.write(sample);
				trackViewRecordStream.write(sample);
				toRead--;
			}

			mixerChannel.audioSync(newSamplePos);
			trackViewData.recordAudioSync(newSamplePos);
			audioTrack.audioSync(newSamplePos);

			trackFileStream.sync(newSamplePos);
			trackViewRecordStream.sync(newSamplePos);
		} else {
			audioTrack.audioSync(newSamplePos);
			mixerChannel.audioSync(newSamplePos);
		}
		mixerInStream.sync(newSamplePos);

		// VU meter update
		int deltaPeakPos = (int) (newSamplePos - lastPeakPos);
		if (deltaPeakPos > 2400) {
			vuUpdate(mixerInStream, trackPanel.getInVUMeter(), deltaPeakPos);
			vuUpdate(mixerChannel.getMasterLeft(), trackPanel.getOutLeftVUMeter(), deltaPeakPos);
			vuUpdate(mixerChannel.getMasterRight(), trackPanel.getOutRightVUMeter(), deltaPeakPos);

			if (trackPanel.isRecording()) {
				trackPanel.getTrackView().setTrackPeaks(0, newSamplePos, trackViewData.getPeakArray());
			}
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
		trackName = newName;
		trackPanel.setTrackName(newName);
		updateTrackView();
	}

	private void updateTrackView() {
		TrackView tView = trackPanel.getTrackView();
		Dimension viewDim = tView.getPreferredSize();
		int trackViewWidth = viewDim.width;
		tView.setViewWindow(0, viewEndSamplePos);

		long nofSamples = trackViewData.getNofSamples(trackName);
		trackViewData.setPeakWindow(0, nofSamples, viewEndSamplePos / trackViewWidth);
		if (trackViewData.loadTrackFile(trackName)) {
			int[] trackPeaks = trackViewData.getPeakArray();
			System.out.println("TrackController - trackPeaks: " + trackPeaks.length);
			tView.setTrackPeaks(0, nofSamples, trackPeaks);
		}

	}
}
