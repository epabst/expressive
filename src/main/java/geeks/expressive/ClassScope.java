package geeks.expressive;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.HashSet;

/**
 * The Scope for a Class.
 *
 * @author pabstec
 */
class ClassScope implements Scope {
  private final Class<?> classForScope;

  ClassScope(Class<?> classForScope) {
    this.classForScope = classForScope;
  }

  public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
    Set<Method> result = new HashSet<Method>();
    for (Method method : classForScope.getMethods()) {
      if (method.getAnnotation(annotationClass) != null) {
        result.add(method);
      }
    }
    for (Method method : classForScope.getDeclaredMethods()) {
      if (method.getAnnotation(annotationClass) != null) {
        result.add(method);
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return classForScope.getName();
  }
}
