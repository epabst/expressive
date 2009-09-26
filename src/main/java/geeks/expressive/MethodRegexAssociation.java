package geeks.expressive;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Collections;

/**
 * An object that locates the regular expression for a Method.
 *
 * @author pabstec
 */
public interface MethodRegexAssociation extends MethodSpecifier {
  MethodRegexAssociation NONE = new MethodRegexAssociation() {
    public String findRegex(Method method) {
      return null;
    }

    public Set<Method> getMethods(Reflections reflections) {
      return Collections.emptySet();
    }
  };

  /**
   * Finds the regular expression associated with the given method.
   * @param method the Method
   * @return the regular expression or null if there is none
   */
  String findRegex(Method method);

  /**
   * Gets a String representation of this object.
   * @return a String
   */
  String toString();

}
