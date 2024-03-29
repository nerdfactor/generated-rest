package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.AuthenticationInjector;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.ReturnStatementInjector;
import eu.nerdfactor.springutil.generatedrest.config.AccessorType;
import eu.nerdfactor.springutil.generatedrest.config.RelationConfiguration;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class GetMultipleRelationsMethodBuilder extends MethodBuilder {

	RelationConfiguration relationConfiguration;

	public GetMultipleRelationsMethodBuilder withRelation(RelationConfiguration relation) {
		this.relationConfiguration = relation;
		return this;
	}

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.GET, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetMultipleRelationsMethod", 1);
		TypeName responseType = this.relationConfiguration.isWithDtos() && this.relationConfiguration.getDtoClass() != null && !this.relationConfiguration.getDtoClass().equals(TypeName.OBJECT) ? this.relationConfiguration.getDtoClass() : this.relationConfiguration.getEntityClass();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.GET))
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(this.configuration.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withType(this.configuration.getEntity())
				.withRelation(this.relationConfiguration.getEntityClass())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);
		method.addStatement("$T entity = this.dataAccessor.readData(id)", this.configuration.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.beginControlFlow("for($T rel : entity." + this.relationConfiguration.getGetter() + "())", this.relationConfiguration.getEntityClass());
		if (this.relationConfiguration.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(rel, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = rel", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.withResponseVariable("responseList")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
