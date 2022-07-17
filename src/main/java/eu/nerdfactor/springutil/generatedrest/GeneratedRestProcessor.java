package eu.nerdfactor.springutil.generatedrest;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestConfiguration;
import eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestController;
import eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestSecurity;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.config.SecurityConfiguration;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor for generated rest controllers.
 * Will check all GeneratedRestController and GeneratedRestConfiguration annotations and
 * build new controller classes out of the information.
 * <p>
 * <a href="https://stackoverflow.com/a/31358366">How to debug</a>
 * In directory of pom: mvnDebug clean test
 *
 * @author Daniel Klug
 */
@SupportedAnnotationTypes({
		"eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestController",
		"eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestConfiguration"
})
@AutoService(Processor.class)
public class GeneratedRestProcessor extends AbstractProcessor {

	private Filer filer;
	private Elements elementUtils;
	private Messager messanger;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		this.filer = processingEnvironment.getFiler();
		this.elementUtils = processingEnvironment.getElementUtils();
		this.messanger = processingEnvironment.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		final Map<String, ControllerConfiguration> controllers = new HashMap<>();

		// Get all values from DynamicRestConfiguration annotations into one map.
		final Map<String, String> generatedConfig = new HashMap<>();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestConfiguration.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				return true;
			}

			GeneratedRestUtil.addAnnotatedValues(
					element,
					GeneratedRestConfiguration.class.getCanonicalName(),
					this.elementUtils,
					generatedConfig
			);

			GeneratedRestUtil.log("GeneratedConfig");
			generatedConfig.forEach((name, value) -> {
				GeneratedRestUtil.log(name + ": " + value);
			});
		}


		// Get all DynamicRestController annotations and gather information from the specified
		// entity in order to create a ControllerConfiguration.
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestController.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				return true;
			}
			ControllerConfiguration config = ControllerConfiguration.builder()
					.fromElement(element)
					.withUtils(this.elementUtils)
					.withEnvironment(roundEnvironment)
					.withPrefix(generatedConfig.getOrDefault("classNamePrefix", "Generated"))
					.withPattern(generatedConfig.getOrDefault("classNamePattern", "{PREFIX}{NAME}"))
					.withDataWrapper(ClassName.bestGuess(generatedConfig.getOrDefault("dataWrapper", Object.class.getCanonicalName())))
					.build();
			controllers.put(config.getClassName().simpleName(), config);
		}

		// Get all DynamicRestSecurity annotations and add them to the matching controllers.
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestSecurity.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				return true;
			}
			SecurityConfiguration security = SecurityConfiguration.builder()
					.fromElement(element)
					.withUtils(this.elementUtils)
					.withEnvironment(roundEnvironment)
					.withPrefix(generatedConfig.getOrDefault("classNamePrefix", "Generated"))
					.withPattern(generatedConfig.getOrDefault("classNamePattern", "{PREFIX}{NAME}"))
					.build();
			if (controllers.containsKey(security.getClassName().simpleName())) {
				controllers.get(security.getClassName().simpleName()).setSecurity(security);
			}
		}

		// Take the ControllerConfigurations and build new classes from them.
		final GeneratedRestBuilder builder = new GeneratedRestBuilder();
		controllers.values().forEach(controllerConfiguration -> {
			try {
				GeneratedRestUtil.log("Generating " + controllerConfiguration.getClassName().canonicalName() + " for " + controllerConfiguration.getEntity().toString() + ".");
				TypeSpec controllerSpec = builder.buildController(controllerConfiguration);
				JavaFile.builder(controllerConfiguration.getClassName().packageName(), controllerSpec).indent(generatedConfig.getOrDefault("indentation", "\t")).build().writeTo(filer);
			} catch (IOException e) {
				GeneratedRestUtil.log("Could not generate " + controllerConfiguration.getClassName().canonicalName() + ".");
				e.printStackTrace();
			}
		});

		return true;
	}
}
