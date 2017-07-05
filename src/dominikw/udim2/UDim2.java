package dominikw.udim2;

public class UDim2 {
	public UDim1 x = new UDim1(0,0);
	public UDim1 y = new UDim1(0,0);
	
	public UDim2(double x_scale,double x_offset,double y_scale,double y_offset) {
		x = new UDim1(x_scale,x_offset);
		y = new UDim1(y_scale,y_offset);
	}
}
