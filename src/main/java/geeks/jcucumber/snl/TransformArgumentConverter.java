package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.annotation.Annotation;

/**
 * An argument converter using {@link Transform} methods. 
 *
 * @author pabstec
 */
class TransformArgumentConverter implements ArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TransformArgumentConverter.class.getName());
  private final StructuredNaturalLanguageExecuter executer;
  private final AnnotationMethodRegexIdentifier<? extends Annotation> transformIdentifier;

  public TransformArgumentConverter(StructuredNaturalLanguageExecuter executer) {
    this.executer = executer;
    this.transformIdentifier = AnnotationMethodRegexIdentifier.getInstance(Transform.class);
  }

  public Object convertArgument(String argString, NaturalLanguageMethod naturalLanguageMethod, int index) {
    Class<?> matchingClass = getClassWithTransforms(naturalLanguageMethod);
    StructuredNaturalLanguageExecuter.NaturalLanguageMethodMatch match = executer.findMatchingNaturalLanguageMethod(
            argString, transformIdentifier, matchingClass);
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
