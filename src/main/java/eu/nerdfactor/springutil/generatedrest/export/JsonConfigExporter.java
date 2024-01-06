package eu.nerdfactor.springutil.generatedrest.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JsonConfigExporter implements GeneratedRestExporter {

	public JsonConfigExporter withFiler(Filer filer) {
		return this;
	}

	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		GeneratedConfigFile file = new GeneratedConfigFile();
		file.config = config;
		file.controllers = controllers;
		ObjectMapper mapper = ConfigMapper.forJson();
		try {
			mapper.writeValue(new File("generated-rest.json"), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
