package eu.nerdfactor.springutil.generatedrest.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.springutil.generatedrest.config.ControllerConfiguration;
import eu.nerdfactor.springutil.generatedrest.entity.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class AddEntityMethodBuilderTest {

	@Mock
	ControllerConfiguration controllerConfiguration = new ControllerConfiguration();

	@Test
	void shouldCreateBasicMethod() {
		Mockito.when(controllerConfiguration.hasExistingRequest(any(RequestMethod.class), anyString()))
				.thenReturn(false);
		Mockito.when(controllerConfiguration.getEntity())
				.thenReturn(ClassName.get(Example.class));
		Mockito.when(controllerConfiguration.getRequest())
				.thenReturn("/api/example");
		Mockito.when(controllerConfiguration.isUsingDto())
				.thenReturn(false);
		Mockito.when(controllerConfiguration.getRequestType())
				.thenReturn(ClassName.get(Example.class));
		Mockito.when(controllerConfiguration.getSingleResponseType())
				.thenReturn(ClassName.get(Example.class));
		Mockito.when(controllerConfiguration.getSecurity())
				.thenReturn(null);
		Mockito.when(controllerConfiguration.getDataWrapperClass())
				.thenReturn(TypeName.OBJECT);

		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);
		new AddEntityMethodBuilder().withConfiguration(this.controllerConfiguration).build(builder);

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
