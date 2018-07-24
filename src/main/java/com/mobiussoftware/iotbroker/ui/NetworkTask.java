package com.mobiussoftware.iotbroker.ui;

import javax.swing.*;
import java.util.Random;

public class NetworkTask<T, V> extends SwingWorker<T, V> {

	@Override
	public T doInBackground() {
		Random random = new Random();
		int progress = 0;
		// Initialize progress property.
		setProgress(0);
		while (progress < 100) {
			// Sleep for up to one second.
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignore) {
			}
			// Make random progress.
			progress += random.nextInt(2) + 1;
			setProgress(Math.min(progress, 100));
		}
		return null;
	}
}
