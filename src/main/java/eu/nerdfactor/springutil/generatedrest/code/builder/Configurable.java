package eu.nerdfactor.springutil.generatedrest.code.builder;

import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

public interface Configurable<T> {

	Configurable<T> withConfiguration(@NotNull T configuration);
}
