package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.code.builder.Buildable;
import eu.nerdfactor.springutil.generatedrest.code.builder.Configurable;
import eu.nerdfactor.springutil.generatedrest.code.builder.MultiStepBuilder;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.RelationConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.RelationType;
import org.jetbrains.annotations.NotNull;

public class RelationshipMethodBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration>, Buildable<TypeSpec.Builder> {

	protected ControllerConfiguration configuration;

	@Override
	public RelationshipMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (!configuration.isUsingRelations() || configuration.getRelations() == null || configuration.getRelations().isEmpty()) {
			return builder;
		}
		for (RelationConfiguration relation : configuration.getRelations().values()) {
			if (relation.getType() == RelationType.SINGLE) {
				this.and(new GetSingleRelationMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new SetSingleRelationMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new DeleteSingleRelationMethodBuilder().withRelation(relation).withConfiguration(configuration));
			}
			if (relation.getType() == RelationType.MULTIPLE) {
				this.and(new GetMultipleRelationsMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new AddToRelationsMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new DeleteFromRelationsMethodBuilder().withRelation(relation).withConfiguration(configuration));
			}
		}
		this.steps.forEach(buildStep -> buildStep.build(builder));
		return builder;
	}
}