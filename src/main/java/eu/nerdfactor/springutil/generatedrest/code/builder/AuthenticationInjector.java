package eu.nerdfactor.springutil.generatedrest.code.builder;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.springutil.generatedrest.config.SecurityConfiguration;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;

public class AuthenticationInjector implements Injectable<MethodSpec.Builder> {

	protected String method = "READ";

	protected TypeName type;

	protected TypeName relation;

	protected SecurityConfiguration securityConfig;

	public AuthenticationInjector withMethod(@NotNull String method) {
		this.method = method.trim().toUpperCase();
		return this;
	}

	public AuthenticationInjector withType(TypeName type) {
		this.type = type;
		return this;
	}

	public AuthenticationInjector withRelation(TypeName relation) {
		this.relation = relation;
		return this;
	}

	public AuthenticationInjector withSecurityConfig(SecurityConfiguration config) {
		this.securityConfig = config;
		return this;
	}

	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		if (this.securityConfig == null) {
			return builder;
		}
		String security = "";
		if (this.relation != null) {
			security = this.securityConfig.getSecurityString(this.type, this.relation, this.method, this.method);
		} else {
			ClassName entityName = GeneratedRestUtil.toClassName(this.type);
			String role = this.securityConfig.getRole(this.method, entityName.simpleName(), entityName.simpleName());
			security = "hasRole('" + role + "')";
		}
		builder.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		return builder;
	}
}
