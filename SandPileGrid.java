import java.io.*;
import java.util.concurrent.locks.*;

public class SandPileGrid implements Cloneable {
    protected int width;
    protected int height;
    protected int[][] grid;
    protected int[][] gridQuad;
        
    public static int TOPPLE_SAND = 4;
    
    public SandPileGrid(int width, int height, int[][] grid) {
        if (width <= 0) {
            throw new IllegalArgumentException("width <= 0!");
        }
    
        if (height <= 0) {
            throw new IllegalArgumentException("height <= 0!");
        }
        
        if (!isValidGrid(grid, width, height)) {
            throw new IllegalArgumentException("grid not valid!");
        }
    
        this.width = width;
        this.height = height;
        this.grid = grid;
    }
    
    public SandPileGrid(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width <= 0!");
        }
    
        if (height <= 0) {
            throw new IllegalArgumentException("height <= 0!");
        }
    
        this.width = width;
        this.height = height;
        this.grid = new int[width][height];
    }
    
    public SandPileGrid(int[][] grid) {
        this(grid.length, grid[0].length, grid);
    }
    
    public SandPileGrid() {
        this(5, 5);
    }
    
    public void add(SandPileGrid s, boolean shouldTopple) {
        if (s.getWidth() != width || s.getHeight() != height) {
            throw new IllegalArgumentException("Sand pile grids different sizes!");
        }
    
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] += s.getSand(i, j);
            }
        }
        
        if (shouldTopple) {
            topple();
        }
    }

    public void add(SandPileGrid s) {
        add(s, true);
    }
    
    public void place(int x, int y, int s, boolean shouldTopple) {
        if (s < 0) {
            throw new IllegalArgumentException("sand < 0!");
        }
        
        if (x < 0 || x >= width) {
            throw new IllegalArgumentException("x out of bounds!");
        }
        
        if (y < 0 || y >= height) {
            throw new IllegalArgumentException("y out of bounds!");
        }

        grid[x][y] += s;
        
        if (shouldTopple) {
            topple();
        }
    }
    
    public void place(int x, int y, int s) {
        place(x, y, s, true);
    }

    public void topple(boolean oneStep) {
        if (gridQuad == null || (gridQuad.length != width && gridQuad[0].length != height)) {
            gridQuad = new int[width][height];
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                gridQuad[i][j] = grid[i][j];
            }
        }

        while (!isStable()) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int sand = grid[i][j];
                    
                    if (sand >= TOPPLE_SAND) {
                        gridQuad[i][j] -= TOPPLE_SAND;
                        
                        if (i > 0) {
                            gridQuad[i-1][j]++;
                        }
                        
                        if (i < width - 1) {
                            gridQuad[i+1][j]++;
                        }
                        
                        if (j > 0) {
                            gridQuad[i][j-1]++;
                        }
                        
                        if (j < height - 1) {
                            gridQuad[i][j+1]++;
                        }
                    }
                }
            }
            
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    grid[i][j] = gridQuad[i][j];
                }
            }
            
            if (oneStep) {
                break;
            }
        }
    }
    
    public void topple() {
        topple(false);
    }
    
    public boolean isStable() {
        for (int[] i: grid) {
            for (int j: i) {
                if (j >= TOPPLE_SAND) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void clear() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = 0;
            }
        }
    }
    
    public int getSand(int x, int y) {
        if (x < 0 || x >= width) {
            throw new IllegalArgumentException("x out of bounds!");
        }
        
        if (y < 0 || y >= height) {
            throw new IllegalArgumentException("y out of bounds!");
        }
    
        return grid[x][y];
    }
    
    public void setSand(int x, int y, int s, boolean shouldTopple) {
        if (s < 0) {
            throw new IllegalArgumentException("sand < 0!");
        }
        
        if (x < 0 || x >= width) {
            throw new IllegalArgumentException("x out of bounds!");
        }
        
        if (y < 0 || y >= height) {
            throw new IllegalArgumentException("y out of bounds!");
        }
        
        grid[x][y] = s;
        
        if (shouldTopple) {
            topple();
        }
    }
    
    public void setSand(int x, int y, int s) {
        setSand(x, y, s, true);
    }       
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int w) {
        if (w <= 0) {
            throw new IllegalArgumentException("width <= 0!");
        }
        
        width = w;
        
        grid = new int[width][height];
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int h) {
        if (h <= 0) {
            throw new IllegalArgumentException("height <= 0!");
        }
        
        height = h;
        
        grid = new int[width][height];
    }
    
    public int[][] toArray() {
        int[][] returnGrid = new int[width][height];
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                returnGrid[i][j] = grid[i][j];
            }
        }
        
        return returnGrid;
    }
    
    @Override
    public String toString() {
        int capacity = width * 2 * height + 100; // 100 is fudge
        StringBuilder builder = new StringBuilder(capacity);
        
        char[] markers = {'.', '!', '=', '#'};  // topple sand length
        char small = '?';
        char big = 'X';
        
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int sand = grid[i][j];
                
                if (sand < 0) {        // should never be small
                    builder.append(small);
                }
                else if (sand >= markers.length) {
                    builder.append(big);
                }
                else {
                    builder.append(markers[sand]);
                }
                
                if (i == width - 1 && j != height - 1) {
                    builder.append('\n');
                }
                else {
                    builder.append(' ');
                }
            }
        }
        
        return builder.toString();
    }
    
    @Override
    public int hashCode() {
        int hashCode = width * height;
        
        int minDim = width < height ? width : height;
        for (int i = 0; i < minDim; i++) {
            hashCode += grid[i][i] << (i % 32); // 32 is length of int
        }
        
        return hashCode;
    }           
    
    @Override
    public Object clone() {
        return new SandPileGrid(width, height, toArray());
    }
    
    // Note that two sand pile grids could be equivalent when toppled, but equals return false
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SandPileGrid)) {
            return false;
        }
        
        SandPileGrid other = (SandPileGrid) obj;
        
        if (other.getWidth() != this.getWidth() || other.getHeight() != this.getHeight()) {
            return false;
        }
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (other.getSand(i, j) != this.getSand(i, j)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean isValidGrid(int[][] grid, int width, int height) {
        if (grid.length != width) {
            return false;
        }
    
        for (int i = 0; i < width; i++) {
            if (grid[i].length != height) {
                return false;
            }
            
            for (int j = 0; j < height; j++) {
                if (grid[i][j] < 0) {
                    return false;
                }
            }
        }
        
        return true;
    }
}

