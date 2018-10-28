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

package neon.systems.time;

/**
 * The calendar used by the game. It features the following:
 * <ul>
 * 	<li>six-day weeks</li>
 * 	<li>five-week months (30 days per month)</li>
 * 	<li>12-month years (360 days per year)</li>
 * 	<li>212-day religious year</li>
 * 	<li>53-year cycles (least common multiple of 212 and 360 days)</li>
 * </ul>
 * 
 * @author mdriesen
 *
 */
public class Calendar {
	private final static int DAYS_PER_WEEK = 6;
	private final static int WEEKS_PER_MONTH = 5;
	private final static int DAYS_PER_MONTH = DAYS_PER_WEEK*WEEKS_PER_MONTH;
	private final static int MONTHS_PER_YEAR = 12;
	private final static int DAYS_PER_YEAR = DAYS_PER_MONTH*MONTHS_PER_YEAR;
	private final static int WEEKS_PER_YEAR = WEEKS_PER_MONTH*MONTHS_PER_YEAR;
	
	/**
	 * Returns the day of the week as an integer.
	 * 
	 * @param days
	 * @return
	 */
	public int getDayOfWeek(int days) {
		return (days - 1)%DAYS_PER_WEEK + 1;
	}
	
	/**
	 * Returns the day of the month as an integer.
	 * 
	 * @param days
	 * @return
	 */
	public int getDayOfMonth(int days) {
		return (days - 1)%DAYS_PER_MONTH + 1;
	}
	
	/**
	 * Returns the day of the year as an integer.
	 * 
	 * @param days
	 * @return
	 */
	public int getDayOfYear(int days) {
		return (days - 1)%DAYS_PER_YEAR + 1;
	}
	
	/**
	 * Returns the name of the day.
	 * 
	 * @param days
	 * @return
	 */
	public Day getDayName(int days) {
		return Day.values()[getDayOfWeek(days) - 1];
	}
	
	/**
	 * Returns the week the given day falls in.
	 * 
	 * @param days
	 * @return
	 */
	public int getWeek(int days) {
		return (days - 1)/DAYS_PER_WEEK + 1;
	}
	
	/**
	 * Return the week of the month.
	 * 
	 * @param days
	 * @return
	 */
	public int getWeekOfMonth(int days) {
		return (getWeek(days) - 1)%WEEKS_PER_MONTH + 1;
	}
	
	/**
	 * Returns the week of the year.
	 * 
	 * @param days
	 * @return
	 */
	public int getWeekOfYear(int days) {
		return (getWeek(days) - 1)%WEEKS_PER_YEAR + 1;
	}
	
	/**
	 * Returns the month the given day falls in.
	 * 
	 * @param days
	 * @return
	 */
	public int getMonth(int days) {
		return (days - 1)/DAYS_PER_MONTH + 1;
	}
	
	/**
	 * Return the month of the year.
	 * 
	 * @param days
	 * @return
	 */
	public int getMonthOfYear(int days) {
		return (getMonth(days) - 1)%MONTHS_PER_YEAR + 1;
	}
	
	/**
	 * Returns the name of the month.
	 * 
	 * @param days
	 * @return
	 */
	public Month getMonthName(int days) {
		return Month.values()[getMonthOfYear(days) - 1];
	}
	
	/**
	 * Returns the year.
	 * 
	 * @param days
	 * @return
	 */
	public int getYear(int days) {
		return (days - 1)/DAYS_PER_YEAR + 1;
	}
}
