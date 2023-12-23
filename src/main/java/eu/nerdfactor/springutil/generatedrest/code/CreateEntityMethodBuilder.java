package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.AuthenticationInjector;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.ReturnStatementInjector;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

public class CreateEntityMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Post method with the Request Url.
		if (this.configuration.hasExistingRequest(RequestMethod.POST, this.configuration.getRequest())) {
			return builder;
		}
		GeneratedRestUtil.log("addCreateEntityMethod", 1);

		// Get the type of incoming and outgoing objects.
		TypeName responseType = this.configuration.getSingleResponseType();
		TypeName requestType = this.configuration.getRequestType();

		// Create the method declaration.
		MethodSpec.Builder method = this.createMethodDeclaration(this.configuration.getRequest(), requestType, responseType);

		// Inject a Security Annotation that will require a role of "CREATE"
		// for the Entity.
		new AuthenticationInjector().withMethod("CREATE")
				.withType(this.configuration.getEntity())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);

		// Add the method body.
		this.addMethodBody(method, this.configuration.getEntity(), requestType, responseType, this.configuration.isUsingDto());

		// Inject a return statement that will return the response object in a ResponseEntity
		// that may be wrapped inside the DataWrapper.
		new ReturnStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Post method called "create" with the requestUrl that takes a Valid
	 * object of requestType from the RequestBody (called "dto") and will return an
	 * ResponseEntity with an object of responseType.
	 *
	 * @param requestUrl   The requested Url.
	 * @param requestType  The type of object inside the RequestBody.
	 * @param responseType The type of object of the response.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	public MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName requestType, TypeName responseType) {
		return MethodSpec.methodBuilder("create")
				.addAnnotation(AnnotationSpec.builder(PostMapping.class).addMember("value", "$S", requestUrl).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(requestType, "dto").addAnnotation(RequestBody.class).addAnnotation(Valid.class).build());
	}

	/**
	 * Add a method body that creates a new Entity from the object in the RequestBody
	 * with the help of the DataAccessor and return the result.
	 *
	 * @param method       The existing {@link MethodSpec.Builder}.
	 * @param entityType   The type of the Entity.
	 * @param requestType  The type of object inside the RequestBody.
	 * @param responseType The type of object of the response.
	 * @param isUsingDto   If the method is using DTOs.
	 */
	public void addMethodBody(MethodSpec.Builder method, TypeName entityType, TypeName requestType, TypeName responseType, boolean isUsingDto) {
		// If the method is using DTOs, the object from the RequestBody will
		// be mapped into the type of the Entity.
		if (isUsingDto) {
			method.addStatement("$T created = this.dataMapper.map(dto, $T.class)", entityType, entityType);
		} else {
			method.addStatement("$T created = dto", requestType);
		}

		// Create the new Entity with help of the DataAccessor.
		method.addStatement("created = this.dataAccessor.createData(created)");

		// If the method is using DTOs, the created object will be mapped into
		// the responseType. Otherwise, the responseType is equals to the entityType
		// and does not need to be mapped.
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(created, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = created", responseType);
		}
	}
}
