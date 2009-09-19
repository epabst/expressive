package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.lang.annotation.Annotation;
import java.util.regex.Matcher;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A parser for structured natural language that executes it based on mapping it to methods.
 * This is based on <a href="http://cukes.info">Cucumber</a>.
 *
 * @author pabstec
 */
public class StructuredNaturalLanguageExecuter {
  static final Logger LOGGER = Logger.getLogger(StructuredNaturalLanguageExecuter.class.getName());
  private final MutablePicoContainer container;
  private final NaturalLanguageFactory naturalLanguageFactory;
  private static final Level DEBUG_LEVEL = Level.INFO;

  public StructuredNaturalLanguageExecuter(MutablePicoContainer container) {
    this.container = container;
    naturalLanguageFactory = new NaturalLanguageFactory(this);
  }

  public Object execute(String languageString, Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    List<NaturalLanguageMethod> naturalLanguageMethods = naturalLanguageFactory.getNaturalLanguageMethods(annotationClass, classWithAnnotations);
    for (NaturalLanguageMethod naturalLanguageMethod : naturalLanguageMethods) {
      NaturalLanguageMethodMatch match = match(naturalLanguageMethod, languageString);
      if (match != null) {
        Object objectWithAnnotationsFromToken = addAndGetComponent(classWithAnnotations);
        return match.invokeMethod(objectWithAnnotationsFromToken);
      }
    }
    throw new IllegalStateException("No matching @" + annotationClass.getSimpleName()
            + " method found for '" + languageString + "' in " + classWithAnnotations);
  }

  private NaturalLanguageMethodMatch match(NaturalLanguageMethod naturalLanguageMethod, String inputString) {
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Checking " + naturalLanguageMethod.getPattern() + " for match with '" + inputString + "'");
    }
    Matcher matcher = naturalLanguageMethod.getPattern().matcher(inputString);
    if (matcher.matches()) {
      return new NaturalLanguageMethodMatch(naturalLanguageMethod, matcher);
    }
    return null;
  }

  <T> T addAndGetComponent(Class<T> componentClass) {
    if (container.getComponent(componentClass) == null) {
      container.addComponent(componentClass);
    }
    return container.getComponent(componentClass);
  }
}
