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

package neon.server.handlers;

import java.io.IOException;
import java.util.Objects;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.entity.Entity;
import neon.common.entity.components.DoorInfo;
import neon.common.entity.components.Shape;
import neon.common.event.ComponentEvent;
import neon.common.event.DoorEvent;
import neon.common.resources.ResourceException;
import neon.server.entity.EntityManager;
import neon.server.entity.Map;

public class DoorHandler {
	private static final long PLAYER_UID = 0;
	
	private final EntityManager entities;
	private final EventBus bus;
	private final Notifier notifier;
	
	public DoorHandler(EntityManager entities, EventBus bus) {
		this.entities = Objects.requireNonNull(entities, "entity manager");
		this.bus = Objects.requireNonNull(bus, "event bus");
		notifier = new Notifier(entities, bus);
	}
	
	@Subscribe
	private void onTransport(DoorEvent.Transport event) throws IOException, ResourceException {
		Entity door = entities.getEntity(event.door);
		DoorInfo info = door.getComponent(DoorInfo.class);
		Map map = entities.getMap(info.getDestination());
		notifier.notifyClient(map);

		Shape player = entities.getEntity(PLAYER_UID).getComponent(Shape.class);
		player.setPosition(info.getDestinationX(), info.getDestinationY(), 0);
		bus.post(new ComponentEvent(player));
	}
}
