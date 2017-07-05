package dominikw.basicGui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

public class Section extends JPanel {
	private static final long serialVersionUID = 1L;
	private String name = "Section";
	
	public Section(String name) {
		this.name = name;
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(new Color(150,150,150));
		g.drawRect(5, 5, getWidth()-10, getHeight()-10);
		
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(name);
		
		g.setColor(new Color(0xEEEEEE));
		g.fillRect(15, 0, width+10, 20);
		
		g.setColor(new Color(0x222222));
		g.drawString(name, 20, 10);
		
		paintChildren(g);
	}
}
