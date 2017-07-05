import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Line.Info;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dominikw.basicGui.*;
import dominikw.measureGui.*;
import dominikw.udim2.*;

public class Main {
	//Static config options
	private static int bufferSize = 4400;
	private static int maxAmpl = 16384;
	
	//Sound data line objects
	private static TargetDataLine soundInput = null;
	
	//GUI element objects
	private static JProgressBar volumeBarL;
	private static JProgressBar volumeBarR;
	private static Oscilloscope oscL;
	private static Oscilloscope oscR;
	private static JCheckBox checkboxTriggerEnabled;
	private static JComboBox<String> triggerSourceSelect;
	private static JSlider sliderTriggerLevel;
	private static JComboBox<String> triggerTypeSelect;
	private static JSlider showLengthSlider;
	private static JSlider sensitivitySlider;
	
	//Configuration options
	private static int oscShowLength = bufferSize;
	private static double sensitivity = 1;
	private static boolean triggerEnabled = false;
	private static int triggerSource = 0; //0 = left 1 = right
	private static int triggerLevel = 0;
	private static int triggerType = 1; //0 = either edge 1 = rising edge 2 = falling edge
	
	//Sound processing objects
	private static double volumeL = 0;
	private static double volumeR = 0;
	private static int oscSampleId = 0;
	
	public static void main(String[] args) {
		String audioSource = "Stereo Mix";
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-bufferSize")) {
				try {
					bufferSize = Integer.parseInt(args[i+1]);
					System.out.println("Set buffer size to " + bufferSize + " samples from command line");
				} catch (Exception e) {
					System.err.println("Failed to set buffer size from parameter");
				}
			}
			
			if (args[i].equals("-maxAmpl")) {
				try {
					maxAmpl = Integer.parseInt(args[i+1]);
					System.out.println("Set maximum amplitude to " + maxAmpl + " from command line");
				} catch (Exception e) {
					System.err.println("Failed to set maximum amplitude from parameter");
				}
			}
			
