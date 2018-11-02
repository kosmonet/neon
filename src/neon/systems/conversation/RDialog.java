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

package neon.systems.conversation;

import java.util.HashMap;

import neon.common.resources.Resource;

final class RDialog extends Resource {
	private final HashMap<String, PlayerNode> pnodes = new HashMap<>();
	private final HashMap<String, CreatureNode> cnodes = new HashMap<>();
	
	RDialog(String id) {
		super(id, "dialog");
	}
	
	CreatureNode getRoot() {
		return cnodes.get(id);	// root has same id as the entire dialog
	}
	
	void addNode(CreatureNode node) {
		cnodes.put(node.id, node);
	}
	
	void addNode(PlayerNode node) {
		pnodes.put(node.id, node);
	}
	
	CreatureNode getCreatureNode(String id) {
		return cnodes.get(id);
	}
	
	PlayerNode getPlayerNode(String id) {
		return pnodes.get(id);
	}
}
