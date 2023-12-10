package eu.nerdfactor.springutil.generatedrest.export;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.GeneratedRestBuilder;
import eu.nerdfactor.springutil.generatedrest.GeneratedRestUtil;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.Map;

public class JavaClassExporter implements GeneratedRestExporter {

	private Filer filer;

	public JavaClassExporter withFiler(Filer filer) {
		this.filer = filer;
		return this;
	}


	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		final GeneratedRestBuilder builder = new GeneratedRestBuilder();
		controllers.values().forEach(controllerConfiguration -> {
			try {
				GeneratedRestUtil.log("Generating " + controllerConfiguration.getClassName().canonicalName() + " for " + controllerConfiguration.getEntity().toString() + ".");
				TypeSpec controllerSpec = builder.buildController(controllerConfiguration);
				JavaFile.builder(controllerConfiguration.getClassName().packageName(), controllerSpec).indent(config.getOrDefault("indentation", "\t")).build().writeTo(filer);
			} catch (IOException e) {
				GeneratedRestUtil.log("Could not generate " + controllerConfiguration.getClassName().canonicalName() + ".");
				e.printStackTrace();
			}
		});
	}
}
