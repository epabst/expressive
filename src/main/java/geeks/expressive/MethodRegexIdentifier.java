package geeks.expressive;

import java.lang.reflect.Method;

/**
 * An object that gets the regular expression for a Method.
 *
 * @author pabstec
 */
public interface MethodRegexIdentifier {
  String toString();

  String getRegex(Method method);
}
