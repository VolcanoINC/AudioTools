package dominikw.measureGui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class Oscilloscope extends JPanel {
	private static final long serialVersionUID = 1L;
	private int bufferSize = 0;
	private int showLength = 0;
	private double[] data;
	private double min = 0;
	private double max = 0;
	private double triggerLevel = 0;
	
	public Oscilloscope(int bufferSize,double min,double max) {
		this.bufferSize = bufferSize;
		this.showLength = bufferSize;
		this.min = min;
		this.max = max;
		
		this.data = new double[bufferSize];
		
		for (int i = 0; i < bufferSize; i++) {
			data[i] = 0;
		}
	}
	
	public void setSample(int index,double sample) {
		data[index] = sample;
	}
	
	public void setTriggerLevel(double level) {
		triggerLevel = level;
	}
	
	public void setShowLength(int showLength) {
		if (showLength > bufferSize) showLength = bufferSize;
		this.showLength = showLength;
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.GRAY);
		g.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
		
		if (triggerLevel != 0) {
			g.setColor(Color.RED);
			int y = getHeight()-(int)((double)getHeight()*(triggerLevel-min)/(max-min));
			g.drawLine(0, y, getWidth(), y);
		}
		
		g.setColor(Color.GREEN);
		for (int i = 1; i < showLength; i++) {
			int x0 = (int)((double)getWidth()*(double)(i-1)/(double)showLength);
			int x1 = (int)((double)getWidth()*(double)i/(double)showLength);
			
			int y0 = getHeight()-(int)((double)getHeight()*(data[i-1]-min)/(max-min));
			int y1 = getHeight()-(int)((double)getHeight()*(data[i]-min)/(max-min));
			
			g.drawLine(x0, y0, x1, y1);
		}
	}
}
