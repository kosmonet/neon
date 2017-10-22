package neon.editor;

import neon.common.event.NeonEvent;

public class SelectionEvent implements NeonEvent {
	private final String id;
	private final String namespace;
	
	private SelectionEvent(String id, String namespace) {
		this.id = id;
		this.namespace = namespace;
	}
	
	public String getID() {
		return id;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public static class Terrain extends SelectionEvent {
		public Terrain(String id) {
			super(id, "terrain");
		}
	}
}
