package eu.nerdfactor.springutil.generatedrest.export;

import com.squareup.javapoet.JavaFile;
import eu.nerdfactor.springutil.generatedrest.code.GeneratedControllerBuilder;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.util.GeneratedRestUtil;

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
		controllers.values().forEach(controllerConfiguration -> {
			try {
				GeneratedRestUtil.log("Generating " + controllerConfiguration.getClassName().canonicalName() + " for " + controllerConfiguration.getEntity().toString() + ".");
				JavaFile.builder(
								controllerConfiguration.getClassName().packageName(),
								new GeneratedControllerBuilder().withConfiguration(controllerConfiguration).build()
						).indent(config.getOrDefault("indentation", "\t"))
						.build()
						.writeTo(filer);
			} catch (IOException e) {
				GeneratedRestUtil.log("Could not generate " + controllerConfiguration.getClassName().canonicalName() + ".");
				e.printStackTrace();
			}
		});
	}
}
