package com.jeffaryan.sandpile;

import java.util.concurrent.locks.ReentrantLock;
import java.util.Timer;
import java.util.TimerTask;

public class SandPileThreads {
	private static Timer timer = new Timer("SandPileThreads Timer", true);

	public static TimerTask scheduleAutosave(LockableSandPileGrid grid, long saveDelay, String... paths) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				grid.getLock().lock();

				for (String path: paths) {
					try {
						SandPiles.saveSandPile(grid, path);

						System.out.println("Autosaved sand pile to " + path);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

				grid.getLock().unlock();
			}
		};

		timer.schedule(task, saveDelay, saveDelay);

		return task;
	}

	public static TimerTask scheduleAutosave(LockableSandPileGrid grid, String... paths) {
		long saveDelay = 18000000; // 5 hours
		return scheduleAutosave(grid, saveDelay, paths);
	}

	public static TimerTask scheduleProgressReport(LockableSandPileGrid grid, long delay) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				grid.getLock().lock();

				long criticalSand = SandPiles.amountCriticalSand(grid);
				System.out.println("Critical Sand: " + criticalSand);
				
				grid.getLock().unlock();

				//System.out.println("done with progress. lock hold count: " + ((ReentrantLock) grid.getLock()).getHoldCount());
			}
		};

		timer.schedule(task, delay, delay);

		return task;
	}

	public static TimerTask scheduleProgressReport(LockableSandPileGrid grid) {
		long delay = 3600000; // 1 hour
		return scheduleProgressReport(grid, delay);
	}

	public static Thread createShutdownHook(LockableSandPileGrid grid, String... paths) {
		Thread hook = new Thread(new Runnable() {
			@Override
			public void run() {
				grid.getLock().lock();

				System.out.println("Performing shutdown tasks.");

				for (String path: paths) {
					System.out.println("Serializing to " + path);

					try {
						SandPiles.saveSandPile(grid, path);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}

				grid.getLock().unlock();
				System.out.println("Exiting...");
			}
		});
		hook.setDaemon(false);

		return hook;
	}

	public static void registerAutosaveSH(LockableSandPileGrid grid, String... paths) {
		Runtime.getRuntime().addShutdownHook(createShutdownHook(grid, paths));
	}
}