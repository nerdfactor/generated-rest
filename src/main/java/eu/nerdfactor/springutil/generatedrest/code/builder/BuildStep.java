package eu.nerdfactor.springutil.generatedrest.code.builder;

import com.squareup.javapoet.TypeSpec;

public interface BuildStep {

	TypeSpec.Builder build(TypeSpec.Builder builder);
}
