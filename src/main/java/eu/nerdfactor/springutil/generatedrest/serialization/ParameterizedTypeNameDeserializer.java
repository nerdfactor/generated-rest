package eu.nerdfactor.springutil.generatedrest.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import java.io.IOException;
import java.util.Arrays;

public class ParameterizedTypeNameDeserializer extends StdDeserializer<ParameterizedTypeName> {

	public ParameterizedTypeNameDeserializer() {
		this(null);
	}

	public ParameterizedTypeNameDeserializer(Class<ParameterizedTypeName> t) {
		super(t);
	}

	@Override
	public ParameterizedTypeName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		// todo: this will only work with one level of parameters (ie. Map<String, String>) but not more (ie. Map<String, Map<String, String>>).
		JsonNode node = jsonParser.readValueAsTree();
		String name = node.asText();
		String typeName = name.substring(0, name.indexOf('<'));
		ClassName[] argumentNames = Arrays.stream(name.substring(name.indexOf('<') + 1).split(","))
				.map(s -> ClassName.bestGuess(s.trim().replace(">", "")))
				.toArray(ClassName[]::new);
		return ParameterizedTypeName.get(ClassName.bestGuess(typeName), argumentNames);
	}
}
