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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Polyhedral dice roller.
 * 
 * @author mdriesen
 */
public class Dice {
	// suppress default constructor for noninstantiability
	private Dice() {
		throw new AssertionError();
	}

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
			result += ThreadLocalRandom.current().nextInt(y) + 1;
		}
		
		return result + z;
	}
	
	/**
	 * Returns the result of a dice roll. The input string has the form 'xdy', 
	 * 'xdy+z' or 'xdy-z', with x, y and z positive integers. 
	 * 
	 * @param roll	the string representation of the roll
	 * @return		the result of the roll
	 */
	public static int roll(String roll) {
		int index1 = roll.indexOf("d");
		int index2 = roll.indexOf("+");
		int index3 = roll.indexOf("-");
		int number = Integer.parseInt(roll.substring(0, index1));
		int dice = 0;
		int mod = 0;
		
		if (index2 > 0 ) {			// -1 means no + was found
			dice = Integer.parseInt(roll.substring(index1 + 1, index2));
			mod = Integer.parseInt(roll.substring(index2 + 1, roll.length()));
		} else if (index3 > 0) {		// -1 means no - was found
			dice = Integer.parseInt(roll.substring(index1 + 1, index3));
			mod = -Integer.parseInt(roll.substring(index3 + 1, roll.length()));			
		} else {
			dice = Integer.parseInt(roll.substring(index1 + 1, roll.length()));
		}
		
		return roll(number, dice, mod);
	}
}
