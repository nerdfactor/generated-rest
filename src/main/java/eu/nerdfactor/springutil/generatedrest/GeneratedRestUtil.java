package eu.nerdfactor.springutil.generatedrest;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * Utility methods for generated rest.
 *
 * @author Daniel Klug
 */
public class GeneratedRestUtil {

	/**
	 * Simple methods to normalize an entity name by removing common suffix
	 * like Model, Entity, BO or Dao.
	 *
	 * @param name The original entity name.
	 * @return The normalized entity name.
	 */
	public static String normalizeEntityName(@NotNull String name) {
		for (String suffix : Arrays.asList("Model", "Entity", "BO", "Dao")) {
			if (name.endsWith(suffix)) {
				name = name.substring(0, name.length() - suffix.length());
			}
		}
		return name;
	}

	/**
	 * Simple method to turn a name into its singular version.
	 * Will not work for all the special cases of plural forms.
	 *
	 * @param name The original name.
	 * @return The singular name.
	 */
	public static String singularName(@NotNull String name) {
		if (name.endsWith("ies")) {
			name = name.substring(0, name.length() - 3) + "y";
		}
		if (name.endsWith("s")) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}

	/**
	 * Removes a string from the end of a string.
	 *
	 * @param str The string.
	 * @param remove The part to remove from the end.
	 * @return The string without the removed part.
	 */
	public static String removeEnd(@NotNull String str, @NotNull String remove) {
		if (remove.length() > 0 && str.endsWith(remove)) {
			return str.substring(0, str.length() - remove.length());
		}
		return str;
	}

	/**
	 * Turn a TypeName into a ClassName.
	 *
	 * @param typeName The original TypeName.
	 * @return The converted ClassName.
	 */
	public static ClassName toClassName(TypeName typeName) {
		return toClassName(typeName.toString());
	}

	/**
	 * Turns a canonical name of a type into a ClassName.
	 *
	 * @param typeName The canonical name of a class.
	 * @return The converted ClassName.
	 */
	public static ClassName toClassName(String typeName) {
		return toClassName(typeName, "");
	}

	/**
	 * Turns a canonical name of a type into a ClassName and
	 * adds a new prefix to the class.
	 *
	 * @param typeName The canonical name of the class.
	 * @param prefix The new prefix for the class.
	 * @return The converted ClassName.
	 */
	public static ClassName toClassName(String typeName, String prefix) {
		String className = typeName.substring(typeName.lastIndexOf('.') + 1).trim();
		String packageName = removeEnd(typeName, "." + className);
		return ClassName.get(packageName, prefix + className);
	}

	/**
	 * Get all the values from a specific annotation of the element in a Map.
	 *
	 * @param element The element to search within.
	 * @param annotationClassName The class name of the annotation.
	 * @param elementUtils Utility object for elements.
	 * @return A map of values with name of value as keys.
	 * @see GeneratedRestUtil#addAnnotatedValues
	 */
	public static Map<String, String> getAnnotatedValues(final Element element, final String annotationClassName, Elements elementUtils) {
		Map<String, String> values = new HashMap<>();
		return addAnnotatedValues(element, annotationClassName, elementUtils, values);
	}

	/**
	 * Add all the values from a specific annotation of the element to a Map.
	 * https://stackoverflow.com/a/52257877
	 *
	 * @param element The element to search within.
	 * @param annotationClassName The class name of the annotation.
	 * @param elementUtils Utility object for elements.
	 * @param values Map that values will be added to.
	 * @return A map of values with name of value as keys.
	 */
	public static Map<String, String> addAnnotatedValues(final Element element, final String annotationClassName, Elements elementUtils, Map<String, String> values) {
		final Optional<? extends AnnotationMirror> retValue = element.getAnnotationMirrors().stream()
				.filter(m -> m.getAnnotationType().toString().equals(annotationClassName))
				.findFirst();
		if (retValue.isPresent()) {
			final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = elementUtils.getElementValuesWithDefaults(retValue.get());
			elementValues.forEach((executableElement, annotationValue) -> {
				try {
					// todo: Value might be a list; Object v = ((AnnotationValue) annotationValue).getValue(); -> addAnnotatedListValues
					values.put(executableElement.getSimpleName().toString(), annotationValue.getValue().toString());
				} catch (Exception e) {
				}
			});
		}
		return values;
	}

	/**
	 * Get all the values from a list of specific annotations of the element to a List of Maps.
	 *
	 * @param element The element to search within.
	 * @param annotationClassName The class name of the annotation.
	 * @param elementUtils Utility object for elements.
	 * @return A list of map of values with name of value as keys.
	 * @see GeneratedRestUtil#addAnnotatedValues
	 */
	public static List<Map<String, String>> getAnnotatedListValues(final Element element, final String annotationClassName, Elements elementUtils) {
		List<Map<String, String>> values = new ArrayList<>();
		return addAnnotatedListValues(element, annotationClassName, elementUtils, values);
	}

	/**
	 * Add all the values from a list of specific annotations of the element to a List of Maps.
	 * https://stackoverflow.com/a/52257877
	 * todo: Combine with addAnnotatedValues?
	 *
	 * @param element The element to search within.
	 * @param annotationClassName The class name of the annotation.
	 * @param elementUtils Utility object for elements.
	 * @param values List of Maps that values will be added to.
	 * @return A list of map of values with name of value as keys.
	 */
	public static List<Map<String, String>> addAnnotatedListValues(final Element element, final String annotationClassName, Elements elementUtils, List<Map<String, String>> values) {
		final Optional<? extends AnnotationMirror> retValue = element.getAnnotationMirrors().stream()
				.filter(m -> m.getAnnotationType().toString().equals(annotationClassName))
				.findFirst();
		if (retValue.isPresent()) {
			elementUtils.getElementValuesWithDefaults(retValue.get()).forEach((executableElement, annotationValue) -> {
				try {
					((Iterable<?>) annotationValue.getValue()).forEach(o -> {
						Map<String, String> item = new HashMap<>();
						elementUtils.getElementValuesWithDefaults((AnnotationMirror) o).forEach((executableElement1, annotationValue1) -> {
							item.put(executableElement1.getSimpleName().toString(), annotationValue1.getValue().toString());
						});
						values.add(item);
					});
				} catch (Exception e) {
				}
			});
		}
		return values;
	}

	public static boolean LOG = false;

	/**
	 * Simplistic log helper.
	 *
	 * @param str The log string.
	 */
	public static void log(String str) {
		log(str, 0);
	}
	public static void log(String str, int indentation) {
		if (LOG) {
			System.out.println("[INFO] " + "  ".repeat(indentation) + str);
		}
	}
}
