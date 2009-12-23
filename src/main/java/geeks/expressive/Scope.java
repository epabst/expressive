package geeks.expressive;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.Collections;

/**
 * A scope that knows about which Classes should be used.
 *
 * @author pabstec
 */
public interface Scope {
  Scope EMPTY = new Scope() {
    public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
      return Collections.emptySet();
    }
  };

  /**
   * Finds all the methods with the given Annotation class.
   * @param annotationClass the Annotation class
   * @return the set of Methods
   */
  Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass);
}
