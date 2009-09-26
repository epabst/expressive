package geeks.expressive;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * A MethodSpecifier with an Annotation.
 *
 * @author pabstec
 */
public class AnnotationMethodSpecifier implements MethodSpecifier {
  private final Class<? extends Annotation> annotationClass;

  public <T extends Annotation> AnnotationMethodSpecifier(Class<T> annotationClass) {
    this.annotationClass = annotationClass;
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
}
