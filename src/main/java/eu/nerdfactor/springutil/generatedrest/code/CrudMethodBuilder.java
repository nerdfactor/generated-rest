package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;

public class CrudMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		builder = this.addGetEntityMethod(builder, configuration);
		builder = this.addCreateEntityMethod(builder, configuration);
		builder = this.addSetEntityMethod(builder, configuration);
		builder = this.addUpdateEntityMethod(builder, configuration);
		builder = this.addDeleteEntityMethod(builder, configuration);
		return builder;
	}

	public TypeSpec.Builder addGetEntityMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addGetEntityMethod", 1);
		TypeName responseType = config.getResponse();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", config.getRequest() + "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("READ", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		this.addReturnStatement(method, config, responseType, "response");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addCreateEntityMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.POST, config.getRequest())) {
			return builder;
		}
		GeneratedRestUtil.log("addCreateEntityMethod", 1);
		TypeName responseType = config.getResponse();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("create")
				.addAnnotation(AnnotationSpec.builder(PostMapping.class).addMember("value", "$S", config.getRequest()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(responseType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("CREATE", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		if (config.isWithDtos()) {
			method.addStatement("$T created = this.dataMapper.map(dto, $T.class)", config.getEntity(), config.getEntity());
		} else {
			method.addStatement("$T created = dto", responseType);
		}
		method.addStatement("created = this.dataAccessor.createData(created)");
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(created, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = created", responseType);
		}
		this.addReturnStatement(method, config, responseType, "response");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addSetEntityMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.PUT, config.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addSetEntityMethod", 1);
		TypeName responseType = config.getResponse();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("set")
				.addAnnotation(AnnotationSpec.builder(PutMapping.class).addMember("value", "$S", config.getRequest() + "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(responseType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("UPDATE", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (config.isWithDtos()) {
			method.addStatement("$T changed = this.dataMapper.map(dto, $T.class)", config.getEntity(), config.getEntity());
		} else {
			method.addStatement("$T changed = dto", config.getEntity());
		}
		method.addStatement("changed = this.dataAccessor.updateData(changed)");
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(changed, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = changed", responseType);
		}
		this.addReturnStatement(method, config, responseType, "response");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addUpdateEntityMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.PATCH, config.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addUpdateEntityMethod", 1);
		TypeName responseType = config.getResponse();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("update")
				.addAnnotation(AnnotationSpec.builder(PatchMapping.class).addMember("value", "$S", config.getRequest() + "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(responseType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("UPDATE", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (config.isWithDtos()) {
			method.addStatement("$T changed = this.dataMapper.map(dto, $T.class)", config.getEntity(), config.getEntity());
		} else {
			method.addStatement("$T changed = dto", config.getEntity());
		}
		method.addStatement("$T updated = this.dataMerger.merge(entity, changed)", config.getEntity());
		method.addStatement("updated = this.dataAccessor.updateData(updated)");
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(updated, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = updated", responseType);
		}
		this.addReturnStatement(method, config, responseType, "response");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addDeleteEntityMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.DELETE, config.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addDeleteEntityMethod", 1);
		TypeName responseType = config.getResponse();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", config.getRequest() + "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ClassName.get(ResponseEntity.class))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("DELETE", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("this.dataAccessor.deleteDataById(id)");
		this.addNoContentStatement(method, config, responseType);
		builder.addMethod(method.build());
		return builder;
	}

}
