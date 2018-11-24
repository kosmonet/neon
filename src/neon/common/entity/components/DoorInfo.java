/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2018 - Maarten Driesen
 * 
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neon.common.entity.components;

import neon.common.entity.DoorState;

public class DoorInfo implements Component {
	private final long uid;
	private final int destination;
	private final String text;
	
	private DoorState state = DoorState.CLOSED;
	
	public DoorInfo(long uid) {
		this(uid, 0, "", 0, 0);
	}
	
	public DoorInfo(long uid, int destination, String text, int x, int y) {
		this.uid = uid;
		this.destination = destination;
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public int getDestination() {
		return destination;
	}
	
	public void open() {
		state = DoorState.OPENED;
	}

	public void close() {
		state = DoorState.CLOSED;
	}

	public boolean isClosed() {
		return state == DoorState.CLOSED || state == DoorState.LOCKED;
	}
	
	@Override
	public long getEntity() {
		return uid;
	}
}
