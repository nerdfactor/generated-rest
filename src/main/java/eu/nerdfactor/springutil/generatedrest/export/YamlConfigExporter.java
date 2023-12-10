package eu.nerdfactor.springutil.generatedrest.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.serialization.*;

import javax.annotation.processing.Filer;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;

public class YamlConfigExporter implements GeneratedRestExporter {

	public YamlConfigExporter withFiler(Filer filer) {
		return this;
	}

	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		ConfigFile file = new ConfigFile();
		file.config = config;
		file.controllers = controllers;
		SimpleModule module = new SimpleModule();
		module.addSerializer(ClassName.class, new ClassNameSerializer());
		module.addDeserializer(ClassName.class, new ClassNameDeserializer());
		module.addSerializer(TypeName.class, new TypeNameSerializer());
		module.addDeserializer(TypeName.class, new TypeNameDeserializer());
		module.addSerializer(ParameterizedTypeName.class, new ParameterizedTypeNameSerializer());
		module.addDeserializer(ParameterizedTypeName.class, new ParameterizedTypeNameDeserializer());
		YAMLFactory yamlFactory = YAMLFactory.builder()
				.disable(SPLIT_LINES)
				.build();
		ObjectMapper mapper = new ObjectMapper(yamlFactory);
		mapper.registerModule(module);
		try {
			mapper.writeValue(new File("generated-rest.yaml"), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class ConfigFile {
		public Map<String, String> config;
		public Map<String, ControllerConfiguration> controllers;

		public ConfigFile() {
		}
	}
}
