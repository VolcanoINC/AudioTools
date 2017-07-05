package dominikw.udim2;

public class UDim2Constraints {
	public UDim2 position = new UDim2(0,0,0,0);
	public UDim2 size = new UDim2(0,100,0,100);
	
	public UDim2Constraints(UDim2 position,UDim2 size) {
		this.position = position;
		this.size = size;
	}
}
