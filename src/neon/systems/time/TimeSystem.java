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

import com.google.common.eventbus.Subscribe;

import neon.common.event.TimerEvent;
import neon.common.event.TurnEvent;
import neon.server.Configuration;
import neon.server.Configuration.GameMode;
import neon.server.systems.NeonSystem;
import neon.systems.scripting.ScriptHandler;

public final class TimeSystem implements NeonSystem {
	private final Configuration config;
	
	public TimeSystem(Configuration config, ScriptHandler scripting) {
		this.config = config;
		scripting.registerObject("calendar", config.getCalendar());
	}
	
	@Subscribe
	private void onTimerTick(TimerEvent event) {
		if (config.isRunning() && config.getMode().equals(GameMode.REAL_TIME)) {
			config.getCalendar().addTicks(1);
		}
	}

	@Subscribe
	private void onNextTurn(TurnEvent event) {
		if (config.isRunning() && config.getMode().equals(GameMode.TURN_BASED)) {
			config.getCalendar().addTicks(5);
		}
	}
	
	@Subscribe
	private void onSleep(RestEvent.Sleep event) {
		config.getCalendar().addTicks(Configuration.TICKS_PER_TURN*Calendar.TURNS_PER_DAY/3);
	}
}
