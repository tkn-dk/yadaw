package dk.yadaw.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;

import dk.yadaw.datamodel.DataModelInstance;
import dk.yadaw.datamodel.YadawDataModel;
import dk.yadaw.widgets.SelectAsioDlg;
import dk.yadaw.widgets.TrackPanel;
import dk.yadaw.widgets.ViewAudioParmsDlg;

public class YadawFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private ArrayList<TrackPanel> trackPanels;
	private ArrayList<TrackPanel> consolidatedPanelAdditions;
	private YadawDataModel model;
	private JScrollPane scrollPane;
	private JPanel mixerPanel;

	public YadawFrame() {
		super("Yadaw");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.NORMAL);
		Insets insets = getToolkit().getScreenInsets(getGraphicsConfiguration());

		setLayout(new BorderLayout());

		mixerPanel = new JPanel(new GridLayout(0, 1));
		scrollPane = new JScrollPane(mixerPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		trackPanels = new ArrayList<TrackPanel>();
		consolidatedPanelAdditions = new ArrayList<TrackPanel>();
		model = DataModelInstance.getModelInstance();

		JMenuBar menuBar = new JMenuBar();

		// File menu
		JMenu fileMenu = new JMenu("File");
		JMenuItem fileSave = new JMenuItem("Save...");
		JMenuItem fileLoad = new JMenuItem("Load...");
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(fileSave);
		fileMenu.add(fileLoad);
		fileMenu.add(fileExit);
		menuBar.add(fileMenu);

		// Audio menu
		JFrame thisFrame = this;
		JMenu audioMenu = new JMenu("Audio");
		JMenuItem audioSelectInterface = new JMenuItem("Open Interface");
		audioSelectInterface.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Open asio dialog");
				SelectAsioDlg dlg = new SelectAsioDlg(thisFrame, DataModelInstance.getModelInstance());
			}
		});

		JMenuItem audioSettings = new JMenuItem("Audio Settings");
		audioSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Audio settings action ");
				ViewAudioParmsDlg dlg = new ViewAudioParmsDlg(thisFrame);
			}
		});

		JMenuItem audioAddTrack = new JMenuItem("Add track");
		audioAddTrack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Add track ");
				model.setUIOperation(YadawDataModel.UIOperation.UI_ADD_TRACK);
			}
		});

		JMenuItem audioStart = new JMenuItem("Start");
		audioStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Start");
				model.setUIOperation(YadawDataModel.UIOperation.UI_AUDIO_START);
			}
		});

		JMenuItem audioStop = new JMenuItem("Stop");
		audioStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Stop");
				model.setUIOperation(YadawDataModel.UIOperation.UI_AUDIO_STOP);

			}
		});

		audioMenu.add(audioSelectInterface);
		audioMenu.add(audioSettings);
		audioMenu.add(audioAddTrack);
		audioMenu.add(audioStart);
		audioMenu.add(audioStop);
		menuBar.add(audioMenu);

		setJMenuBar(menuBar);
		add(scrollPane);
		setSize(new Dimension(400, 300));
		setLocationRelativeTo(null);
		setVisible(true);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				if (e.getComponent() instanceof JFrame) {
					JFrame frame = (JFrame) e.getComponent();

					if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
						System.out.println("Maximized - does not work right now.");
					}
					
					Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
					Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
					Rectangle frameRect = frame.getBounds();
					int adjustY = screen.height - (frameRect.y + frameRect.height) - ins.bottom;
					if (adjustY < 0) {
						frameRect.height += adjustY;
						frame.setBounds(frameRect);
					}

					for (TrackPanel tp : trackPanels) {
						tp.resizeTrackView();
					}
					super.componentResized(e);
				}
			}

		});
	}

	public void newConsolidatedPanel() {
		consolidatedPanelAdditions.clear();
	}

	public void addConsolidatedPanel(TrackPanel tp) {
		consolidatedPanelAdditions.add(tp);
	}

	public void commitConsolidatedPanels() {
		SwingUtilities.invokeLater(() -> {
			int maxWidth = 0;
			int trackPanelHeight = 0;
			for (TrackPanel tp : consolidatedPanelAdditions) {
				trackPanels.add(tp);
				mixerPanel.add(tp);
				if (tp.getWidth() > maxWidth) {
					maxWidth = tp.getWidth();
					trackPanelHeight = tp.getHeight();
				}
			}
			mixerPanel.setSize(new Dimension(maxWidth, trackPanels.size() * trackPanelHeight));
			mixerPanel.revalidate();
//			Dimension dim = getSize();
//			sizeAndPlace(dim.width, dim.height);
			pack();
			setLocationRelativeTo(null);
//			revalidate();
//			repaint();
		});
	}

	public void deletePanel(String label) {
		for (TrackPanel tp : trackPanels) {
			if (tp.getTrackName().equals(label)) {
				trackPanels.remove(tp);
				break;
			}
		}
	}

}
