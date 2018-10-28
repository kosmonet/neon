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

public class ButtonTypes {
	public final static ButtonType talk = new ButtonType("Talk");
	public final static ButtonType attack = new ButtonType("Attack");
	public final static ButtonType pick = new ButtonType("Pick pocket");
	public final static ButtonType ride = new ButtonType("Ride animal");
	public final static ButtonType swap = new ButtonType("Swap position");
	
	public final static ButtonType cancel = new ButtonType("Cancel");
	public final static ButtonType yes = new ButtonType("Yes");
	public final static ButtonType no = new ButtonType("No");
	
	// suppress default constructor for noninstantiability
	private ButtonTypes() {
		throw new AssertionError();
	}
}
