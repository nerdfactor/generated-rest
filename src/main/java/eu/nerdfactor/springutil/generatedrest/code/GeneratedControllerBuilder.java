package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.code.builder.Configurable;
import eu.nerdfactor.springutil.generatedrest.code.builder.MultiStepBuilder;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

public class GeneratedControllerBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration> {

	ControllerConfiguration configuration;

	@Override
	public GeneratedControllerBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	public TypeSpec build() {
		TypeSpec.Builder builder = TypeSpec.classBuilder(configuration.getClassName()).addAnnotation(RestController.class).addModifiers(Modifier.PUBLIC);
		this.and(new GeneratedPropertiesBuilder().withConfiguration(this.configuration));
		this.and(new CrudMethodBuilder().withConfiguration(this.configuration));
		this.and(new ListMethodBuilder().withConfiguration(this.configuration));
		this.and(new SearchMethodBuilder().withConfiguration(this.configuration));
		this.and(new RelationshipMethodBuilder().withConfiguration(this.configuration));
		this.steps.forEach(buildStep -> buildStep.build(builder));
		return builder.build();
	}
}
