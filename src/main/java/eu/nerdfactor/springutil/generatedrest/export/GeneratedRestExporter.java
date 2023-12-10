package eu.nerdfactor.springutil.generatedrest.export;

import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.util.Map;

public interface GeneratedRestExporter {

	public GeneratedRestExporter withFiler(Filer filer);

	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers);
}
