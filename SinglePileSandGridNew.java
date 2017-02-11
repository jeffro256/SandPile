import java.io.*;
import java.util.concurrent.locks.*;

public class SinglePileSandGridNew implements Serializable {
    protected int[][] gridQuad;
    private transient ReentrantLock lock;
    private volatile transient boolean toppling;

    private static final long serialVersionUID = 8379211264561215666L;
    private static final boolean fairLock = true;

    public SinglePileSandGridNew(int size) {
        gridQuad = new int[size][size];
        lock = new ReentrantLock(fairLock);
        toppling = false;
    }

    public void place(int sand) {
        gridQuad[1][1] += sand;
    }

    public long topple() {
        int width = gridQuad.length, height = gridQuad[0].length;
        int calc_width = 1, calc_height = 1; 
        boolean isStable;

        int loops = 0;
        toppling = true;
        do {
            int new_calc_width = 0, new_calc_height = 0;
            isStable = true;
            
            lock.lock();
            for (int i = 1; i < calc_width; i++) {
                for (int j = 1; j < calc_height; j++) {
                    final int sand = gridQuad[i][j];
                    final int splitSand = sand >>> 2;

                    if (sand >= 4) {
                        new_calc_width = Math.max(i+1, new_calc_width);
                        new_calc_height = Math.max(j+1, new_calc_height);
                        new_calc_width = Math.min(new_calc_width, width - 2);
                        new_calc_height = Math.min(new_calc_height, height - 2);
                        isStable = false;

                        gridQuad[i][j] &= 3;

                        if (i == 1) {
                            gridQuad[0][j] += splitSand;
                        }
                        
                        gridQuad[i-1][j] += splitSand;
                        gridQuad[i+1][j] += splitSand;

                        if (j == 1) {
                            gridQuad[i][0] += splitSand;
                        }

                        gridQuad[i][j-1] += splitSand;
                        gridQuad[i][j+1] += splitSand;
                    }
                }
            }
            lock.unlock();

            calc_width = new_calc_width;
            calc_height = new_calc_height;

            if (calc_width >= width || calc_height >= height) {
                resize(width * 2, height * 2);
                width *= 2;
                height *= 2;
            }

            loops++;
        } while (!isStable && toppling);

        toppling = false;

        return loops;
    }

    public void stopTopple() {
        toppling = false;
    }

    public int amountSand() {
        int sand = 0;

        lock.lock();
        for (int i = 1; i < gridQuad.length; i++) {
            for (int j = 1; j < gridQuad[0].length; j++) {
                sand += gridQuad[i][j];
            }
        }
        lock.unlock();

        return sand;
    }

    // @TODO: Work with edges?
    public void trim() {
        lock.lock();
        int width = gridQuad.length, height = gridQuad[0].length;
        int maxX = 1, maxY = 1;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (gridQuad[i][j] >= 4) {
                    maxX = Math.max(i, maxX);
                    maxY = Math.max(j, maxY);
                }
            }
        }

        int[][] newGrid = new int[maxX][maxY];

        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                newGrid[i][j] = gridQuad[i][j];
            }
        }

        gridQuad = newGrid;

        lock.unlock();
    }

    public int getWidth() {
        return (gridQuad.length - 3) * 2 + 1;
    }

    public int getHeight() {
        return (gridQuad[0].length - 3) * 2 + 1;
    }

    public void resize(int w, int h) {
        lock.lock();

        int[][] newGrid = new int[w][h];

        int width = Math.min(gridQuad.length, w);
        int height = Math.min(gridQuad[0].length, h);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newGrid[i][j] = gridQuad[i][j];
            }
        }

        gridQuad = newGrid;

        lock.unlock();
    }

    public int[][] getGrid() {
        return gridQuad;
    }

    public SandPileGrid toSandPileGrid() {
        lock.lock();
        int width = gridQuad.length, height = gridQuad[0].length;
        int fullWidth = (width - 3) * 2 + 1, fullHeight = (height - 3) * 2 + 1;
        int[][] fullGrid = new int[fullWidth][fullHeight];

        for (int i = 0; i < fullWidth; i++) {
            for (int j = 0; j < fullHeight; j++) {
                int x = Math.abs(i - width);
                int y = Math.abs(j - height);

                fullGrid[i][j] = gridQuad[x][y];
            }
        }
        lock.unlock();

        return new SandPileGrid(fullGrid);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        lock.lock();
        try {
            out.writeObject(gridQuad);
        } finally {
            lock.unlock();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        gridQuad = (int[][]) in.readObject();
        lock = new ReentrantLock(fairLock);
    }
}