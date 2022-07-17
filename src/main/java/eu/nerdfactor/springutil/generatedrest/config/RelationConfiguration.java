package eu.nerdfactor.springutil.generatedrest.config;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.springutil.generatedrest.GeneratedRestUtil;

/**
 * Configuration for relation generation.
 *
 * @author Daniel Klug
 */
public class RelationConfiguration {

	private String name;

	private RelationType type;

	private String getter;

	private String setter;

	private String adder;

	private String remover;

	private ClassName entityClass;

	private TypeName dtoClass;

	private boolean withDtos;

	private TypeName idClass;

	private String idAccessor;

	public RelationConfiguration() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RelationType getType() {
		return type;
	}

	public void setType(RelationType type) {
		this.type = type;
	}

	public String getGetter() {
		return getter;
	}

	public void setGetter(String getter) {
		this.getter = getter;
	}

	public String getSetter() {
		return setter;
	}

	public void setSetter(String setter) {
		this.setter = setter;
	}

	public String getAdder() {
		return adder;
	}

	public void setAdder(String adder) {
		this.adder = adder;
	}

	public String getRemover() {
		return remover;
	}

	public void setRemover(String remover) {
		this.remover = remover;
	}

	public void setAccessors(String[] accessors) {
		this.setGetter(accessors[0]);
		this.setSetter(accessors[1]);
		this.setAdder(accessors[2]);
		this.setRemover(accessors[3]);
	}

	public ClassName getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(ClassName entityClass) {
		this.entityClass = entityClass;
	}

	public TypeName getDtoClass() {
		return dtoClass;
	}

	public void setDtoClass(TypeName dtoClass) {
		this.dtoClass = dtoClass;
		this.withDtos = dtoClass != null;
	}

	public boolean isWithDtos() {
		return this.withDtos;
	}

	public TypeName getResponse() {
		return this.withDtos && this.dtoClass != null && !this.dtoClass.equals(TypeName.OBJECT) ? this.dtoClass : this.entityClass;
	}

	public TypeName getIdClass() {
		return idClass;
	}

	public void setIdClass(TypeName idClass) {
		this.idClass = idClass;
	}

	public String getIdAccessor() {
		return idAccessor;
	}

	public void setIdAccessor(String idAccessor) {
		this.idAccessor = idAccessor;
	}

	public String getMethodName(AccessorType type) {
		String methodName = this.name.substring(0, 1).toUpperCase() + this.name.substring(1);
		String singularName = GeneratedRestUtil.singularName(methodName);
		switch (type) {
			case GET:
				return "get" + methodName;
			case SET:
				return "set" + methodName;
			case ADD:
				return "add" + singularName;
			case REMOVE:
				return "remove" + singularName;
		}
		return methodName;
	}

	public static RelationConfigurationBuilder builder() {
		return new RelationConfigurationBuilder();
	}

}
