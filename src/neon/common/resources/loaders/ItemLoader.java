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

package neon.common.resources.loaders;

import org.jdom2.Element;

import javafx.scene.paint.Color;
import neon.common.entity.ArmorType;
import neon.common.entity.Slot;
import neon.common.resources.RItem;
import neon.systems.magic.Effect;

/**
 * A resource loader specifically for items.
 * 
 * @author mdriesen
 *
 */
public final class ItemLoader implements ResourceLoader<RItem> {
	@Override
	public RItem load(Element root) {
		String name = root.getAttributeValue("name");
		String id = root.getAttributeValue("id");
		char glyph = root.getChild("graphics").getAttributeValue("char").charAt(0);
		Color color = Color.web(root.getChild("graphics").getAttributeValue("color"));
		
		RItem.Builder builder = new RItem.Builder(id, name).setGraphics(glyph, color);
		
		if (root.getAttribute("price") != null) {
			builder.setPrice(Integer.parseInt(root.getAttributeValue("price")));
		}
		
		if (root.getAttribute("weight") != null) {
			builder.setWeight(Integer.parseInt(root.getAttributeValue("weight")));
		}
		
		Element magic = root.getChild("magic");
		if (magic != null) {
			Effect effect = Effect.valueOf(magic.getAttributeValue("effect").toUpperCase());
			int magnitude = Integer.parseInt(magic.getAttributeValue("magnitude"));
			builder.setEnchantment(effect, magnitude);
		}
		
		switch (root.getName()) {
		case "armor":
			return createArmor(root, builder);
		case "clothing":
			return createClothing(root, builder);
		case "weapon":
			return createWeapon(root, builder);
		case "coin":
			return new RItem.Coin(builder);
		case "container":
			return new RItem.Container(builder);
		default:
			return new RItem(builder);
		}
	}
	
	private RItem.Weapon createWeapon(Element root, RItem.Builder builder) {
		Element weapon = root.getChild("weapon");
		return new RItem.Weapon(builder.setDamage(weapon.getAttributeValue("dmg")));		
	}
	
	private RItem.Clothing createClothing(Element root, RItem.Builder builder) {
		Element clothing = root.getChild("clothing");
		builder.setSlot(Slot.valueOf(clothing.getAttributeValue("slot").toUpperCase()));
		return new RItem.Clothing(builder);		
	}
	
	private RItem.Armor createArmor(Element root, RItem.Builder builder) {
		Element clothing = root.getChild("clothing");
		Slot slot = Slot.valueOf(clothing.getAttributeValue("slot").toUpperCase());
		Element armor = root.getChild("armor");
		int rating = Integer.parseInt(armor.getAttributeValue("rating"));
		ArmorType type = ArmorType.valueOf(armor.getAttributeValue("type").toUpperCase());
		return new RItem.Armor(builder.setSlot(slot).setRating(rating).setArmorType(type));		
	}
	
	@Override
	public Element save(RItem ri) {
		Element item = new Element("item");
		item.setAttribute("id", ri.id);
		item.setAttribute("name", ri.name);
		return item;
	}
}
