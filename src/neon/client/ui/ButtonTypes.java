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

package neon.client.ui;

import javafx.scene.control.ButtonType;

/**
 * A collection of standard button types in the game.
 * 
 * @author mdriesen
 *
 */
public final class ButtonTypes {
	public static final ButtonType TALK = new ButtonType("Talk");
	public static final ButtonType ATTACK = new ButtonType("Attack");
	public static final ButtonType PICK = new ButtonType("Pick pocket");
	public static final ButtonType RIDE = new ButtonType("Ride animal");
	public static final ButtonType SWAP = new ButtonType("Swap position");
	
	public static final ButtonType CANCEL = new ButtonType("Cancel");
	public static final ButtonType YES = new ButtonType("Yes");
	public static final ButtonType NO = new ButtonType("No");
	
	// suppress default constructor for noninstantiability
	private ButtonTypes() {
		throw new AssertionError();
	}
}
