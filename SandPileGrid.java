public class SandPileGrid implements Cloneable {
    protected int width;
    protected int height;
    protected int[][] grid;
        
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
        int[][] newGrid = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newGrid[i][j] = grid[i][j];
            }
        }

        while (!isStable()) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int sand = grid[i][j];
                    
                    if (sand >= TOPPLE_SAND) {
                        newGrid[i][j] -= TOPPLE_SAND;
                        
                        if (i > 0) {
                            newGrid[i-1][j]++;
                        }
                        
                        if (i < width - 1) {
                            newGrid[i+1][j]++;
                        }
                        
                        if (j > 0) {
                            newGrid[i][j-1]++;
                        }
                        
                        if (j < height - 1) {
                            newGrid[i][j+1]++;
                        }
                    }
                }
            }
            
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    grid[i][j] = newGrid[i][j];
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

    public int amountSand() {
        int sand = 0;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                sand += grid[i][j];
            }
        }

        return sand;
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