package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.entity.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

@ExtendWith(MockitoExtension.class)
public class ReadEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		ReadEntityMethodBuilder.create()
				.withHasExistingRequest(false)
				.withUsingDto(false)
				.withRequestUrl("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withIdentifyingType(ClassName.get(Integer.class))
				.withResponseType(ClassName.get(Example.class))
				.withSecurityConfiguration(null)
				.withDataWrapperClass(TypeName.OBJECT)
				.build(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				package eu.nerdfactor.test;
				    
				import eu.nerdfactor.springutil.generatedrest.entity.Example;
				import jakarta.persistence.EntityNotFoundException;
				import java.lang.Integer;
				import org.springframework.http.HttpStatus;
				import org.springframework.http.ResponseEntity;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				import org.springframework.web.bind.annotation.RestController;
				    
				@RestController
				public class ExampleController {
				  @GetMapping("/api/example")
				  public ResponseEntity<Example> get(@PathVariable final Integer id) {
				    Example entity = this.dataAccessor.readData(id);
				    if(entity == null) {
				      throw new EntityNotFoundException();
				    }
				    Example response = entity;
				    return new ResponseEntity<>(response, HttpStatus.OK);
				  }
				}
				""";
		Assertions.assertEquals(code, expected);
	}
}
