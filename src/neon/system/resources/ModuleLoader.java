package neon.system.resources;

import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

public class ModuleLoader implements ResourceLoader {
	@Override
	public RModule load(Element root) {
		RModule module = new RModule("module", root.getChildText("title"));
		
		Set<String> species = new HashSet<>();		
		Element playable = root.getChild("playable");
		if (playable != null) {
			for (Element id : playable.getChildren()) {
				species.add(id.getText());
			}
		}
		module.addSpecies(species);
		
		return module;	
	}

	@Override
	public Element save(Resource resource) {
		// TODO Auto-generated method stub
		return null;
	}
}
