public interface SandPileGrid {
    public long topple();
    public long step(long maxSteps);
    public void step();
    public boolean isStable();

    public int getSand(int x, int y);
    public void setSand(int sand, int x, int y);
    public long amountSand();

    public int getWidth();
    public int getHeight();
    public void resize(int width, int height);
}