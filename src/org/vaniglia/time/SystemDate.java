/**
 * Project Vaniglia
 * User: Michele Aiello
 *
 * Copyright (C) 2003/2007  Michele Aiello
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.vaniglia.time;

public class SystemDate {

	private static SystemDate _instance;

	private long baseTime = -1;
	private long timeOfBaseTime = -1;
	private int speed = 1;

	private SystemDate() {
	}

	public static synchronized final SystemDate getInstance() {
		if (_instance == null) {
			_instance = new SystemDate();
		}
		return _instance;
	}

	public void setCurrent(long millis) {
		this.baseTime = millis;
		this.timeOfBaseTime = System.currentTimeMillis();
	}

	public void setUseSystemTime() {
		this.baseTime = -1;
		this.timeOfBaseTime = -1;
		this.speed = 1;
	}

	public void setTimeSpeed(int speed) {
		this.speed = speed;
	}

	public long currentTimeMillis() {
		if (baseTime >= 0) {
			return baseTime + ((System.currentTimeMillis() - timeOfBaseTime))*speed;
		}
		else {
			return System.currentTimeMillis();
		}
	}
}
