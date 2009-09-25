package geeks.expressive;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * An object that locates the regular expression for a Method.
 *
 * @author pabstec
 */
public interface MethodRegexAssociation {

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

  /**
   * Gets the methods found within the Reflections that are associated.
   * @param reflections the Reflections
   * @return the methods
   */
  Set<Method> getMethods(Reflections reflections);
}
