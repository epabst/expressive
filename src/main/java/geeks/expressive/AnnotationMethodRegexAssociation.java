package geeks.expressive;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * A method filter for methods that have a given annotation.
 *
 * @author pabstec
 */
class AnnotationMethodRegexAssociation implements MethodRegexAssociation {
  private final Class<? extends Annotation> annotationClass;
  private final Method annotationValueMethod;

  public <T extends Annotation> AnnotationMethodRegexAssociation(Class<T> annotationClass) {
    this.annotationClass = annotationClass;
    annotationValueMethod = getValueMethod(annotationClass);
  }

  public Class<? extends Annotation> getAnnotationClass() {
    return annotationClass;
  }

  public String toString() {
    return "@" + annotationClass.getSimpleName();
  }

  public Set<Method> getMethods(Reflections reflections) {
    return reflections.getMethodsAnnotatedWith(annotationClass);
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
