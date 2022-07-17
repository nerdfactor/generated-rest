package eu.nerdfactor.springutil.generatedrest.config;

import com.squareup.javapoet.ClassName;
import eu.nerdfactor.springutil.generatedrest.GeneratedRestUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Security configuration for controller generation.
 *
 * @author Daniel Klug
 */
public class SecurityConfiguration {

	ClassName className;

	/**
	 * Pattern for roles that will be checked on rest methods.
	 * <li>METHOD: Type of method - CREATE, READ, UPDATE, DELETE</li>
	 * <li>ENTITY: Name of the entity.</li>
	 * <li>Name: Name of relation or entity.</li>
	 */
	String pattern = "ROLE_{METHOD}_{ENTITY}";

	/**
	 * The security checks can include the base check on relations. The user
	 * has to have READ permissions for the base element to access any relations
	 * of it and UPDATE permissions to change a relation.
	 */
	boolean inclusive = true;

	public String getRole(String method, String entity, String name) {
		return this.pattern
				.replace("{METHOD}", method)
				.replace("{ENTITY}", GeneratedRestUtil.normalizeEntityName(entity))
				.replace("{NAME}", name)
				.toUpperCase();
	}

	public String getSecurityString(ControllerConfiguration config, String method){
		ClassName entityName = GeneratedRestUtil.toClassName(config.getEntity());
		String base = this.getRole(method, entityName.simpleName(), entityName.simpleName());
		return "hasRole('" + base + "')";
	}

	public String getSecurityString(ControllerConfiguration config, RelationConfiguration relation, String method, String methodBase){
		ClassName entityName = relation.getEntityClass();
		String role = this.getRole(method, relation.getEntityClass().simpleName(),  relation.getEntityClass().simpleName());
		String security = "hasRole('" + role + "')";
		if(config.getSecurity().inclusive){
			ClassName baseEntityName = GeneratedRestUtil.toClassName(config.getEntity());
			String base = this.getRole(methodBase, baseEntityName.simpleName(), baseEntityName.simpleName());
			security += " and hasRole('" + base + "')";
		}
		return security;
	}

	public SecurityConfiguration() {
	}

	public SecurityConfiguration(ClassName className, String pattern, boolean inclusive) {
		this.className = className;
		this.pattern = pattern;
		this.inclusive = inclusive;
	}

	public static @NotNull SecurityConfigurationBuilder builder() {
		return new SecurityConfigurationBuilder();
	}

	public ClassName getClassName() {
		return className;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isInclusive() {
		return inclusive;
	}
}