			if (args[i].equals("-source")) {
				try {
					audioSource = args[i+1];
					System.out.println("Set audio source to " + audioSource + " from command line");
				} catch (Exception e) {
					System.err.println("Failed to set audio source from parameter");
				}
			}
		}
		
		oscShowLength = bufferSize;
		
		//Setting up the UI
		JFrame frame = new JFrame("Audio Tools");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(800,600));
		frame.setLayout(new UDim2Layout());
		
		frame.add(new JLabel("Left Audio Channel"),new UDim2Constraints(new UDim2(0,10,0,5),new UDim2(0.5,-20,0,20)));
		frame.add(new JLabel("Right Audio Channel"),new UDim2Constraints(new UDim2(0.5,10,0,5),new UDim2(0.5,-20,0,20)));
		
		volumeBarL = new JProgressBar(JProgressBar.VERTICAL,0,maxAmpl);
		volumeBarR = new JProgressBar(JProgressBar.VERTICAL,0,maxAmpl);
		
		frame.add(volumeBarL,new UDim2Constraints(new UDim2(0,10,0,30),new UDim2(0,20,0,140)));
		frame.add(volumeBarR,new UDim2Constraints(new UDim2(0.5,10,0,30),new UDim2(0,20,0,140)));
		
		oscL = new Oscilloscope(bufferSize/2,-maxAmpl,maxAmpl);
		oscR = new Oscilloscope(bufferSize/2,-maxAmpl,maxAmpl);
		
		frame.add(oscL,new UDim2Constraints(new UDim2(0,40,0,30),new UDim2(0.5,-50,0,140)));
		frame.add(oscR,new UDim2Constraints(new UDim2(0.5,40,0,30),new UDim2(0.5,-50,0,140)));
		
		//Create trigger options
		Section triggerSection = new Section("Trigger Options");
		frame.add(triggerSection,new UDim2Constraints(new UDim2(0,10,0,200),new UDim2(0.5,-20,0,90)));
		
		checkboxTriggerEnabled = new JCheckBox("Enable Triggering");
		triggerSourceSelect = new JComboBox<String>();
		triggerSourceSelect.addItem("Trigger on Left");
		triggerSourceSelect.addItem("Trigger on Right");
		sliderTriggerLevel = new JSlider(-maxAmpl,maxAmpl,0);
		sliderTriggerLevel.setToolTipText("Trigger Level");
		triggerTypeSelect = new JComboBox<String>();
		triggerTypeSelect.addItem("Edge");
		triggerTypeSelect.addItem("Rising Edge");
		triggerTypeSelect.addItem("Falling Edge");
		triggerTypeSelect.setSelectedIndex(1);
		
		triggerSection.setLayout(new UDim2Layout());
		triggerSection.add(checkboxTriggerEnabled,new UDim2Constraints(new UDim2(0,20,0,20),new UDim2(0.5,-20,0,20)));
		triggerSection.add(triggerSourceSelect,new UDim2Constraints(new UDim2(0.5,10,0,20),new UDim2(0.5,-30,0,20)));
		triggerSection.add(sliderTriggerLevel,new UDim2Constraints(new UDim2(0,20,0,50),new UDim2(0.5,-20,0,20)));
		triggerSection.add(triggerTypeSelect,new UDim2Constraints(new UDim2(0.5,10,0,50),new UDim2(0.5,-30,0,20)));
		
		checkboxTriggerEnabled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { triggerEnabled = checkboxTriggerEnabled.isSelected(); } 
		});
		
		triggerSourceSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { triggerSource = triggerSourceSelect.getSelectedIndex(); }
		});
		
		sliderTriggerLevel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) { triggerLevel = sliderTriggerLevel.getValue(); }
		});
		
		//Create Oscilloscope settings
		Section oscSettings = new Section("Oscilloscope Settings");
		frame.add(oscSettings,new UDim2Constraints(new UDim2(0.5,10,0,200),new UDim2(0.5,-20,0,90)));
		
		showLengthSlider = new JSlider(1,bufferSize,bufferSize);
		sensitivitySlider = new JSlider(0,5000,1000);
		
		oscSettings.setLayout(new UDim2Layout());
		oscSettings.add(new JLabel("Signal Length"),new UDim2Constraints(new UDim2(0,20,0,20),new UDim2(0,100,0,20)));
		oscSettings.add(new JLabel("Sensitivity"),new UDim2Constraints(new UDim2(0,20,0,50),new UDim2(0,100,0,20)));
		oscSettings.add(showLengthSlider,new UDim2Constraints(new UDim2(0,120,0,20),new UDim2(1,-140,0,20)));
		oscSettings.add(sensitivitySlider,new UDim2Constraints(new UDim2(0,120,0,50),new UDim2(1,-140,0,20)));
		
		showLengthSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) { oscShowLength = showLengthSlider.getValue(); }
		});
		
		sensitivitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) { sensitivity = sensitivitySlider.getValue()/1000d; }
		});
		
		//Create the window
		frame.setVisible(true);
		
		//Setting up all the audio stuff
		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		Mixer mixer = null;
		for (int i = 0; i < mixers.length; i++) {
			if (mixers[i].getName().contains(audioSource)) {
				System.out.println("Found audio device: " + mixers[i].getName());
				mixer = AudioSystem.getMixer(mixers[i]);
				break;
			}
		}
		
		if (mixer == null) {
			System.err.println("Could not find audio device for " + audioSource);
			System.exit(-1);
		}
		
		Info[] infos = mixer.getTargetLineInfo();
		
		try {
			AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
			soundInput = (TargetDataLine)mixer.getLine(infos[0]);
			soundInput.open(audioFormat, bufferSize*2);
		} catch (LineUnavailableException e) {
			System.err.println("Failed to get audio input data line");
			System.exit(-1);
		}
		soundInput.start();
		
		Thread soundHandler = new Thread(new Runnable() {
			@Override
			public void run() {
				//Oscilloscope helper values
				int lastSample = 0;
				
				//Main loop
				while (true) {
					byte[] dataIn = new byte[bufferSize*2];
					soundInput.read(dataIn,0,bufferSize*2);
					
					volumeL = 0;
					volumeR = 0;
					for (int i = 0; i < bufferSize; i += 2) {
						int byte0 = (int)dataIn[i*2];
						int byte1 = (int)dataIn[i*2 + 1] << 8;
						int byte2 = (int)dataIn[i*2 + 2];
						int byte3 = (int)dataIn[i*2 + 3] << 8;
						
						if (byte0 < 0) byte0 += 256;
						if (byte2 < 0) byte2 += 256;
						
						int valueL = (int)((byte0 | byte1)*sensitivity);
						int valueR = (int)((byte2 | byte3)*sensitivity);
						
						volumeL = Math.max(volumeL,Math.abs(valueL));
						volumeR = Math.max(volumeR,Math.abs(valueR));
						
						if (triggerEnabled) {
							if (oscSampleId < bufferSize/2) {
								oscL.setSample(oscSampleId,valueL);
								oscR.setSample(oscSampleId,valueR);
								oscSampleId ++;
							} else {
								int value = 0;
								if (triggerSource == 0) { value = valueL; } else { value = valueR; }
								
								if ((triggerType == 0 && ((lastSample < triggerLevel && value >= triggerLevel) || (lastSample > triggerLevel && value <= triggerLevel)))
										|| (triggerType == 1 && lastSample < triggerLevel && value >= triggerLevel)
										|| (triggerType == 2 && lastSample > triggerLevel && value <= triggerLevel)) {
									oscSampleId = 0;
								}
								
								lastSample = value;
							}
						} else {
							oscL.setSample(i/2,valueL);
							oscR.setSample(i/2,valueR);
						}
					}
					
					volumeBarL.setValue((int)volumeL);
					volumeBarR.setValue((int)volumeR);
					
					if (triggerEnabled) {
						oscL.setTriggerLevel(triggerLevel);
						oscR.setTriggerLevel(triggerLevel);
					} else {
						oscL.setTriggerLevel(0);
						oscR.setTriggerLevel(0);
					}
					
					oscL.setShowLength(oscShowLength);
					oscR.setShowLength(oscShowLength);
					
					oscL.repaint();
					oscR.repaint();
				}
			}
		});
		soundHandler.start();
	}
}
