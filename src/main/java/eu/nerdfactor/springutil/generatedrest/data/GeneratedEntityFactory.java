package eu.nerdfactor.springutil.generatedrest.data;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factory containing beans for generated entity mapper and merger.
 *
 * @author Daniel Klug
 */
@Configuration
public class GeneratedEntityFactory {

	@Bean
	@ConditionalOnMissingBean(DataMapper.class)
	public DataMapper getGeneratedEntityMapper() {
		return new GeneratedEntityMapper();
	}

	@Bean
	@ConditionalOnMissingBean(DataMerger.class)
	public DataMerger getGeneratedEntityMerger() {
		return new GeneratedEntityMerger();
	}
}
