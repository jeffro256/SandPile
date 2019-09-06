import java.awt.Point;
import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Symmetric Byte-Array Sand Pile Grid 
public class SBSandPileGrid implements LockableSandPileGrid, Serializable {
	private int width;
	private int height;
	private int quadWidth;
	private int quadHeight;
	private byte[][] gridQuad;
	private transient ReentrantLock lock;

	private static final long serialVersionUID = 1L;
	
	public SBSandPileGrid(int width, int height) {
		if (width % 2 == 0 || height % 2 == 0) {
			throw new IllegalArgumentException("width and height must be odd");
		}

		this.width = width;
		this.height = height;
		this.quadWidth = width / 2 + 1;
		this.quadHeight = height / 2 + 1;
		this.gridQuad = new byte[this.quadWidth][this.quadHeight];
		this.lock = new ReentrantLock(true);
	}

	// Assumes other is symmetric
	public SBSandPileGrid(SandPileGrid other) {
		if (other.getWidth() % 2 == 0 || other.getHeight() % 2 == 0) {
			throw new IllegalArgumentException("grid has even-numbered dimensions.");
		}

		this.width = other.getWidth();
		this.height = other.getHeight();
		this.quadWidth = this.width / 2 + 1;
		this.quadHeight = this.height / 2 + 1;
		this.gridQuad = new byte[this.quadWidth][this.quadHeight];
		this.lock = new ReentrantLock(true);

		for (int i = 0; i < this.quadWidth; i++) {
			for (int j = 0; j < this.quadHeight; j++) {
				int otherX = this.width / 2 + i;
				int otherY = this.height / 2 + j;
				int sand = other.getSand(otherX, otherY);

				if (sand >= 128) {
					throw new IllegalArgumentException("sand must be less than 128.");
				}
				else if (sand < 0) {
					throw new IllegalArgumentException("sand must be at least 0.");
				}

				gridQuad[i][j] = (byte) sand;
			}
		}
	}

	public SBSandPileGrid(int[][] gridQuad) {
		final int quadWidth = gridQuad.length;
		final int quadHeight = gridQuad[0].length;

		// I *think* that the byte will never overflow during toppling, even if
		// all sand is 63. Maybe conservative estimate, maybe not ;)
		final int sandLimit = 63; 

		byte[][] byteQuad = new byte[quadWidth][quadHeight];

		for (int i = 0; i < quadWidth; i++) {
			if (gridQuad[i].length != quadHeight) {
				throw new IllegalArgumentException("grid quadrant is jagged");
			}

			for (int j = 0; j < quadHeight; j++) {
				int sand = gridQuad[i][j];

				if (sand > sandLimit) {
					String msg = "sand greater than " + sandLimit + " at (" + i + ", " + j + "): " + sand;

					throw new IllegalArgumentException(msg);
				}

				if (sand < 0) {
					String msg = "sand less than 0 at (" + i + ", " + j + ")";

					throw new IllegalArgumentException(msg);
				}

				byteQuad[i][j] = (byte) sand;
			}
		}

		this.width = quadWidth * 2 - 1;
		this.height = quadHeight * 2 - 1;
		this.quadWidth = quadWidth;
		this.quadHeight = quadHeight;
		this.gridQuad = byteQuad;
		this.lock = new ReentrantLock(true);
	}

	public void place(byte sand) {
		this.gridQuad[0][0] += sand;
	}

	@Override
	public long topple() {
		return this.step(-1);
	}

	@Override
	public long step(long maxSteps) {
		final boolean indefinite = maxSteps < 0;
		final int lockInterval = 20; 
		boolean stable = this.isStable();
		Point topplePt = new Point(this.quadWidth - 1, this.quadHeight - 1);

		lock.lock();

		int steps = 0;
		while ((steps < maxSteps || indefinite) && !stable) {
			if (steps % lockInterval == 0 && lock.hasQueuedThreads()) {
				lock.unlock();
				lock.lock();
			}

			topplePt = this.centralStep(topplePt);

			stable = topplePt.equals(this.doneStepPoint);

			steps++;
		}

		lock.unlock();

		return steps;
	}

	@Override
	public void step() {
		this.centralStep(new Point(this.quadWidth - 1, this.quadHeight - 1));
	}

	@Override
	public boolean isStable() {
		lock.lock();

		for (int i = 0; i < this.quadWidth; i++) {
			for (int j = 0; j < this.quadHeight; j++) {
				if (this.gridQuad[i][j] >= 4) {
					lock.unlock();

					return false;
				}
			}
		}

		lock.unlock();

		return true;
	}

