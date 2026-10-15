package com.ugcs.geohammer.service.script;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JsonScriptMetadataLoaderTest {
	@Test
	void loadsScriptsFromDirectory() throws Exception {
		Path testDir = Paths.get("src/test/resources/scripts");
		ScriptMetadataLoader loader = new JsonScriptMetadataLoader();
		List<ScriptMetadata> scripts = loader.loadScriptMetadata(testDir);
		Assertions.assertFalse(scripts.isEmpty());
	}


	@Test
	void returnsEmptyListForEmptyDirectory() throws Exception {
		Path emptyDir = Files.createTempDirectory("empty-scripts");
		ScriptMetadataLoader loader = new JsonScriptMetadataLoader();
		List<ScriptMetadata> scripts = loader.loadScriptMetadata(emptyDir);
		assertTrue(scripts.isEmpty());
		Files.delete(emptyDir);
	}

	@Test
	void skipsMalformedJson() throws Exception {
		Path tempDir = Files.createTempDirectory("malformed-scripts");
		Path badJson = tempDir.resolve("bad.json");
		Path goodJson = tempDir.resolve("good.json");
		Files.writeString(badJson, "{ this is not valid json }");
		Files.writeString(goodJson, """
				{"filename": "good.py", "display_name": "Good", "parameters": [], "templates": ["csv"]}
				""");
		ScriptMetadataLoader loader = new JsonScriptMetadataLoader();
		List<ScriptMetadata> scripts = loader.loadScriptMetadata(tempDir);
		assertEquals(1, scripts.size());
		assertEquals("good.py", scripts.getFirst().filename());
		Files.delete(badJson);
		Files.delete(goodJson);
		Files.delete(tempDir);
	}

	@Test
	void ignoresNonJsonFiles() throws Exception {
		Path tempDir = Files.createTempDirectory("mixed-scripts");
		Path txtFile = tempDir.resolve("not_a_script.txt");
		Files.writeString(txtFile, "not a script");
		ScriptMetadataLoader loader = new JsonScriptMetadataLoader();
		List<ScriptMetadata> scripts = loader.loadScriptMetadata(tempDir);
		assertTrue(scripts.isEmpty());
		Files.delete(txtFile);
		Files.delete(tempDir);
	}
}
