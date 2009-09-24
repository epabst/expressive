package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Method;

/**
 * An argument converter using {@link Transform} methods. 
 *
 * @author pabstec
 */
class TransformArgumentConverter implements ArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TransformArgumentConverter.class.getName());
  private final StructuredNaturalLanguageExecuter executer;
  private final MethodRegexIdentifier methodIdentifier;

  public TransformArgumentConverter(final Class<?> targetType, StructuredNaturalLanguageExecuter executer) {
    this.executer = executer;
    final AnnotationMethodRegexIdentifier<Transform> annotationMethodRegexIdentifier = AnnotationMethodRegexIdentifier.getInstance(Transform.class);
    this.methodIdentifier = new MethodRegexIdentifier() {
      public String getRegex(Method method) {
        if (targetType.isAssignableFrom(method.getReturnType())) {
          return annotationMethodRegexIdentifier.getRegex(method);
        } else {
          //if the type doesn't match then don't use the Transform
          return null;
        }
      }
    };
  }

  public Object convertArgument(String argString, NaturalLanguageMethod naturalLanguageMethod, int index) {
    Class<?> matchingClass = getClassWithTransforms(naturalLanguageMethod);
    StructuredNaturalLanguageExecuter.NaturalLanguageMethodMatch match = executer.findMatchingNaturalLanguageMethod(
            argString, methodIdentifier, matchingClass);
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
