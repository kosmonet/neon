package neon.editor;

import neon.system.event.NeonEvent;

public class SelectionEvent extends NeonEvent {
	private final String id;
	
	private SelectionEvent(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
	
	public static class Terrain extends SelectionEvent {
		public Terrain(String id) {
			super(id);
		}
	}
}
