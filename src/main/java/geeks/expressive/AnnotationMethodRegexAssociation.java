package geeks.expressive;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * A method filter for methods that have a given annotation.
 *
 * @author pabstec
 */
public class AnnotationMethodRegexAssociation extends AnnotationMethodSpecifier implements MethodRegexAssociation {
  private final Method annotationValueMethod;

  public <T extends Annotation> AnnotationMethodRegexAssociation(Class<T> annotationClass) {
    super(annotationClass);
    annotationValueMethod = getValueMethod(annotationClass);
  }

  public String findRegex(Method method) {
    Annotation annotation = method.getAnnotation(getAnnotationClass());
    String regex = null;
    if (annotation != null) {
      regex = (String) ReflectionUtil.invokeWithArgs(annotationValueMethod, annotation);
    }
    return regex;
  }

  private Method getValueMethod(Class<? extends Annotation> annotationClass) {
    Method valueMethod;
    try {
      valueMethod = annotationClass.getMethod("value");
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
    return valueMethod;
  }
}
