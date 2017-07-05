package dominikw.udim2;


import java.awt.*;
import java.util.Hashtable;

public class UDim2Layout implements LayoutManager2 {
	private static Hashtable<Component,Object> constraintsReference = new Hashtable<Component,Object>();
	
	private Dimension calculateComponentPosition(Container c) {
		Object constraints = constraintsReference.getOrDefault(c,null);
		if (constraints != null) {
			if (constraints instanceof UDim2Constraints) {
				UDim2Constraints con = (UDim2Constraints)constraints;
				Dimension parentSize = c.getParent().getSize();
				
				double xPos = con.position.x.offset + parentSize.getWidth() * con.position.x.scale;
				double yPos = con.position.y.offset + parentSize.getHeight() * con.position.y.scale;
				return new Dimension((int)xPos,(int)yPos);
			}
		}
		return new Dimension(0,0);
	}
	
	private Dimension calculateComponentSize(Container c) {
		Object constraints = constraintsReference.getOrDefault(c,null);
		if (constraints != null) {
			if (constraints instanceof UDim2Constraints) {
				UDim2Constraints con = (UDim2Constraints)constraints;
				Dimension parentSize = c.getParent().getSize();
				
				double xSize = con.size.x.offset + parentSize.getWidth() * con.size.x.scale;
				double ySize = con.size.y.offset + parentSize.getHeight() * con.size.y.scale;
				return new Dimension((int)xSize,(int)ySize);
			}
		}
		return new Dimension(100,100);
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		int nComps = parent.getComponentCount();
		for (int i = 0; i < nComps; i++) {
			Component c = parent.getComponent(i);
			Dimension pos = calculateComponentPosition((Container) c);
			Dimension size = calculateComponentSize((Container)c);
			
			c.setBounds(pos.width, pos.height, size.width, size.height);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return calculateComponentSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return calculateComponentSize(parent);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public void addLayoutComponent(Component arg0, Object arg1) {
		constraintsReference.put(arg0, arg1);
	}

	@Override
	public float getLayoutAlignmentX(Container arg0) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container arg0) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container arg0) {
	}

	@Override
	public Dimension maximumLayoutSize(Container arg0) {
		return null;
	}
}