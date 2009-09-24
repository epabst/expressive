package geeks.expressive;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Method;

/**
 * An argument converter using methods matching a {@link MethodRegexAssociation}.
 *
 * @author pabstec
 */
class TransformArgumentConverter implements ArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TransformArgumentConverter.class.getName());
  private final Expressive executer;
  private final MethodRegexAssociation transformRegexAssociation;

  public TransformArgumentConverter(final Class<?> targetType, Expressive executer, final MethodRegexAssociation transformRegexAssociation) {
    this.executer = executer;
    this.transformRegexAssociation = new MethodRegexAssociation() {
      public String findRegex(Method method) {
        if (targetType.isAssignableFrom(method.getReturnType())) {
          return transformRegexAssociation.findRegex(method);
        } else {
          //if the type doesn't match then don't use the Transform
          return null;
        }
      }
    };
  }

  public Object convertArgument(String argString, final NaturalLanguageMethod naturalLanguageMethod, int index) {
    Class<?> matchingClass = getClassWithTransforms(naturalLanguageMethod);
    MethodRegexAssociation noncircularRegexAssociation = new MethodRegexAssociation() {
      public String findRegex(Method method) {
        if (!method.equals(naturalLanguageMethod.getMethod())) {
          return transformRegexAssociation.findRegex(method);
        } else {
          return null;
        }
      }
    };
    Expressive.NaturalLanguageMethodMatch match = executer.findMatchingNaturalLanguageMethod(
            argString, transformRegexAssociation, noncircularRegexAssociation, matchingClass);
    if (match != null && !match.getNaturalLanguageMethod().equals(naturalLanguageMethod)) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(Level.FINE, "Converting " + argString + " using " + match.getNaturalLanguageMethod().getMethod());
      }
      return executer.invokeMethod(match, matchingClass);
    } else {
      return argString;
    }
  }

  private Class<?> getClassWithTransforms(NaturalLanguageMethod naturalLanguageMethod) {
    return naturalLanguageMethod.getMethod().getDeclaringClass();
  }
}
