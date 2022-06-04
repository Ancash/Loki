package de.ancash.loki;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.ancash.libs.org.simpleyaml.configuration.file.YamlFile;

public class LokiPluginDescription {

	private final YamlFile description;
	
	@SuppressWarnings("deprecation")
	public LokiPluginDescription(InputStream in) throws IOException {
		description = YamlFile.loadConfiguration(in);
		description.set("depend", stringToList(description.getString("depend")));
		description.set("softdepend", stringToList(description.getString("softdepend")));
		description.set("loadbefore", stringToList(description.getString("loadebefore")));
	}
	
	private List<String> stringToList(String str) {
		if(str == null)
			return new ArrayList<>();
		return Arrays.asList(str.replace("[", "").replace("]", "").split(",")).stream().map(s -> s.trim()).collect(Collectors.toList());
	}

	public String getString(String path) {
		return description.getString(path);
	}
	
	public String getName() {
		return getString("name");
	}
	
	public String getVersion() {
		return getString("version");
	}
	
	public String getAuthor() {
		return getString("author");
	}
	
	public String getMain() {
		return getString("main");
	}
	
	public Collection<String> getDepend() {
		return Collections.unmodifiableCollection(description.getStringList("depend"));
	}
	
	public Collection<String> getSoftDepend() {
		return Collections.unmodifiableCollection(description.getStringList("softdepend"));
	}
	
	public Collection<String> getLoadBefore() {
		return Collections.unmodifiableCollection(description.getStringList("loadbefore"));
	}
	
	public boolean isValid() {
		return getName() != null
				&& getVersion() != null
				&& getMain() != null;
	}
}