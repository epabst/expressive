package geeks.expressive;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * A specifier that can identify matching methods.
 *
 * @author pabstec
 */
public interface MethodSpecifier {
  /**
   * Gets the methods found within the Scope that are associated.
   * @param scope the Scope
   * @return the methods
   */
  Set<Method> getMethods(Scope scope);
}
