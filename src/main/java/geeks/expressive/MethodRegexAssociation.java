package geeks.expressive;

import java.lang.reflect.Method;

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
}
