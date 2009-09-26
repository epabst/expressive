package geeks.expressive;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * A specifier that can identify matching methods.
 *
 * @author pabstec
 */
public interface MethodSpecifier {
  /**
   * Gets the methods found within the Reflections that are associated.
   * @param reflections the Reflections
   * @return the methods
   */
  Set<Method> getMethods(Reflections reflections);
}
