package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.code.builder.Buildable;
import eu.nerdfactor.springutil.generatedrest.code.builder.ConfiguredBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.MultiStepBuilder;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

public class CrudMethodBuilder extends MultiStepBuilder<TypeSpec.Builder> implements ConfiguredBuilder, Buildable<TypeSpec.Builder> {

	protected ControllerConfiguration configuration;

	@Override
	public CrudMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		this.and(new GetEntityMethodBuilder().withConfiguration(this.configuration));
		this.and(new AddEntityMethodBuilder().withConfiguration(this.configuration));
		this.and(new SetEntityMethodBuilder().withConfiguration(this.configuration));
		this.and(new UpdateEntityMethodBuilder().withConfiguration(this.configuration));
		this.and(new DeleteEntityMethodBuilder().withConfiguration(this.configuration));
		this.steps.forEach(buildStep -> buildStep.build(builder));
		return builder;
	}

}
