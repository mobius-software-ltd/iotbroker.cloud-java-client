package com.mobiussoftware.iotbroker.ui;

/**
* Mobius Software LTD
* Copyright 2015-2018, Mobius Software LTD
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

import javax.swing.*;
import java.util.Random;

public class NetworkTask<T, V> extends SwingWorker<T, V>
{

	@Override public T doInBackground()
	{
		Random random = new Random();
		int progress = 0;
		// Initialize progress property.
		setProgress(0);
		while (progress < 100)
		{
			// Sleep for up to one second.
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException ignore)
			{
			}
			// Make random progress.
			progress += random.nextInt(2) + 1;
			setProgress(Math.min(progress, 100));
		}
		return null;
	}
}
