package geeks.expressive;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * A method filter for methods that have a given annotation.
 *
 * @author pabstec
 */
class AnnotationMethodRegexAssociation<T extends Annotation> implements MethodRegexAssociation {
  private final Class<T> annotationClass;
  private final Method annotationValueMethod;

  public static <T extends Annotation> AnnotationMethodRegexAssociation<T> getInstance(Class<T> annotationClass) {
    return new AnnotationMethodRegexAssociation<T>(annotationClass);
  }

  private AnnotationMethodRegexAssociation(Class<T> annotationClass) {
    this.annotationClass = annotationClass;
    annotationValueMethod = getValueMethod(annotationClass);
  }

  public Class<T> getAnnotationClass() {
    return annotationClass;
  }

  public String toString() {
    return "@" + annotationClass.getSimpleName();
  }

  public String findRegex(Method method) {
    Annotation annotation = method.getAnnotation(annotationClass);
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
