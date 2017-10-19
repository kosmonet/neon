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

package neon.util;

import java.util.Random;

/**
 * Polyhedral dice roller.
 * 
 * @author mdriesen
 */
public class Dice {
	private final static Random random = new Random();
	
	/**
	 * Rolls dice.
	 * 
	 * @param x	the amount of rolls
	 * @param y	the type of dice
	 * @param z	a modifier
	 * @return	the result of the dice roll <i>x</i>d<i>y</i> + <i>z</i>
	 */
	public static int roll(int x, int y, int z) {
		int result = 0;
		
		for (int i = 0; i < x; i++) {
			result += random.nextInt(y) + 1;
		}
		
		return result + z;
	}
	
//	public static int roll(String roll) {
//		return 0;
//	}
}
