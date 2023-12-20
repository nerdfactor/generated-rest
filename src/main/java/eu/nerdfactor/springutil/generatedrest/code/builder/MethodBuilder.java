package eu.nerdfactor.springutil.generatedrest.code.builder;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MethodBuilder implements Buildable<TypeSpec.Builder>, ConfiguredBuilder {

	protected ControllerConfiguration configuration;

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		return builder;
	}

	@Override
	public MethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}
}
