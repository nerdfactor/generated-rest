package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.springutil.generatedrest.code.builder.BuildStep;
import eu.nerdfactor.springutil.generatedrest.code.builder.ConfiguredBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.MethodBuilder;
import eu.nerdfactor.springutil.generatedrest.code.builder.MultiStepBuilder;
import eu.nerdfactor.springutil.generatedrest.config.AccessorType;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.RelationConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.RelationType;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RelationshipMethodBuilder extends MethodBuilder implements MultiStepBuilder, BuildStep, ConfiguredBuilder {

	ControllerConfiguration configuration;
	Queue<BuildStep> steps = new LinkedList<>();

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (!configuration.isWithRelations() || configuration.getRelations() == null || configuration.getRelations().isEmpty()) {
			return builder;
		}
		for (RelationConfiguration relation : configuration.getRelations().values()) {
			if (relation.getType() == RelationType.SINGLE) {
				builder = this.addGetSingleRelationMethod(builder, configuration, relation);
				builder = this.addSetSingleRelationMethod(builder, configuration, relation);
				builder = this.addDeleteSingleRelationMethod(builder, configuration, relation);
			}
			if (relation.getType() == RelationType.MULTIPLE) {
				builder = this.addGetMultipleRelationsMethod(builder, configuration, relation);
				builder = this.addAddToRelationsMethod(builder, configuration, relation);
				builder = this.addDeleteFromRelationsMethod(builder, configuration, relation);
			}
		}
		return builder;
	}

	@Override
	public RelationshipMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	@Override
	public RelationshipMethodBuilder and(BuildStep buildStep) {
		this.steps.add(buildStep);
		return this;
	}

	public TypeSpec.Builder addGetSingleRelationMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetSingleRelationMethod", 1);
		TypeName responseType = relation.isWithDtos() && relation.getDtoClass() != null && !relation.getDtoClass().equals(TypeName.OBJECT) ? relation.getDtoClass() : relation.getEntityClass();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.GET))
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			String security = config.getSecurity().getSecurityString(config, relation, "READ", "READ");
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (relation.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(entity." + relation.getGetter() + "(), $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity." + relation.getGetter() + "()", responseType);
		}
		this.addReturnStatement(method, config, responseType, "response");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addSetSingleRelationMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.POST, config.getRequest() + "/{id}/" + relation.getName()) ||
				config.hasExistingRequest(RequestMethod.PUT, config.getRequest() + "/{id}/" + relation.getName()) ||
				config.hasExistingRequest(RequestMethod.PATCH, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addSetSingleRelationMethod", 1);
		TypeName responseType = relation.isWithDtos() && relation.getDtoClass() != null && !relation.getDtoClass().equals(TypeName.OBJECT) ? relation.getDtoClass() : relation.getEntityClass();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.SET))
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName()).addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class).build())
				.addAnnotation(ResponseBody.class)
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
			String security = config.getSecurity().getSecurityString(config, relation, "UPDATE", "UPDATE");
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (relation.isWithDtos()) {
			method.addStatement("$T rel = this.dataMapper.map(dto, $T.class)", relation.getEntityClass(), relation.getEntityClass());
		} else {
			method.addStatement("$T rel = dto", relation.getEntityClass());
		}
		method.addStatement("entity." + relation.getSetter() + "(rel)");
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
		}
		method.addStatement("return this." + relation.getMethodName(AccessorType.GET) + "(id)");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addDeleteSingleRelationMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.DELETE, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addDeleteSingleRelationMethod", 1);
		TypeName responseType = relation.isWithDtos() && relation.getDtoClass() != null && !relation.getDtoClass().equals(TypeName.OBJECT) ? relation.getDtoClass() : relation.getEntityClass();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.REMOVE))
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName()).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ClassName.get(ResponseEntity.class))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			String security = config.getSecurity().getSecurityString(config, relation, "UPDATE", "UPDATE");
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		method.addStatement("entity." + relation.getSetter() + "(null)");
		this.addNoContentStatement(method, config, responseType);
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addGetMultipleRelationsMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetMultipleRelationsMethod", 1);
		TypeName responseType = relation.isWithDtos() && relation.getDtoClass() != null && !relation.getDtoClass().equals(TypeName.OBJECT) ? relation.getDtoClass() : relation.getEntityClass();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.GET))
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			String security = config.getSecurity().getSecurityString(config, relation, "READ", "READ");
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.beginControlFlow("for($T rel : entity." + relation.getGetter() + "())", relation.getEntityClass());
		if (relation.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(rel, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = rel", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		this.addReturnStatement(method, config, responseType, "responseList");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addAddToRelationsMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.POST, config.getRequest() + "/{id}/" + relation.getName()) ||
				config.hasExistingRequest(RequestMethod.PUT, config.getRequest() + "/{id}/" + relation.getName()) ||
				config.hasExistingRequest(RequestMethod.PATCH, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addAddToRelationsMethod", 1);
		TypeName responseType = relation.isWithDtos() && relation.getDtoClass() != null && !relation.getDtoClass().equals(TypeName.OBJECT) ? relation.getDtoClass() : relation.getEntityClass();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.ADD))
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName()).addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
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
			String security = config.getSecurity().getSecurityString(config, relation, "UPDATE", "UPDATE");
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
		}
		method.addStatement("return this." + relation.getMethodName(AccessorType.ADD) + "ById(id, dto." + relation.getIdAccessor() + "())");
		builder.addMethod(method.build());


		if (config.hasExistingRequest(RequestMethod.POST, config.getRequest() + "/{id}/" + relation.getName() + "/{relationId}") ||
				config.hasExistingRequest(RequestMethod.PUT, config.getRequest() + "/{id}/" + relation.getName() + "/{relationId}") ||
				config.hasExistingRequest(RequestMethod.PATCH, config.getRequest() + "/{id}/" + relation.getName() + "/{relationId}")) {
			return builder;
		}
		MethodSpec.Builder methodById = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.ADD) + "ById")
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName() + "/{relationId}").addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(relation.getIdClass(), "relationId")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			String security = config.getSecurity().getSecurityString(config, relation, "UPDATE", "UPDATE");
			methodById.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		methodById.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		methodById.beginControlFlow("if(entity == null)");
		methodById.addStatement("throw new $T()", EntityNotFoundException.class);
		methodById.endControlFlow();
		methodById.addStatement("$T rel = this.entityManager.getReference($T.class, relationId)", relation.getEntityClass(), relation.getEntityClass());
		methodById.addStatement("entity." + relation.getAdder() + "(rel)");
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			methodById.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
		}
		methodById.addStatement("return this." + relation.getMethodName(AccessorType.GET) + "(id)");
		builder.addMethod(methodById.build());
		return builder;
	}

	public TypeSpec.Builder addDeleteFromRelationsMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.DELETE, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addDeleteFromRelationsMethod", 1);
		TypeName responseType = relation.isWithDtos() && relation.getDtoClass() != null && !relation.getDtoClass().equals(TypeName.OBJECT) ? relation.getDtoClass() : relation.getEntityClass();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.REMOVE))
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
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
			String security = config.getSecurity().getSecurityString(config, relation, "UPDATE", "UPDATE");
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
		}
		method.addStatement("return this." + relation.getMethodName(AccessorType.REMOVE) + "ById(id, dto." + relation.getIdAccessor() + "())");
		builder.addMethod(method.build());

		if (config.hasExistingRequest(RequestMethod.DELETE, config.getRequest() + "/{id}/" + relation.getName() + "/{relationId}")) {
			return builder;
		}
		MethodSpec.Builder methodById = MethodSpec
				.methodBuilder(relation.getMethodName(AccessorType.REMOVE) + "ById")
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", config.getRequest() + "/{id}/" + relation.getName() + "/{relationId}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(config.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(relation.getIdClass(), "relationId")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		if (config.getSecurity() != null) {
			String security = config.getSecurity().getSecurityString(config, relation, "UPDATE", "UPDATE");
			methodById.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		methodById.addStatement("$T entity = this.dataAccessor.readData(id)", config.getEntity());
		methodById.beginControlFlow("if(entity == null)");
		methodById.addStatement("throw new $T()", EntityNotFoundException.class);
		methodById.endControlFlow();
		methodById.addStatement("$T rel = this.entityManager.getReference($T.class, relationId)", relation.getEntityClass(), relation.getEntityClass());
		methodById.addStatement("entity." + relation.getRemover() + "(rel)");
		this.addNoContentStatement(methodById, config, responseType);
		builder.addMethod(methodById.build());
		return builder;
	}
}