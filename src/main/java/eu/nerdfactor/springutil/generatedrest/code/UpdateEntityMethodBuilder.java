package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.AuthenticationInjector;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.ReturnStatementInjector;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

public class UpdateEntityMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.PATCH, this.configuration.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addUpdateEntityMethod", 1);
		TypeName responseType = this.configuration.getResponse();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("update")
				.addAnnotation(AnnotationSpec.builder(PatchMapping.class).addMember("value", "$S", this.configuration.getRequest() + "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(this.configuration.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(responseType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("UPDATE")
				.withType(this.configuration.getEntity())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);
		method.addStatement("$T entity = this.dataAccessor.readData(id)", this.configuration.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T changed = this.dataMapper.map(dto, $T.class)", this.configuration.getEntity(), this.configuration.getEntity());
		} else {
			method.addStatement("$T changed = dto", this.configuration.getEntity());
		}
		method.addStatement("$T updated = this.dataMerger.merge(entity, changed)", this.configuration.getEntity());
		method.addStatement("updated = this.dataAccessor.updateData(updated)");
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T response = this.dataMapper.map(updated, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = updated", responseType);
		}
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.withResponseVariable("response")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
