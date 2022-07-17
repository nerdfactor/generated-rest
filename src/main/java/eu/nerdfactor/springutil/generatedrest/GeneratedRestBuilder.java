package eu.nerdfactor.springutil.generatedrest;

import com.squareup.javapoet.*;
import com.turkraft.springfilter.boot.Filter;
import eu.nerdfactor.springutil.generatedrest.config.AccessorType;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.RelationConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.RelationType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds TypeSpec and MethodSpec for generated rest controllers.
 *
 * @author Daniel Klug
 */
public class GeneratedRestBuilder {

	/**
	 * Build the TypeSpec for a controller from the configuration.
	 *
	 * @param configuration The controller configuration.
	 * @return The new TypeSpec.
	 */
	public TypeSpec buildController(ControllerConfiguration configuration) {
		// Create the new class.
		TypeSpec.Builder builder = TypeSpec
				.classBuilder(configuration.getClassName())
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		// Add basic elements for entity access.
		builder = this.addElementsForEntityManager(builder);
		builder = this.addElementsForDataAccessor(builder, configuration.getDataAccessorClass());
		if (configuration.getDataMapperClass() != null) {
			builder = this.addElementsForDataMapper(builder, configuration.getDataMapperClass());
		}
		if (configuration.getDataMergerClass() != null) {
			builder = this.addElementsForDataMerger(builder, configuration.getDataMergerClass());
		}

		// Add elements to load and search entities.
		builder = this.addGetAllEntitiesMethod(builder, configuration);
		builder = this.addSearchEntitiesMethod(builder, configuration);

		// Add crud elements for entity.
		builder = this.addGetEntityMethod(builder, configuration);
		builder = this.addCreateEntityMethod(builder, configuration);
		builder = this.addSetEntityMethod(builder, configuration);
		builder = this.addUpdateEntityMethod(builder, configuration);
		builder = this.addDeleteEntityMethod(builder, configuration);

		if (configuration.isWithRelations() && configuration.getRelations() != null && configuration.getRelations().size() > 0) {
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
		}

		return builder.build();
	}

