public class SandPileThreads {
	public static Thread createAutosaveThread(Object obj, int saveDelay, String... paths) {
		Thread saveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					try {
						for (String path: paths) {
							FileOutputStream fstream = new FileOutputStream(path);
							ObjectOutputStream ostream = new ObjectOutputStream(fstream);
							ostream.writeObject(obj);
							ostream.close();
							fstream.close();
						}

						Thread.sleep(saveDelay);
					}
					catch (InterruptedException ie) { 
						break;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		saveThread.setDaemon(true);

		return saveThread;
	}

	public static Thread createAutosaveThread(Object obj, String... paths) {
		return createAutosaveThread(obj, 180000000, paths);
	}

	public static Thread createShutdownHook(Object obj, String... paths) {
		Thread hook = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					for (String path: paths) {
						System.out.println("Serializing to " + path);
						FileOutputStream fstream = new FileOutputStream(path);
						ObjectOutputStream ostream = new ObjectOutputStream(fstream);
						ostream.writeObject(obj);
						ostream.close();
						fstream.close();
					}
					System.out.println("Exiting...");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		hook.setDaemon(false);

		return hook;
	}
}