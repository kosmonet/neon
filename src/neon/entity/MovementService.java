/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017 - Maarten Driesen
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

package neon.entity;

import java.awt.Point;

import neon.entity.entities.Creature;
import neon.entity.entities.Player;
import neon.entity.systems.NeonSystem;
import neon.util.Direction;

/**
 * The system that handles all movement-related events.
 * 
 * @author mdriesen
 *
 */
public class MovementService implements NeonSystem {
	/**
	 * Moves the player on the current map.
	 * 
	 * @param event
	 */
	public void move(Player player, Direction direction) {
		switch(direction) {
		case LEFT: player.shape.setX(Math.max(0, player.shape.getX() - 1)); break;
		case RIGHT: player.shape.setX(player.shape.getX() + 1); break;
		case UP: player.shape.setY(Math.max(0, player.shape.getY() - 1)); break;
		case DOWN: player.shape.setY(player.shape.getY() + 1); break;
		}

		player.stats.perform(Action.MOVE_STRAIGHT);	
	}

	/**
	 * Moves a creature on the current map.
	 * 
	 * @param event
	 */
	public void move(Creature creature, Point position) {
		if(creature.shape.getX() == position.x || creature.shape.getY() == position.y) {
			creature.stats.perform(Action.MOVE_STRAIGHT);			
		} else {
			creature.stats.perform(Action.MOVE_DIAGONAL);				
		}
		creature.shape.setX(position.x);
		creature.shape.setY(position.y);
	}
}
