package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Method;
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
  private final Map<Class<?>, Object> addedComponents = new HashMap<Class<?>, Object>();
  private final NaturalLanguageFactory naturalLanguageFactory;
  private static final Level DEBUG_LEVEL = Level.INFO;

  public StructuredNaturalLanguageExecuter(MutablePicoContainer container) {
    this.container = container;
    naturalLanguageFactory = new NaturalLanguageFactory(this);
  }

  public Object execute(String languageString, MethodRegexIdentifier regexIdentifier, MethodRegexIdentifier transformMethodIdentifier, Class<?> matchingClass) {
    NaturalLanguageMethodMatch match = findMatchingNaturalLanguageMethod(languageString, regexIdentifier, transformMethodIdentifier, matchingClass);
    if (match != null) {
      return invokeMethod(match, matchingClass);
    }
    throw new IllegalStateException("No matching " + regexIdentifier
            + " method found for '" + languageString + "' in " + matchingClass);
  }

  Object invokeMethod(NaturalLanguageMethodMatch match, Class<?> matchingClass) {
    Object objectToInvoke = addAndGetComponent(matchingClass);
    return match.invokeMethod(objectToInvoke);
  }

  NaturalLanguageMethodMatch findMatchingNaturalLanguageMethod(String languageString, MethodRegexIdentifier regexIdentifier, MethodRegexIdentifier transformMethodIdentifier, Class<?> matchingClass) {
    List<NaturalLanguageMethod> naturalLanguageMethods = naturalLanguageFactory.getNaturalLanguageMethods(regexIdentifier, transformMethodIdentifier, matchingClass);
    NaturalLanguageMethodMatch match = null;
    for (NaturalLanguageMethod naturalLanguageMethod : naturalLanguageMethods) {
      match = match(naturalLanguageMethod, languageString);
      if (match != null) {
        break;
      }
    }
    return match;
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
    if (false) {
      //todo why doesn't this work?
      if (container.getComponent(componentClass) == null) {
        container.addComponent(componentClass);
      }
      return container.getComponent(componentClass);
    }
    if (!addedComponents.containsKey(componentClass)) {
      container.addComponent(componentClass);
      addedComponents.put(componentClass, container.getComponent(componentClass));
    }
    //noinspection unchecked
    return (T) addedComponents.get(componentClass);
  }

  /**
 * A success match of a NaturalLanguageMethod to a string.
  *
  * @author pabstec
  */
  static class NaturalLanguageMethodMatch {
    private final NaturalLanguageMethod naturalLanguageMethod;
    private final Matcher matcher;

    public NaturalLanguageMethodMatch(NaturalLanguageMethod naturalLanguageMethod, Matcher matcher) {
      this.naturalLanguageMethod = naturalLanguageMethod;
      this.matcher = matcher;
    }

    public NaturalLanguageMethod getNaturalLanguageMethod() {
      return naturalLanguageMethod;
    }

    public Matcher getMatcher() {
      return matcher;
    }

    Object invokeMethod(Object objectToInvoke) {
      Method method = getNaturalLanguageMethod().getMethod();
      List<ArgumentConverter> argumentConverters = naturalLanguageMethod.getArgumentConverters();
      Object[] args = new Object[matcher.groupCount()];
      for (int i = 0; i < matcher.groupCount(); i++) {
        String group = matcher.group(i + 1);
        args[i] = argumentConverters.get(i).convertArgument(group, naturalLanguageMethod, i);
      }
      return ReflectionUtil.invokeWithArgs(method, objectToInvoke, args);
    }
  }
}
