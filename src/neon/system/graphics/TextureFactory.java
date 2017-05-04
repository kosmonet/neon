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

package neon.system.graphics;

import java.util.HashMap;

import com.google.common.base.Objects;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

class TextureFactory {
	private final static HashMap<Type, Image> map = new HashMap<>();
	private final static SnapshotParameters parameters = new SnapshotParameters();
	private final static Color bg = Color.BLACK.deriveColor(0, 0, 0, 0.6);
	
	static {
		parameters.setFill(Color.TRANSPARENT);
	}
	
	static Image getImage(int size, Paint paint, String text) {
		Type type = new Type(size, paint, text);
		if (map.containsKey(type)) {
			return map.get(type);
		} else {
			Canvas canvas = new Canvas(size, size);
			GraphicsContext gc = canvas.getGraphicsContext2D();
			gc.setFill(bg);
			gc.fillRect(0, 0, size, size);
			gc.setFill(paint);
			gc.fillText(text, 10, 10);

			WritableImage image = new WritableImage(size, size);
			map.put(type, canvas.snapshot(parameters, image));
			return image;
		}
	}
	
	private static class Type {
		private final int size;
		private final Paint paint;
		private final String text;
		private final int hash;
		
		private Type(int size, Paint paint, String text) {
			this.size = size;
			this.paint = paint;
			this.text = text;
			hash = Objects.hashCode(size, paint, text);
		}
		
		@Override
		public boolean equals(Object object) {
			if (!Type.class.isInstance(object)) {
				return false;
			} else {
				Type type = (Type) object;
				return text.equals(type.text) && paint.equals(type.paint) && size == type.size;				
			}
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
	}
}
