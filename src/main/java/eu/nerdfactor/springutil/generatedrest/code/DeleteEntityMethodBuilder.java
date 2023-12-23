package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.AuthenticationInjector;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.NoContentStatementInjector;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;

public class DeleteEntityMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.DELETE, this.configuration.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addDeleteEntityMethod", 1);
		TypeName responseType = this.configuration.getResponseType();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", this.configuration.getRequest() + "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ClassName.get(ResponseEntity.class))
				.addParameter(ParameterSpec.builder(this.configuration.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("DELETE")
				.withType(this.configuration.getEntity())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);
		method.addStatement("this.dataAccessor.deleteDataById(id)");
		method = new NoContentStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