	/**
	 * Creates a fields and methods for a DataMapper and adds it to the TypeSpec.
	 *
	 * @param builder The builder for a TypeSpec.
	 * @param dataMapperClass The TypeName for the DataMapper.
	 * @return The builder with added fields and methods.
	 */
	public TypeSpec.Builder addElementsForDataMapper(TypeSpec.Builder builder, TypeName dataMapperClass) {
		builder.addField(FieldSpec
				.builder(dataMapperClass, "dataMapper", Modifier.PROTECTED)
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("getDataMapper")
				.addModifiers(Modifier.PUBLIC)
				.returns(dataMapperClass)
				.addStatement("return this.dataMapper")
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("setDataMapper")
				.addAnnotation(Autowired.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(dataMapperClass, "dataMapper")
				.addStatement("this.dataMapper = dataMapper")
				.build());
		return builder;
	}

	/**
	 * Creates a fields and methods for a DataAccessor and adds it to the TypeSpec.
	 *
	 * @param builder The builder for a TypeSpec.
	 * @param dataAccessorClass The TypeName for the DataAccessor.
	 * @return The builder with added fields and methods.
	 */
	public TypeSpec.Builder addElementsForDataAccessor(TypeSpec.Builder builder, ParameterizedTypeName dataAccessorClass) {
		builder.addField(FieldSpec
				.builder(dataAccessorClass, "dataAccessor", Modifier.PROTECTED)
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("getDataAccessor")
				.addModifiers(Modifier.PUBLIC)
				.returns(dataAccessorClass)
				.addStatement("return this.dataAccessor")
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("setDataAccessor")
				.addAnnotation(Autowired.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(dataAccessorClass, "dataAccessor")
				.addStatement("this.dataAccessor = dataAccessor")
				.build());
		return builder;
	}

	/**
	 * Creates a fields and methods for a DataMerger and adds it to the TypeSpec.
	 *
	 * @param builder The builder for a TypeSpec.
	 * @param dataMergerClass The TypeName for the DataMapper.
	 * @return The builder with added fields and methods.
	 */
	public TypeSpec.Builder addElementsForDataMerger(TypeSpec.Builder builder, TypeName dataMergerClass) {
		builder.addField(FieldSpec
				.builder(dataMergerClass, "dataMerger", Modifier.PROTECTED)
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("getDataMerger")
				.addModifiers(Modifier.PUBLIC)
				.returns(dataMergerClass)
				.addStatement("return this.dataMerger")
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("setDataMerger")
				.addAnnotation(Autowired.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(dataMergerClass, "dataMerger")
				.addStatement("this.dataMerger = dataMerger")
				.build());
		return builder;
	}

	/**
	 * Creates a fields and methods for a jpa EntityManager and adds it to the TypeSpec.
	 *
	 * @param builder The builder for a TypeSpec.
	 * @return The builder with added fields and methods.
	 */
	public TypeSpec.Builder addElementsForEntityManager(TypeSpec.Builder builder) {
		builder.addField(FieldSpec
				.builder(EntityManager.class, "entityManager", Modifier.PROTECTED)
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("getEntityManager")
				.addModifiers(Modifier.PUBLIC)
				.returns(EntityManager.class)
				.addStatement("return this.entityManager")
				.build());

		builder.addMethod(MethodSpec
				.methodBuilder("setEntityManager")
				.addAnnotation(Autowired.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(EntityManager.class, "entityManager")
				.addStatement("this.entityManager = entityManager")
				.build());
		return builder;
	}

	public TypeSpec.Builder addGetAllEntitiesMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetAllEntitiesMethod");
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
		method.beginControlFlow("for($T entity : this.getDataAccessor().listData())", config.getEntity());
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(entity, $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addSearchAllEntitiesMethod");
		TypeName responseType = config.getResponse();
		ParameterizedTypeName responsePage = ParameterizedTypeName.get(ClassName.get(Page.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("searchAll")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", config.getRequest() + "/search").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responsePage))
				.addParameter(ParameterSpec.builder(
								ParameterizedTypeName.get(ClassName.get(Specification.class), config.getEntity()), "spec")
						.addAnnotation(AnnotationSpec.builder(Filter.class).addMember("parameterName", "$S", "query").build()).
						build()
				)
				.addParameter(ParameterSpec.builder(
								Pageable.class, "pageable")
						.addAnnotation(AnnotationSpec.builder(PageableDefault.class).addMember("size", "20").build()).
						build()
				);
		if (config.getSecurity() != null) {
			ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
			String role = config.getSecurity().getRole("READ", entityName.simpleName(), entityName.simpleName());
			String security = "hasRole('" + role + "')";
			method.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		}
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.addStatement("$T page = this.getDataAccessor().searchData(spec, pageable)", ParameterizedTypeName.get(ClassName.get(Page.class), config.getEntity()));
		method.beginControlFlow("for($T entity : page.getContent())", config.getEntity());
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method.addStatement("$T<$T> responsePage = new $T<>(responseList, page.getPageable(), page.getTotalElements())", Page.class, responseType, PageImpl.class);
		this.addReturnStatement(method, config, responseType, "responsePage");
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addGetEntityMethod(@NotNull TypeSpec.Builder builder, @NotNull ControllerConfiguration config) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest() + "/{id}")) {
			return builder;
		}
		GeneratedRestUtil.log("addGetEntityMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(entity, $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addCreateEntityMethod");
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
			method.addStatement("$T created = this.getDataMapper().map(dto, $T.class)", config.getEntity(), config.getEntity());
		} else {
			method.addStatement("$T created = dto", responseType);
		}
		method.addStatement("created = this.getDataAccessor().createData(created)");
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(created, $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addSetEntityMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (config.isWithDtos()) {
			method.addStatement("$T changed = this.getDataMapper().map(dto, $T.class)", config.getEntity(), config.getEntity());
		} else {
			method.addStatement("$T changed = dto", config.getEntity());
		}
		method.addStatement("changed = this.getDataAccessor().updateData(changed)");
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(changed, $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addUpdateEntityMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (config.isWithDtos()) {
			method.addStatement("$T changed = this.getDataMapper().map(dto, $T.class)", config.getEntity(), config.getEntity());
		} else {
			method.addStatement("$T changed = dto", config.getEntity());
		}
		method.addStatement("$T updated = this.getDataMerger().merge(entity, changed)", config.getEntity());
		method.addStatement("updated = this.getDataAccessor().updateData(updated)");
		if (config.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(updated, $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addDeleteEntityMethod");
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
		method.addStatement("this.getDataAccessor().deleteDataById(id)");
		this.addNoContentStatement(method, config, responseType);
		builder.addMethod(method.build());
		return builder;
	}

	public TypeSpec.Builder addGetSingleRelationMethod(TypeSpec.Builder builder, ControllerConfiguration config, RelationConfiguration relation) {
		if (config.hasExistingRequest(RequestMethod.GET, config.getRequest() + "/{id}/" + relation.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetSingleRelationMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (relation.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(entity." + relation.getGetter() + "(), $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addSetSingleRelationMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (relation.isWithDtos()) {
			method.addStatement("$T rel = this.getDataMapper().map(dto, $T.class)", relation.getEntityClass(), relation.getEntityClass());
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
		GeneratedRestUtil.log("addDeleteSingleRelationMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
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
		GeneratedRestUtil.log("addGetMultipleRelationsMethod");
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
		method.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.beginControlFlow("for($T rel : entity." + relation.getGetter() + "())", relation.getEntityClass());
		if (relation.isWithDtos()) {
			method.addStatement("$T response = this.getDataMapper().map(rel, $T.class)", responseType, responseType);
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
		GeneratedRestUtil.log("addAddToRelationsMethod");
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
		methodById.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		methodById.beginControlFlow("if(entity == null)");
		methodById.addStatement("throw new $T()", EntityNotFoundException.class);
		methodById.endControlFlow();
		methodById.addStatement("$T rel = this.getEntityManager().getReference($T.class, relationId)", relation.getEntityClass(), relation.getEntityClass());
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
		GeneratedRestUtil.log("addDeleteFromRelationsMethod");
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
		methodById.addStatement("$T entity = this.getDataAccessor().readData(id)", config.getEntity());
		methodById.beginControlFlow("if(entity == null)");
		methodById.addStatement("throw new $T()", EntityNotFoundException.class);
		methodById.endControlFlow();
		methodById.addStatement("$T rel = this.getEntityManager().getReference($T.class, relationId)", relation.getEntityClass(), relation.getEntityClass());
		methodById.addStatement("entity." + relation.getRemover() + "(rel)");
		this.addNoContentStatement(methodById, config, responseType);
		builder.addMethod(methodById.build());
		return builder;
	}

	private MethodSpec.Builder addReturnStatement(MethodSpec.Builder method, ControllerConfiguration config, TypeName responseType, String variable) {
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
			method.addStatement("$T<$T> wrapper = new $T<>()", config.getDataWrapper(), responseType, config.getDataWrapper());
			method.addStatement("wrapper.setContent(" + variable + ")");
			method.addStatement("return new $T<>(wrapper, $T.OK)", ResponseEntity.class, HttpStatus.class);
		} else {
			method.addStatement("return new $T<>(" + variable + ", $T.OK)", ResponseEntity.class, HttpStatus.class);
		}
		return method;
	}

	private MethodSpec.Builder addNoContentStatement(MethodSpec.Builder method, ControllerConfiguration config, TypeName responseType) {
		if (config.getDataWrapper() != null && !config.getDataWrapper().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(config.getDataWrapper().toString()), responseType)));
			method.addStatement("$T<$T> wrapper = new $T<>()", config.getDataWrapper(), responseType, config.getDataWrapper());
			method.addStatement("wrapper.noContent()");
			method.addStatement("return new $T<>(wrapper, $T.OK)", ResponseEntity.class, HttpStatus.class);
		} else {
			method.addStatement("return ResponseEntity.noContent().build()", ResponseEntity.class);
		}
		return method;
	}


}