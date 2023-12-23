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

public class AddEntityMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Post method with the Request Url.
		if (this.configuration.hasExistingRequest(RequestMethod.POST, this.configuration.getRequest())) {
			return builder;
		}
		GeneratedRestUtil.log("addCreateEntityMethod", 1);
		TypeName responseType = this.configuration.getSingleResponseType();
		TypeName requestType = this.configuration.getRequestType();

		// Create a Post method called "create" with the Request Url that takes a Valid
		// object of requestDto from the RequestBody (called "dto") and return an object of
		// responseType.
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("create")
				.addAnnotation(AnnotationSpec.builder(PostMapping.class).addMember("value", "$S", this.configuration.getRequest()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(requestType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);

		// Inject a Security Annotation that will require a role of "CREATE"
		// for the Entity.
		method = new AuthenticationInjector()
				.withMethod("CREATE")
				.withType(this.configuration.getEntity())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);

		// If the method is using DTOs, the object from the ResponseBody will
		// be mapped into the type of the Entity.
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T created = this.dataMapper.map(dto, $T.class)", this.configuration.getEntity(), this.configuration.getEntity());
		} else {
			method.addStatement("$T created = dto", requestType);
		}

		// Create the new Entity with help of the DataAccessor.
		method.addStatement("created = this.dataAccessor.createData(created)");

		// If the method is using DTOs, the created object will be mapped into
		// the responseType. Otherwise, the responseType is equals the entityType
		// and does not need to be mapped.
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T response = this.dataMapper.map(created, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = created", responseType);
		}

		// Inject a return statement that will return the response object in a ResponseEntity
		// that may be wrapped inside the DataWrapper.
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.withResponseVariable("response")
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}
}