class SinglePileSandGrid implements Serializable {
    protected int[][] gridQuad;
    private transient Lock lock;

    public SinglePileSandGrid(int size) {
        gridQuad = new int[size][size];
        lock = new ReentrantLock();
    }

    public void place(int sand) {
        gridQuad[0][0] += sand;
    }

    public void topple(String saveFileName) {
        int width = gridQuad.length, height = gridQuad[0].length;
        int calc_width = 1, calc_height = 1; 
        boolean isStable;
        
        Thread cacheThread = null;
        if (saveFileName != null) {
            final Object obj = this;
            cacheThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            saveToFile(saveFileName);
                            Thread.sleep(200000);
                        }
                        catch (InterruptedException ie) { 
                            break;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        System.out.println("Im just going along...");
                    }
                }
            });
            cacheThread.setDaemon(true);
            cacheThread.start();
        }

        do {
            int new_calc_width = 0, new_calc_height = 0;
            isStable = true;
            
            lock.lock();
            for (int i = 0; i < calc_width; i++) {
                for (int j = 0; j < calc_height; j++) {
                    int sand = gridQuad[i][j];

                    if (sand >= 4) {
                        new_calc_width = Math.max(i+2, new_calc_width);
                        new_calc_height = Math.max(j+2, new_calc_height);
                        new_calc_width = Math.min(new_calc_width, width);
                        new_calc_height = Math.min(new_calc_height, height);
                        isStable = false;

                        gridQuad[i][j] -= sand - (sand & 3);

                        if (i > 1) {
                            gridQuad[i-1][j] += sand >>> 2;
                        }
                        else if (i == 1) {
                            gridQuad[0][j] += sand >>> 2 << 1;
                        }

                        if (i < width - 1) {
                            gridQuad[i+1][j] += sand >>> 2;
                        }

                        if (j > 1) {
                            gridQuad[i][j-1] += sand >>> 2;
                        }
                        else if (j == 1) {
                            gridQuad[i][0] += sand >>> 2 << 1;
                        }

                        if (j < height - 1) {
                            gridQuad[i][j+1] += sand >>> 2;
                        }
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
        } while (!isStable);

        if (saveFileName != null) {
            cacheThread.interrupt();
            try {
                cacheThread.join();
                saveToFile(saveFileName);
            }
            catch (InterruptedException ie) {}
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void topple() {
        topple("sandpile_save.ser");
        // alternatively:
        //topple(null);
    }

    public int amountSand() {
        int sand = 0;

        lock.lock();
        for (int i = 0; i < gridQuad.length; i++) {
            for (int j = 0; j < gridQuad[0].length; j++) {
                sand += gridQuad[i][j];
            }
        }
        lock.unlock();

        return sand;
    }

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
        return gridQuad.length * 2 - 1;
    }

    public int getHeight() {
        return gridQuad[0].length * 2 - 1;
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
        int fullWidth = width * 2 - 1, fullHeight = height * 2 - 1;
        int[][] fullGrid = new int[fullWidth][fullHeight];

        for (int i = 0; i < fullWidth; i++) {
            for (int j = 0; j < fullHeight; j++) {
                int x = Math.abs(i - (width - 1));
                int y = Math.abs(j - (height - 1));

                fullGrid[i][j] = gridQuad[x][y];
            }
        }
        lock.unlock();

        return new SandPileGrid(fullGrid);
    }

    public void saveToFile(String fname) throws IOException {
        FileOutputStream fstream = new FileOutputStream(fname);
        ObjectOutputStream outStream = new ObjectOutputStream(fstream);
        outStream.writeObject(this);
        outStream.close();
        fstream.close();
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
        lock = new ReentrantLock();
    }
}