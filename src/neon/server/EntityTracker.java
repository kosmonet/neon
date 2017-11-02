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

package neon.server;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.jdom2.Document;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import neon.common.files.NeonFileSystem;
import neon.common.files.XMLTranslator;
import neon.common.resources.ResourceException;
import neon.common.resources.ResourceManager;
import neon.entity.EntityProvider;
import neon.entity.entities.Entity;

/**
 * This class tracks all loaded entities in the current game (and by extension
 * all loaded modules and maps). 
 * 
 * @author mdriesen
 *
 */
class EntityTracker implements EntityProvider, RemovalListener<Long, Entity> {
	private final Cache<Long, Entity> entities = CacheBuilder.newBuilder().removalListener(this).softValues().build();
	private final EntitySaver saver;
	private final NeonFileSystem files;
	
	EntityTracker(NeonFileSystem files, ResourceManager resources) {
		this.files = files;
		saver = new EntitySaver(resources);	
	}
	
	@Override @SuppressWarnings("unchecked")
	public <T extends Entity> T getEntity(long uid) {
		try {
			return (T) entities.get(uid, () -> loadEntity(uid));
		} catch (ExecutionException e) {
			throw new IllegalArgumentException("No entity with uid <" + uid + "> found", e);
		}
	}
	
	void addEntity(Entity entity) {
		entities.put(entity.uid, entity);
	}
	
	@Override
	public Collection<Entity> getEntities() {
		return entities.asMap().values();
	}
	
	/**
	 * 
	 * @param entity
	 * @return	the uid of the module an entity belongs to
	 */
	short getModuleUID(long entity) {
		return (short) (entity >>> 48);		
	}

	@Override
	public void onRemoval(RemovalNotification<Long, Entity> notification) {
		System.out.println("entity removed from tracker");
		saveEntity(notification.getValue());
	}
	
	/**
	 * Stores all remaining entities in the cache in the temp folder on disk.
	 */
	void flush() {
		for (Entity entity : entities.asMap().values()) {
			saveEntity(entity);
		}
	}
	
	private void saveEntity(Entity entity) {
		Document doc = new Document(saver.save(entity));
		try {
			files.saveFile(doc, new XMLTranslator(), "entities", Long.toString(entity.uid) + ".xml");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private Entity loadEntity(long uid) throws IOException, ResourceException {
		Document doc = files.loadFile(new XMLTranslator(), "entities", Long.toString(uid) + ".xml");
		return saver.load(uid, doc.getRootElement());
	}
}
