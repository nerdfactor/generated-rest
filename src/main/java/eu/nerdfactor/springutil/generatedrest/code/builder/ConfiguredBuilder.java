package eu.nerdfactor.springutil.generatedrest.code.builder;

import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

public interface ConfiguredBuilder {

	ConfiguredBuilder withConfiguration(@NotNull ControllerConfiguration configuration);
}
