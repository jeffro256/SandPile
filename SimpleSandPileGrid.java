public class SimpleSandPileGrid implements SandPileGrid {
	protected int width;
	protected int height;
	protected int[][] grid;
		
	public static int TOPPLE_SAND = 4;
	
	public SimpleSandPileGrid(int width, int height, int[][] grid) {
		if (width <= 0) {
			throw new IllegalArgumentException("width <= 0!");
		}
	
		if (height <= 0) {
			throw new IllegalArgumentException("height <= 0!");
		}
	
		this.width = width;
		this.height = height;
		this.grid = grid;
	}
	
	public SimpleSandPileGrid(int width, int height) {
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
	
	public SimpleSandPileGrid(int[][] grid) {
		this(grid.length, grid[0].length, grid);
	}
	
	public SimpleSandPileGrid() {
		this(5, 5);
	}
	
	public void add(SandPileGrid s) {
		if (s.getWidth() != width || s.getHeight() != height) {
			throw new IllegalArgumentException("Sand pile grids different sizes!");
		}
	
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				grid[i][j] += s.getSand(i, j);
			}
		}
	}

	public SimpleSandPileGrid plus(SandPileGrid other) {
		SimpleSandPileGrid newGrid = new SimpleSandPileGrid(this.grid);
		newGrid.add(other);

		return newGrid;
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

	@Override
	public long step(long maxSteps) {
		int[][] newGrid = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newGrid[i][j] = grid[i][j];
			}
		}

		long steps = 0;
		while (!isStable() && (maxSteps < 0 || steps < maxSteps)) {
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

			steps++;
		}

		return steps;
	}


	@Override
	public void step() {
		step(1);
	}
	
	@Override
	public long topple() {
		return step(-1);
	}
	
	@Override
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
	
	@Override
	public int getSand(int x, int y) {
		if (x < 0 || x >= width) {
			throw new IllegalArgumentException("x out of bounds!");
		}
		
		if (y < 0 || y >= height) {
			throw new IllegalArgumentException("y out of bounds!");
		}
	
		return grid[x][y];
	}
	
	@Override
	public void setSand(int x, int y, int s) {
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
	}

	@Override
	public long amountSand() {
		long sum = 0;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				sum += grid[i][j];
			}
		}

		return sum;
	}	  
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void resize(int w, int h) {
		if (w <= 0) {
			throw new IllegalArgumentException("width <= 0!");
		}

		if (h <= 0) {
			throw new IllegalArgumentException("height <= 0!");
		}

		width = w;
		height = h;

		grid = new int[width][height];
	}
	
	@Override
	public String toString() {
		int capacity = width * (2 * height + 1) + 100; // 100 is fudge
		StringBuilder builder = new StringBuilder(capacity);
		
		char[] markers = {'.', '!', '=', '#'};  // topple sand length
		char small = '?';
		char big = 'X';
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int sand = grid[i][j];
				
				if (sand < 0) {  // should never be small
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
	
	// Note that two sand pile grids could be equivalent when toppled, but equals return false
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof SimpleSandPileGrid)) {
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
}