package geeks.expressive;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * A scope that knows about which Classes should be used.
 *
 * @author pabstec
 */
public interface Scope {
  Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass);
}
