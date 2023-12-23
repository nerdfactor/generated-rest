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
public class CreateEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		CreateEntityMethodBuilder.create()
				.withHasExistingRequest(false)
				.withUsingDto(false)
				.withRequestUrl("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestType(ClassName.get(Example.class))
				.withResponseType(ClassName.get(Example.class))
				.withSecurityConfiguration(null)
				.withDataWrapperClass(TypeName.OBJECT)
				.build(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				package eu.nerdfactor.test;
				    
				import eu.nerdfactor.springutil.generatedrest.entity.Example;
				import jakarta.validation.Valid;
				import org.springframework.http.HttpStatus;
				import org.springframework.http.ResponseEntity;
				import org.springframework.web.bind.annotation.PostMapping;
				import org.springframework.web.bind.annotation.RequestBody;
				import org.springframework.web.bind.annotation.RestController;
				    
				@RestController
				public class ExampleController {
				  @PostMapping("/api/example")
				  public ResponseEntity<Example> create(@RequestBody @Valid Example dto) {
				    Example created = dto;
				    created = this.dataAccessor.createData(created);
				    Example response = created;
				    return new ResponseEntity<>(response, HttpStatus.OK);
				  }
				}
				""";
		Assertions.assertEquals(code, expected);
	}
}
