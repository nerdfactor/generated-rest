package eu.nerdfactor.springutil.generatedrest.config;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.springutil.generatedrest.GeneratedRestUtil;
import eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestController;
import eu.nerdfactor.springutil.generatedrest.annotation.IdAccessor;
import eu.nerdfactor.springutil.generatedrest.data.DataAccessor;
import eu.nerdfactor.springutil.generatedrest.data.DataMapper;
import eu.nerdfactor.springutil.generatedrest.data.DataMerger;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;

import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Builder that creates a new controller configuration from the
 * provided information.
 *
 * @author Daniel Klug
 */
public class ControllerConfigurationBuilder {

	RoundEnvironment environment;
	Elements elementUtils;
	TypeElement element;

	private String classNamePrefix;
	private String classNamePattern;

	private TypeName dataWrapper;

	private Map<String, List<TypeName>> dtoClasses;

	/**
	 * @param element The annotated Element.
	 * @return The ControllerConfigurationCollector.
	 */
	public ControllerConfigurationBuilder fromElement(Element element) {
		this.element = (TypeElement) element;
		return this;
	}

	public ControllerConfigurationBuilder withUtils(Elements utils) {
		this.elementUtils = utils;
		return this;
	}

	public ControllerConfigurationBuilder withEnvironment(RoundEnvironment env) {
		this.environment = env;
		return this;
	}

	public ControllerConfigurationBuilder withPrefix(@NotNull String prefix) {
		this.classNamePrefix = prefix;
		return this;
	}

	public ControllerConfigurationBuilder withPattern(@NotNull String pattern) {
		this.classNamePattern = pattern;
		return this;
	}

	public ControllerConfigurationBuilder withDataWrapper(@NotNull TypeName dataWrapper) {
		this.dataWrapper = dataWrapper;
		return this;
	}

	public ControllerConfigurationBuilder withDtoClasses(@NotNull Map<String, List<TypeName>> dtoClasses) {
		this.dtoClasses = dtoClasses;
		return this;
	}

	/**
	 * Collect information about the controller from the annotated class.
	 *
	 * @return ControllerConfiguration with the found information.
	 */
	public ControllerConfiguration build() {
		// Create parts of the annotated class name.
		String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
		String className = element.getSimpleName().toString();

		// Find all the annotated values in the annotation
		Map<String, String> annotatedValues = GeneratedRestUtil.getAnnotatedValues(element, GeneratedRestController.class.getName(), this.elementUtils);
		// And create class names from the informationen.
		ClassName entityClass = ClassName.bestGuess(annotatedValues.get("entity"));
		ClassName dtoClass = ClassName.bestGuess(annotatedValues.get("dto"));
		if (dtoClass.equals(ClassName.OBJECT)) {
			dtoClass = entityClass;
		}
		boolean withDto = !entityClass.equals(dtoClass);
		ClassName idClass = ClassName.bestGuess(annotatedValues.get("id"));

		// Combine the generated class name and package.
		String generatedClassName = annotatedValues.getOrDefault("className", "");
		if (generatedClassName.length() <= 0) {
			generatedClassName = this.classNamePattern.replace("{PREFIX}", this.classNamePrefix).replace("{NAME}", className).replace("{NAME_NORMALIZED}", className.replace("Controller", ""));
		}
		if (!generatedClassName.contains(".")) {
			generatedClassName = packageName + "." + generatedClassName;
		}

		// Find elements for the specified entity.
		TypeElement entityElement = element;
		for (Element elem : this.environment.getRootElements()) {
			if (elem.getSimpleName().toString().equals(entityClass.simpleName())) {
				entityElement = (TypeElement) elem;
			}
		}

		ParameterizedTypeName dataAccessorClass = ParameterizedTypeName.get(ClassName.get(DataAccessor.class), entityClass, idClass);
		ClassName dataMergerClass = ClassName.get(DataMerger.class);
		ClassName dataMapperClass = ClassName.get(DataMapper.class);

		// Check how the id can be accessed in the entity.
		String idAccessor = "getId";
		for (ExecutableElement method : methodsIn(entityElement.getEnclosedElements())) {
			for (AnnotationMirror anno : method.getAnnotationMirrors()) {
				String annotationName = anno.getAnnotationType().toString();
				if (annotationName.equals(IdAccessor.class.getName())) {
					idAccessor = method.getSimpleName().toString();
				}
			}
		}

		// Check for existing requests in the annotated class.
		List<String> existingRequests = new ArrayList<>();
		for (ExecutableElement method : methodsIn(element.getEnclosedElements())) {
			for (AnnotationMirror anno : method.getAnnotationMirrors()) {
				Arrays.asList(RequestMapping.class, GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class).forEach(cls -> {
					if (cls.getCanonicalName().equals(anno.getAnnotationType().toString())) {
						Map<String, String> requestMappingAnnotatedValues = GeneratedRestUtil.getAnnotatedValues(method, cls.getCanonicalName(), this.elementUtils);
						String requestMapping = requestMappingAnnotatedValues.getOrDefault("value", "/").replaceAll("\"$", "").replaceAll("^\"", "");
						if (requestMapping.length() > 1) {
							String clsName = cls.getSimpleName();
							String methodName = clsName.substring(0, clsName.indexOf('M')).toUpperCase();
							List<String> methodNames = new ArrayList<>(Collections.singletonList(methodName));
							if (cls == RequestMapping.class) {
								String[] requestMethods = requestMappingAnnotatedValues.getOrDefault("method", "GET").replaceAll("\"$", "").replaceAll("^\"", "").split(",");
								Arrays.stream(requestMethods).forEach(s -> methodNames.add(s.substring(s.lastIndexOf(".") + 1)));
							}
							methodNames.forEach(m -> {
								if (!m.equals("REQUEST")) {
									existingRequests.add(m + requestMapping.toLowerCase());
								}
							});
						}
					}
				});
			}
		}

		// Get the path for the request mapping from the annotation.
		String requestMapping = annotatedValues.getOrDefault("value", "");

		// If the controller should contain relations, collect them from the entity.
		Map<String, RelationConfiguration> relations = new HashMap<>();
		if (annotatedValues.get("withRelations").equals("true")) {
			// Get all compiled classes in order to determine dto for entity.


			// Collect all the relations.
			relations = RelationConfiguration.builder().withElement(entityElement).withUtils(this.elementUtils).withClasses(this.dtoClasses).withDtos(withDto).build();
		}

		return new ControllerConfiguration(GeneratedRestUtil.toClassName(generatedClassName), requestMapping, entityClass, idClass, idAccessor, withDto, withDto ? dtoClass : null, dataAccessorClass, dataMapperClass, dataMergerClass, relations, existingRequests, this.dataWrapper);
	}
}