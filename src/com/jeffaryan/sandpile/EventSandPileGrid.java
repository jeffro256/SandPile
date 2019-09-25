package com.jeffaryan.sandpile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class EventSandPileGrid implements LockableSandPileGrid {
	private int width;
	private int height;
	private int[][] grid;

	private List<SandPileListener> listeners = new ArrayList<SandPileListener>();

	private transient EventLock lock = new EventLock(this);

	protected static enum Event {
		SET_SAND, TOPPLE, START_TOP, STOP_TOP, START_STEP, STOP_STEP, RESIZE, LOCK, UNLOCK
	}
	
	public EventSandPileGrid(int width, int height) {
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
	
	public EventSandPileGrid(int[][] grid) {
		this.width = grid.length;
		this.height = grid[0].length;
		this.grid = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.grid[i][j] = grid[i][j];
			}
		}
	}

	public void addListener(SandPileListener listener) {
		listeners.add(listener);
	}

	// Return true if able to remove listener
	public boolean removeListener(SandPileListener listener) {
		return listeners.remove(listener);
	}
	
	@Override
	public long step(long maxSteps) {
		boolean isTopple = maxSteps < 0;

		if (isTopple) {
			triggerEvent(Event.START_TOP, new long[0]);	
		}
		else {
			triggerEvent(Event.START_STEP, new long[]{maxSteps});
		}

		int[][] newGrid = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newGrid[i][j] = grid[i][j];
			}
		}

		long steps = 0;
		while (!isStable() && (isTopple || steps < maxSteps)) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int sand = grid[i][j];
					
					if (sand >= 4) {
						newGrid[i][j] -= 4;
						
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

						triggerEvent(Event.TOPPLE, new long[]{i, j, newGrid[i][j], newGrid[i][j] + 4});
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

		if (isTopple) {
			triggerEvent(Event.STOP_TOP, new long[]{steps});	
		}
		else {
			triggerEvent(Event.STOP_STEP, new long[]{steps});
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
				if (j >= 4) {
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

		triggerEvent(Event.SET_SAND, new long[]{x, y, s});
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

	public void multiply(int a) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				grid[i][j] *= a;
			}
		}
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

		triggerEvent(Event.RESIZE, new long[]{width, height});
	}

	@Override
	public Lock getLock() {
		return lock;
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

	protected void triggerEvent(Event event, long[] args) {
		if (!isVaidTrigger(event, args)) {
			String errMsg = "Invalid arguments - " + args + " - for event '" + event + "'.";
			throw new IllegalArgumentException(errMsg);
		}

		for (SandPileListener listener : listeners) {
			switch (event) {
				case SET_SAND:
				listener.setSand(this, (int) args[0], (int) args[1], (int) args[2]);
				break;
				case TOPPLE:
				listener.toppledCell(this, (int) args[0], (int) args[1], (int) args[2], (int) args[3]);
				break;
				case START_TOP:
				listener.startedToppling(this);
				break;
				case STOP_TOP:
				listener.stoppedToppling(this, args[0]);
				break;
				case START_STEP:
				listener.startedStepping(this, args[0]);
				break;
				case STOP_STEP:
				listener.stoppedStepping(this, args[0]);
				break;
				case RESIZE:
				listener.resized(this, (int) args[0], (int) args[1]);
				break;
				case LOCK:
				listener.locked(this);
				break;
				case UNLOCK:
				listener.unlocked(this);
				break;
				default:
				String errMsg = "Event (" + event + ") not supported (yet).";
				throw new UnsupportedOperationException(errMsg);
			}
		}
	}

	private boolean isVaidTrigger(Event event, long[] args) {
		int argc = args.length;

		switch (event) {
			case SET_SAND:
			return argc == 3;
			case TOPPLE:
			return argc == 4;
			case START_TOP:
			return argc == 0;
			case STOP_TOP:
			return argc == 1;
			case START_STEP:
			return argc == 1;
			case STOP_STEP:
			return argc == 1;
			case RESIZE:
			return argc == 2;
			case LOCK:
			return argc == 0;
			case UNLOCK:
			return argc == 0;
			default:
			System.err.println("Event (" + event + ") not supported (yet).");
			return false;
		}
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
		lock = new EventLock(this);
	}
}

class EventLock extends ReentrantLock {
	private WeakReference<EventSandPileGrid> grid;

	public EventLock(EventSandPileGrid grid, boolean fair) {
		super(fair);

		this.grid = new WeakReference<EventSandPileGrid>(grid);
	}

	public EventLock(EventSandPileGrid grid) {
		this(grid, true);
	}

	@Override
	public void lock() {
		super.lock();

		grid.get().triggerEvent(EventSandPileGrid.Event.LOCK, new long[0]);
	}

	@Override
	public boolean tryLock() {
		boolean res = super.tryLock();

		if (res) {
			grid.get().triggerEvent(EventSandPileGrid.Event.LOCK, new long[0]);
		}

		return res;
	}

	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		boolean res = super.tryLock(timeout, unit);

		if (res) {
			grid.get().triggerEvent(EventSandPileGrid.Event.LOCK, new long[0]);
		}

		return res;
	}

	@Override
	public void unlock() {
		super.unlock();

		grid.get().triggerEvent(EventSandPileGrid.Event.UNLOCK, new long[0]);
	}
}
