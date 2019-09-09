package com.jeffaryan.sandpile;

import java.io.*;
import java.util.concurrent.locks.*;

public class SinglePileSandGrid implements Serializable {
	protected int[][] gridQuad;
	private transient ReentrantLock lock;
	private volatile transient boolean toppling;
	//private transient int calc_width, calc_height;

	private static final long serialVersionUID = 8309200284561310866L;
	private static final boolean fairLock = true;

	public SinglePileSandGrid(int size) {
		gridQuad = new int[size][size];
		lock = new ReentrantLock(fairLock);
		toppling = false;
	}

	public void place(int sand) {
		gridQuad[0][0] += sand;
	}

	public long topple(boolean printProgess) {
		int width = gridQuad.length, height = gridQuad[0].length;
		boolean isStable;

		int calc_width = width;
		int calc_height = height;

		if (printProgess) {
			int critSand = amountCriticalSand();
			System.out.print(String.valueOf(critSand) + "\r");
		}

		int loops = 0;
		toppling = true;
		do {
			int new_calc_width = 0, new_calc_height = 0;
			isStable = true;
			
			//lock.lock();
			for (int i = 0; i < calc_width; i++) {
				for (int j = 0; j < calc_height; j++) {
					int sand = gridQuad[i][j];

					if (sand >= 4) {
						new_calc_width = Math.max(i+2, new_calc_width);
						new_calc_height = Math.max(j+2, new_calc_height);
						new_calc_width = Math.min(new_calc_width, width);
						new_calc_height = Math.min(new_calc_height, height);
						isStable = false;

						gridQuad[i][j] &= 3;

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
			//lock.unlock();

			if (printProgess && loops % 1000 == 0) {
				int critSand = amountCriticalSand();
				System.out.print(String.valueOf(critSand) + "              \r");
			}

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

	public long topple() {
		return this.topple(false);
	}

	public void stopTopple() {
		toppling = false;
	}

	public void multiply(int scalar) {
		if (scalar <= 1) return;

		lock.lock();
		for (int i = 0; i < gridQuad.length; i++) {
			for (int j = 0; j < gridQuad[0].length; j++) {
				gridQuad[i][j] *= scalar;
			}
		}
		lock.unlock();
	}

	public int amountSand() {
		int sand = 0;

		lock.lock();
		for (int i = 0; i < gridQuad.length; i++) {
			for (int j = 0; j < gridQuad[0].length; j++) {
				int s = gridQuad[i][j];
				
				if (i != 0) s *= 2;
				if (j != 0) s *= 2;

				sand += s;
			}
		}
		lock.unlock();

		return sand;
	}

	public int amountCriticalSand() {
		int sum = 0;

		lock.lock();
		for (int i = 0; i < gridQuad.length; i++) {
			for (int j = 0; j < gridQuad[0].length; j++) {
				int s = Math.max(gridQuad[i][j] - 3, 0);

				if (i != 0) s *= 2;
				if (j != 0) s *= 2;

				sum += s;
			}
		}
		lock.unlock();

		return sum;
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

	public int getSand(int x, int y) {
		int quadX = Math.abs(this.getWidth() / 2 - x);
		int quadY = Math.abs(this.getHeight() / 2 - y);

		return this.gridQuad[quadX][quadY];
	}

	public void resize(int w, int h) {
		throw new UnsupportedOperationException("Im not ready to be used yet!!!!!!!!!");
	}

	public int[][] getGrid() {
		return gridQuad;
	}

	// @TODO this is terrible
	// NOT THREAD SAFE!!!!!!
	public void copyFrom(SinglePileSandGrid other) {
		// very inefficient (with sizes), work on

		boolean lopped = false;
		int[][] otherGrid = other.getGrid();
		for (int i = 0; i < otherGrid.length; i++) {
			for (int j = 0; j < otherGrid[0].length; j++) {
				try {
					gridQuad[i][j] = otherGrid[i][j];
				}
				catch (ArrayIndexOutOfBoundsException aie) {
					if (!lopped) {
						System.out.println("Grids not equal sizes!"); 
						lopped = true;
					}
				}
			}
		}
	}

	public void unfoldOnto(int[][] fullGrid) {
		int width = gridQuad.length, height = gridQuad[0].length;
		int fullWidth = width * 2 - 1, fullHeight = height * 2 - 1;

		if (fullGrid.length < fullWidth || fullGrid[0].length < fullHeight) {
			throw new IllegalArgumentException("grid passed is too small");
		}

		lock.lock();
		for (int i = 0; i < fullWidth; i++) {
			for (int j = 0; j < fullHeight; j++) {
				int x = Math.abs(i - (width - 1));
				int y = Math.abs(j - (height - 1));

				fullGrid[i][j] = gridQuad[x][y];
			}
		}
		lock.unlock();
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