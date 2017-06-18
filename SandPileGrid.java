import java.io.Serializable;

// equals, clone
public interface SandPileGrid {
    public void add(SandPileGrid other);
    public SandPileGrid plus(SandPileGrid other);

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