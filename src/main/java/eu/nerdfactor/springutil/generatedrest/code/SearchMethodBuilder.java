package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.data.DataPage;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SearchMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		builder = this.addGetAllEntitiesMethod(builder, configuration);
		builder = this.addSearchEntitiesMethod(builder, configuration);
		return builder;
	}

	public TypeSpec.Builder addGetAllEntitiesMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetAllEntitiesMethod", 1);
		TypeName responseType = config.getResponse();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("all")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", config.getRequest()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList));
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("READ", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.beginControlFlow("for($T entity : this.dataAccessor.listData())", config.getEntity());
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		this.addReturnStatement(method, config, responseType, "responseList");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addSearchEntitiesMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest() + "/search")) {
			return builder;
		}
		GeneratedRestUtil.log("addSearchAllEntitiesMethod", 1);
		TypeName responseType = config.getResponse();
		ParameterizedTypeName responsePage = ParameterizedTypeName.get(ClassName.get(Page.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("searchAll")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", config.getRequest() + "/search").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responsePage))
				.addParameter(ParameterSpec.builder(String.class, "filter")
						.addAnnotation(AnnotationSpec.builder(RequestParam.class)
								.addMember("required", "false").
								build()
						)
						.build()
				)
				.addParameter(ParameterSpec.builder(Pageable.class, "pageable")
						.addAnnotation(AnnotationSpec.builder(PageableDefault.class).addMember("size", "20").build()).
						build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("READ", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T<$T> spec = this.specificationBuilder.build(filter, $T.class)", Specification.class, config.getEntity(), config.getEntity());
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.addStatement("$T page = this.dataAccessor.searchData(spec, pageable)", ParameterizedTypeName.get(ClassName.get(Page.class), config.getEntity()));
		method.beginControlFlow("for($T entity : page.getContent())", config.getEntity());
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method.addStatement("$T<$T> responsePage = new $T<>(responseList, page.getPageable(), page.getTotalElements())", Page.class, responseType, DataPage.class);
		this.addReturnStatement(method, config, responseType, "responsePage");
		builder.addMethod(method.build());
		return builder;
	}
}