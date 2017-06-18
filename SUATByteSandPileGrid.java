import java.awt.Point;

// Symmetric, Unique Array Toppler, Sand Pile Grid that stores values as bytes 
public class SUATByteSandPileGrid implements SandPileGrid {
	private byte[] grid;
	private int width;
	private int height;
	private Point[] unstableCells;

    public void add(SandPileGrid other) {
    	int minWidth = Math.min(width, other.getWidth());
    	int minHeight = Math.min(height, other.getHeight());

    	for (int i = 0; i < minWidth; i++) {
    		for (int j = 0; j < minHeight; j++) {
    			this.place(other.getSand(i, j));
    		}
    	}
    }

    public SandPileGrid plus(SandPileGrid other) {
    	SimpleSandPileGrid result = new SimpleSandPileGrid();

    	int minWidth = Math.min(width, other.getWidth());
    	int minHeight = Math.min(height, other.getHeight());
    	
    	for (int i = 0; i < minWidth; i++) {
    		for (int j = 0; j < minHeight; j++) {
    			result.place(this.getSand(i, j) + other.getSand(i, j));
    		}
    	}

    	return result;
    }

    public long topple();
    public long step(long maxSteps);
    public void step();
    public boolean isStable();

    public int getSand(int x, int y);
    public void setSand(int sand, int x, int y);
    public void place(int sand, int x, int y);
    public long amountSand();
    public void fill(int sand);
    public void clear();

    public int getWidth();
    public int getHeight();
    public void setWidth(int width);
    public void setHeight(int height);
    public void setSize(int width, int height);
}