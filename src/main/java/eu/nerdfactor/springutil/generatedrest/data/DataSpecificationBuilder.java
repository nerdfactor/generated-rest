package eu.nerdfactor.springutil.generatedrest.data;

import org.springframework.data.jpa.domain.Specification;

/**
 * Generic way to build a Specification from a filter string.
 *
 * @author Daniel Klug
 */
public interface DataSpecificationBuilder {

	<T> Specification<T> build(String filter, Class<T> cls);
}
