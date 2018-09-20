/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2017-2018 - Maarten Driesen
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Logger;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import neon.common.console.ConsoleEvent;
import neon.common.event.NewGameEvent;
import neon.common.event.ScriptEvent;

public class ScriptHandler {
	private static final Logger logger = Logger.getGlobal();
	
	private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	private final EventBus bus;
	
	public ScriptHandler(EventBus bus) {
		this.bus = bus;
		
		try {
			engine.eval(new FileReader("data/aneirin/scripts/start.js"));
			engine.eval(new FileReader("data/aneirin/scripts/stop.js"));
			
			for (Map.Entry<String, Object> entry : engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()) {
				System.out.println("binding: " + entry.getKey() + ": " + entry.getValue().getClass());
			}
		} catch (FileNotFoundException | ScriptException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes a script and sends the result to the debug console. To prevent
	 * server resources from leaking to the client, only the {@code String}
	 * representation of the result is sent.
	 * 
	 * @param event
	 */
	@Subscribe
	private void execute(ScriptEvent event) {
		Object result = execute(event.getScript());
		if (result != null) {
			bus.post(new ConsoleEvent(result.toString()));
		}
	}
	
	/**
	 * Executes the given script with the nashorn engine.
	 * 
	 * @param script the script to execute
	 * @return the result of the script
	 */
	private Object execute(String script) {
		Object result = null;
		try {
			result = engine.eval(script);
		} catch (ScriptException e) {
			logger.warning("could not evaluate script: " + script);
		}		
		return result;
	}
	
	@Subscribe
	private void onGameStart(NewGameEvent event) throws ScriptException {
		engine.eval("onGameStart()");		
	}
	
	public static String output(String name) {
	    System.out.println(name);
	    return "greetings from java";
	}
}
