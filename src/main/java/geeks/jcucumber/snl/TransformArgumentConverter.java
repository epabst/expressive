package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Method;

/**
 * An argument converter using methods matching a {@link MethodRegexIdentifier}.
 *
 * @author pabstec
 */
class TransformArgumentConverter implements ArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TransformArgumentConverter.class.getName());
  private final StructuredNaturalLanguageExecuter executer;
  private final MethodRegexIdentifier methodIdentifier;

  public TransformArgumentConverter(final Class<?> targetType, StructuredNaturalLanguageExecuter executer, final MethodRegexIdentifier transformMethodIdentifier) {
    this.executer = executer;
    this.methodIdentifier = new MethodRegexIdentifier() {
      public String getRegex(Method method) {
        if (targetType.isAssignableFrom(method.getReturnType())) {
          return transformMethodIdentifier.getRegex(method);
        } else {
          //if the type doesn't match then don't use the Transform
          return null;
        }
      }
    };
  }

  public Object convertArgument(String argString, final NaturalLanguageMethod naturalLanguageMethod, int index) {
    Class<?> matchingClass = getClassWithTransforms(naturalLanguageMethod);
    MethodRegexIdentifier methodIdentifierWithoutCircularity = new MethodRegexIdentifier() {
      public String getRegex(Method method) {
        if (!method.equals(naturalLanguageMethod.getMethod())) {
          return methodIdentifier.getRegex(method);
        } else {
          return null;
        }
      }
    };
    StructuredNaturalLanguageExecuter.NaturalLanguageMethodMatch match = executer.findMatchingNaturalLanguageMethod(
            argString, methodIdentifier, methodIdentifierWithoutCircularity, matchingClass);
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
