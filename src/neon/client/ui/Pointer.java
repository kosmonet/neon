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

package neon.client.ui;

import javafx.scene.paint.Color;
import neon.common.entity.components.Graphics;
import neon.common.entity.components.Shape;
import neon.common.resources.RMap;
import neon.util.Direction;

/**
 * A pointer that can be moved on the game screen.
 * 
 * @author mdriesen
 *
 */
public class Pointer {
	private final Shape shape;
	private final Graphics graphics;

	public Pointer(long uid) {
		graphics = new Graphics(uid, '◎', Color.WHITE);
		shape = new Shape(uid);
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public Graphics getGraphics() {
		return graphics;
	}
	
	public int getX() {
		return shape.getX();
	}
	
	public int getY() {
		return shape.getY();
	}
	
	/**
	 * Sets the position of the pointer.
	 * 
	 * @param position
	 */
	public void setPosition(Shape position) {
		shape.setPosition(position.getX(), position.getY(), position.getZ());
	}
	
	/**
	 * Moves the pointer in a certain direction on the map.
	 * 
	 * @param direction
	 * @param map
	 */
	public void move(Direction direction, RMap map) {
		switch (direction) {
		case LEFT: 
			shape.setX(Math.max(0, shape.getX() - 1)); 
			break;
		case RIGHT: 
			shape.setX(Math.min(map.width, shape.getX() + 1)); 
			break;
		case UP: 
			shape.setY(Math.max(0, shape.getY() - 1)); 
			break;
		case DOWN: 
			shape.setY(Math.min(map.height, shape.getY() + 1)); 
			break;
		case DOWN_LEFT:
			shape.setX(Math.max(0, shape.getX() - 1)); 
			shape.setY(Math.max(0, shape.getY() - 1)); 
			break;
		case DOWN_RIGHT:
			shape.setX(Math.min(map.width, shape.getX() + 1)); 
			shape.setY(Math.max(0, shape.getY() - 1)); 
			break;
		case UP_LEFT:
			shape.setX(Math.max(0, shape.getX() - 1)); 
			shape.setY(Math.max(0, shape.getY() - 1)); 
			break;
		case UP_RIGHT:
			shape.setX(Math.min(map.width, shape.getX() + 1)); 
			shape.setY(Math.max(0, shape.getY() - 1)); 
			break;
		default:
			break;
		}		
	}
}
