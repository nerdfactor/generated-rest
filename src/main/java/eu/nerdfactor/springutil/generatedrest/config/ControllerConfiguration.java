package eu.nerdfactor.springutil.generatedrest.config;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for controller generation.
 *
 * @author Daniel Klug
 */
@Getter
public class ControllerConfiguration {

	/**
	 * Class name for the controller.
	 */
	private ClassName className;

	/**
	 * Base path for requests to the generated controller.
	 * Will be added in a RequestMapping annotation on the class.
	 */
	private String request;

	/**
	 * Class of the entity that will be accessed by the generated controller.
	 */
	private TypeName entity;

	/**
	 * Type of the id for the accessed entity.
	 */
	private TypeName id;

	private String idAccessor = "getId";

	/**
	 * The generated controller can use data access objects in responses.
	 */
	private boolean withDtos = false;

	/**
	 * Class of the data transfer objet that may be in the response of the
	 * generated controller.
	 */
	private TypeName dto = TypeName.OBJECT;

	/**
	 * Class of a data accessor that can be used to access entities.
	 * This can be a DataAccessController, DataAccessService, DataAccessRepository
	 * or any other class implementing DataAccessor.
	 */
	private ParameterizedTypeName dataAccessorClass;

	/**
	 * Class of a data mapper that will be used to map between entities and dtos.
	 * This can be one of the well known DataMappers or any other class implementing DataMapper.
	 */
	private TypeName dataMapperClass;

	private TypeName dataMergerClass;

	@Setter
	private SecurityConfiguration security;

	private List<String> existingRequests;

	private TypeName dataWrapperClass;

	/**
	 * Map of relations that will be added to the controller.
	 */
	private Map<String, RelationConfiguration> relations = new HashMap<>();

	public ControllerConfiguration() {
	}

	public ControllerConfiguration(@NotNull ClassName className, @NotNull String request,
	                               @NotNull TypeName entity, @NotNull TypeName id, @NotNull String idAccessor,
	                               boolean withDtos, TypeName dto,
	                               @NotNull ParameterizedTypeName dataAccessorClass,
	                               TypeName dataMapperClass, TypeName dataMergerClass,
	                               @Nullable Map<String, RelationConfiguration> relations,
	                               List<String> existingRequests,
	                               TypeName dataWrapperClass) {
		this.className = className;
		this.request = request;
		this.entity = entity;
		this.id = id;
		this.idAccessor = idAccessor;
		this.dto = dto;
		this.dataAccessorClass = dataAccessorClass;
		this.dataMapperClass = dataMapperClass;
		this.dataMergerClass = dataMergerClass;
		if (relations != null && !relations.isEmpty()) {
			this.relations = relations;
		}
		this.withDtos = withDtos;
		this.existingRequests = existingRequests;
		this.dataWrapperClass = dataWrapperClass;
	}

	/**
	 * Creates a builder for controller configuration.
	 *
	 * @return A new ControllerConfigurationBuilder.
	 */
	public static @NotNull ControllerConfigurationBuilder builder() {
		return new ControllerConfigurationBuilder();
	}


	public boolean hasExistingRequest(RequestMethod method, String request) {
		return this.hasExistingRequest(method.name().toUpperCase(), request);
	}

	public boolean hasExistingRequest(String method, String request) {
		return this.existingRequests.contains(method.toUpperCase() + request.toLowerCase());
	}

	public TypeName getResponse() {
		return getSingleResponse();
	}

	public TypeName getSingleResponse() {
		return this.isUsingDto() ? this.dto : this.entity;
	}

	public TypeName getListResponse() {
		return this.isUsingDto() ? this.dto : this.entity;
	}

	public boolean isUsingRelations() {
		return this.relations != null && !this.relations.isEmpty();
	}

	public boolean isUsingDto() {
		return this.dto != null && !this.dto.equals(TypeName.OBJECT);
	}

}
