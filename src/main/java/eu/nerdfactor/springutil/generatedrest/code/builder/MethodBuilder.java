package eu.nerdfactor.springutil.generatedrest.code.builder;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MethodBuilder implements BuildStep, ConfiguredBuilder {

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

	protected MethodSpec.Builder addReturnStatement(MethodSpec.Builder method, ControllerConfiguration config, TypeName responseType, String variable) {
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
			method.addStatement("$T<$T> wrapper = new $T<>()", config.getDataWrapper(), responseType, config.getDataWrapper());
			method.addStatement("wrapper.setContent(" + variable + ")");
			method.addStatement("return new $T<>(wrapper, $T.OK)", ResponseEntity.class, HttpStatus.class);
		} else {
			method.addStatement("return new $T<>(" + variable + ", $T.OK)", ResponseEntity.class, HttpStatus.class);
		}
		return method;
	}

	protected MethodSpec.Builder addNoContentStatement(MethodSpec.Builder method, ControllerConfiguration config, TypeName responseType) {
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
			method.addStatement("$T<$T> wrapper = new $T<>()", config.getDataWrapper(), responseType, config.getDataWrapper());
			method.addStatement("wrapper.noContent()");
			method.addStatement("return new $T<>(wrapper, $T.OK)", ResponseEntity.class, HttpStatus.class);
		} else {
			method.addStatement("return ResponseEntity.noContent().build()", ResponseEntity.class);
		}
		return method;
	}
}
