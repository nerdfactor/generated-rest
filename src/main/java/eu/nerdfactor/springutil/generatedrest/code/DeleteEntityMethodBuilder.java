package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.AuthenticationInjector;
import eu.nerdfactor.springutil.generatedrest.code.builder.Buildable;
import eu.nerdfactor.springutil.generatedrest.code.builder.Configurable;
import eu.nerdfactor.springutil.generatedrest.code.builder.NoContentStatementInjector;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.SecurityConfiguration;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	protected boolean hasExistingRequest;
	protected String requestUrl;
	protected TypeName entityType;
	protected TypeName identifyingType;
	protected SecurityConfiguration securityConfiguration;
	protected TypeName dataWrapperClass;

	public static DeleteEntityMethodBuilder create() {
		return new DeleteEntityMethodBuilder();
	}

	@Override
	public DeleteEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return new DeleteEntityMethodBuilder(
				configuration.hasExistingRequest(RequestMethod.DELETE, configuration.getRequest() + "/{id}"),
				configuration.getRequest() + "/{id}",
				configuration.getEntity(),
				configuration.getId(),
				configuration.getSecurity(),
				configuration.getDataWrapperClass()
		);
	}

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.hasExistingRequest) {
			return builder;
		}
		GeneratedRestUtil.log("addDeleteEntityMethod", 1);

		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.identifyingType);

		new AuthenticationInjector()
				.withMethod("DELETE")
				.withType(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		this.addMethodBody(method);

		new NoContentStatementInjector()
				.withWrapper(this.dataWrapperClass)
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Delete method called "delete" with the requestUrl that takes
	 * an identifyingType (called "id") from the PathVariable and will return
	 * a ResponseEntity.
	 *
	 * @param requestUrl      The requested Url.
	 * @param identifyingType The type of object identifying the Entity.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	protected MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName identifyingType) {
		return MethodSpec.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", requestUrl).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ResponseEntity.class)
				.addParameter(ParameterSpec.builder(identifyingType, "id").addModifiers(Modifier.FINAL).addAnnotation(PathVariable.class).build());
	}

	protected void addMethodBody(MethodSpec.Builder method) {
		method.addStatement("this.dataAccessor.deleteDataById(id)");
	}
}
