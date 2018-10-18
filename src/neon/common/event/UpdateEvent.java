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

package neon.common.event;

import neon.common.resources.RCreature;
import neon.common.resources.RMap;
import neon.entity.Skill;
import neon.entity.components.Info;
import neon.entity.components.Shape;
import neon.entity.components.Skills;
import neon.entity.components.Stats;
import neon.entity.entities.Creature.Resource;
import neon.entity.entities.Player;

/**
 * An event containing updates for the client.
 * 
 * @author mdriesen
 *
 */
public abstract class UpdateEvent extends NeonEvent {
	/**
	 * An event to indicate that a game is started.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Start extends UpdateEvent {
		public final String id, name, gender;
		public final int x, y, z;
		public final int strength, constitution, dexterity, intelligence, wisdom, charisma;
		public final int swimming;

		public Start(Player player) {
			Shape shape = player.getComponent(Shape.class);
			RCreature creature = player.getComponent(Resource.class).getResource();
			Info info = player.getComponent(Info.class);
			Stats stats = player.getComponent(Stats.class);
			Skills skills = player.getComponent(Skills.class);
			
			id = creature.id;
			name = info.getName();
			gender = info.getGender();
			
			x = shape.getX();
			y = shape.getY();
			z = shape.getZ();
			
			strength =  stats.getBaseStr();
			constitution = stats.getBaseCon();
			dexterity = stats.getBaseDex();
			intelligence = stats.getBaseInt();
			wisdom = stats.getBaseWis();
			charisma = stats.getBaseCha();
			
			swimming = (int) skills.getSkill(Skill.SWIMMING);
		}	
	}
	
	/**
	 * An event to indicate a change of map.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Map extends UpdateEvent {
		public final String map;
		
		public Map(RMap map) {
			this.map = map.id;
		}
	}
	
	/**
	 * An event to signal a creature update.
	 * 
	 * @author mdriesen
	 *
	 */
	public static class Creature extends UpdateEvent {
		public final long uid;
		public final String id, map;
		public final int x, y, z;
		
		public Creature(long uid, String id, String map, int x, int y, int z) {
			this.uid = uid;
			this.id = id;
			this.map = map;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static class Move extends UpdateEvent {
		public final long uid;
		public final int x, y, z;
		public final String map;

		public Move(long uid, String map, int x, int y, int z) {
			this.uid = uid;
			this.x = x;
			this.y = y;
			this.z = z;
			this.map = map;
		}
	}
	
	public static class Item extends UpdateEvent {
		public final long uid;
		public final String id, map;
		public final int x, y, z;
		
		public Item(long uid, String id, String map, int x, int y, int z) {
			this.uid = uid;
			this.id = id;
			this.map = map;
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static class Remove extends UpdateEvent {
		public final long uid;
		public final String map;
		
		public Remove(long uid, String map) {
			this.uid = uid;
			this.map = map;
		}
	}
	
	public static class Pick extends UpdateEvent {
		public final long uid;
		public final String map;
		
		public Pick(long uid, String map) {
			this.uid = uid;
			this.map = map;
		}
	}
	
	public static class SkillUpdate extends UpdateEvent {
		public final long uid;
		public final String skill;
		public final int value;
		
		public SkillUpdate(long uid, String skill, int value) {
			this.uid = uid;
			this.skill = skill;
			this.value = value;
		}
	}
	
	public static class Level extends UpdateEvent {
		public final long uid;
		public final int level;
		
		public Level(long uid, int level) {
			this.uid = uid;
			this.level = level;
		}
	}
}
