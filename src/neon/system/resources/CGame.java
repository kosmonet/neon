package neon.system.resources;

import java.util.Set;

public class CGame extends Resource {
	private final Set<String> species;
	private final String title;

	public CGame(String title, Set<String> species) {
		super("game", "config");
		this.species = species;
		this.title = title;
	}
	
	public Set<String> getPlayableSpecies() {
		return species;
	}
	
	/**
	 * @return the title of the current game
	 */
	public String getTitle() {
		return title;
	}
}