	@Override
	public int getSand(int x, int y) {
		int quadX = Math.abs(this.width  / 2 - x);
		int quadY = Math.abs(this.height / 2 - y);

		return this.gridQuad[quadX][quadY];
	}

	@Override
	public void setSand(int x, int y, int sand) {
		if (sand >= 128) {
			throw new IllegalArgumentException("sand must be less than 128.");
		}
		else if (sand < 0) {
			throw new IllegalArgumentException("sand must be at least 0.");
		}

		int quadX = Math.abs(this.width  / 2 - x);
		int quadY = Math.abs(this.height / 2 - y);

		this.gridQuad[quadX][quadY] = (byte) sand;
	}

	@Override
	public long amountSand() {
		long sandSum = 0;

		lock.lock();

		for (int i = 0; i < this.quadWidth; i++) {
			for (int j = 0; j < this.quadHeight; j++) {
				long sand = this.gridQuad[i][j];

				if (i > 0) sand <<= 1;
				if (j > 0) sand <<= 1;

				sandSum += sand;
			}
		}

		lock.unlock();

		return sandSum;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public void resize(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("width or height not positive.");
		}
		else if (width % 2 == 0 || height % 2 == 0) {
			throw new IllegalArgumentException("width or height not odd.");
		}

		lock.lock();

		int newQuadWidth = width / 2 + 1;
		int newQuadHeight = height / 2 + 1;
		byte[][] newGrid = new byte[newQuadWidth][newQuadHeight];

		int smallQuadWidth = Math.min(this.quadWidth, newQuadWidth);
		int smallQuadHeight = Math.min(this.quadHeight, newQuadHeight);

		for (int i = 0; i < smallQuadWidth; i++) {
			for (int j = 0; j < smallQuadHeight; j++) {
				newGrid[i][j] = this.gridQuad[i][j];
			}
		}

		this.width = width;
		this.height = height;
		this.quadWidth = newQuadWidth;
		this.quadHeight = newQuadHeight;
		this.gridQuad = newGrid;

		lock.unlock();
	}

	public void multiply(int scalar) {
		for (int i = 0; i < this.quadWidth; i++) {
			for (int j = 0; j < this.quadHeight; j++) {
				gridQuad[i][j] *= scalar;
			}
		}
	}

	// Note that two sand pile grids could be equivalent when toppled, 
	// but equals() returns false
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;

		try {
			this.lock.lock();

			SandPileGrid other = (SandPileGrid) obj;

			if (this.width != other.getWidth()) return false;
			if (this.height != other.getHeight()) return false;

			for (int j = 0; j < this.height; j++) {
				for (int i = 0; i < this.width; i++) {
					int thisSand = this.getSand(i, j);
					int otherSand = other.getSand(i, j);

					if (thisSand != otherSand) {
						return false;
					}
				}
			}
		}
		catch (ClassCastException e) {
			return false;
		}
		finally {
			this.lock.unlock();
		}

		return true;
	}

	@Override
	public Lock getLock() {
		return this.lock;
	}

	// Implementation details
	
	private Point doneStepPoint = new Point(-1, -1);

	// The point passed and returned signfies the maximum (x, y) to check
	private Point centralStep(Point maxPt) {
		int maxX = -1;
		int maxY = -1;

		for (int i = 0; i <= maxPt.x; i++) {
			for (int j = 0; j <= maxPt.y; j++) {
				final int toppleSand = this.gridQuad[i][j] >>> 2;

				if (toppleSand != 0) {
					this.gridQuad[i][j] &= 3;

					if (i > 1) {
						this.gridQuad[i-1][j] += toppleSand;
					}
					else if (i == 1) {
						this.gridQuad[0][j] += toppleSand << 1;
					}

					if (i < this.quadWidth - 1) {
						this.gridQuad[i+1][j] += toppleSand;

						maxX = Math.max(i + 1, maxX);
					}

					if (j > 1) {
						this.gridQuad[i][j-1] += toppleSand;
					}
					else if (j == 1) {
						this.gridQuad[i][0] += toppleSand << 1;
					}

					if (j < this.quadHeight - 1) {
						this.gridQuad[i][j+1] += toppleSand;

						maxY = Math.max(j + 1, maxY);
					}
				}
			}
		}

		return new Point(maxX, maxY);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		lock.lock();
		try {
			out.defaultWriteObject();
		} finally {
			lock.unlock();
		}
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		lock = new ReentrantLock(true);
	}
}