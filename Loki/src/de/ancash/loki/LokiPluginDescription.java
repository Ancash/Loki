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
	
	private final String name;
	private final String version;
	private final String author;
	private final String main;
	
	@SuppressWarnings("deprecation")
	public LokiPluginDescription(InputStream in) throws IOException {
		description = YamlFile.loadConfiguration(in);
		name = description.getString("name");
		version = description.getString("version");
		author = description.getString("author");
		main = description.getString("main");
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
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getMain() {
		return main;
	}
	
	@Override
	public String toString() {
		return String.format("%s v%s by %s", name, version, author);
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