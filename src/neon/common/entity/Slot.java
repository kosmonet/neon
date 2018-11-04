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

package neon.common.entity;

/**
 * <p>An enumeration of all equipable item slots. One single item may 
 * be equipped in each slot.</p> 
 * 
 * <p>Slots that accept armor have a modifier to indicate how much that slot 
 * contributes to overall protection. As an example, the head slot has a 
 * modifier of 0.1. If a helmet with armor rating 200 is equipped, this will
 * increase the total armor rating of a creature by 0.1×200 = 20.</p>
 * 
 * @author mdriesen
 *
 */
public enum Slot {
	// armor
	/**	The head slot, will accept hats and helmets. Armor modifier 0.1. Can be combined with a hood. */
	HEAD(0.1f), 
	
	/** A slot for pauldrons (shoulder/upper arm protection). Armor modifier 0.2. */
	PAULDRONS(0.15f), 
	
	/** A slot for gloves and gauntlets (hand/forearm protection). Armor modifier 0.1. */	
	HANDS(0.1f), 
	
	/** Slot for a cuirass (torso protection). Armor modifier 0.4. Can be combined with a shirt and cloak. */
	CUIRASS(0.4f), 
	
	/** Slot for chausses (upper leg protection). Armor modifier 0.1. Can be combined with trousers or skirt. */
	CHAUSSES(0.15f), 
	
	/** A slot for boots or shoes. Armor modifier 0.1. */
	BOOTS(0.1f), 
	
	/** A slot for weapons or shields carried in the left hand. Armor modifier 0.2. */
	HAND_LEFT(0.2f), 

	/** A slot for weapons or shields carried in the right hand. Armor modifier 0.2. */
	HAND_RIGHT(0.2f),
	
	// jewelry
	/** Slot for amulets or necklaces. */
	NECK, 
	
	/** Slot for rings worn on the left hand. */
	RING_LEFT, 
	
	/** Slot for rings worn on the right hand. */
	RING_RIGHT, 
	
	/** Slot for e.g. belts or sashes worn around the waist. */
	BELT, 
	
	/** Slot for e.g. glasses or eyepatches. */
	EYES, 
	
	/** Slot for bracers or bracelets. */
	ARMS, 
	
	/** Slot for masks and other facial ornaments. */
	FACE,

	// clothing
	/** Slot for shirts or other upper body garment. Can be combined with a cuirass. */
	SHIRT, 
	
	/** Slot for skirts or trousers. Can be combined with chausses. */
	LEGS, 
	
	/** Slot for a cloak. */
	CLOAK, 
	
	/** Slot for hoods or veils. May be combined with helmets or hats in the head slot. */
	HOOD,
	
	/** Slot for e.g. backpacks. */
	BACK;
	
	public final float modifier;
	
	private Slot() {
		this(0);
	}
	
	private Slot(float modifier) {
		this.modifier = modifier;
	}
}
