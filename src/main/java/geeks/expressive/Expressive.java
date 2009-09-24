package geeks.expressive;

import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A parser for structured natural language that executes it based on mapping it to methods.
 * This is based on <a href="http://cukes.info">Cucumber</a>.
 *
 * @author pabstec
 */
public class Expressive {
  static final Logger LOGGER = Logger.getLogger(Expressive.class.getName());
  private final MutablePicoContainer container;
  private final Map<Class<?>, Object> addedComponents = new HashMap<Class<?>, Object>();
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();
  private static final Level DEBUG_LEVEL = Level.INFO;

  public Expressive(MutablePicoContainer container) {
    this.container = container;
  }

  public Object execute(String languageString, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Class<?> matchingClass) {
    NaturalLanguageMethodMatch match = findMatchingNaturalLanguageMethod(languageString, regexAssociation, transformRegexAssociation, matchingClass);
    if (match != null) {
      return invokeMethod(match, matchingClass);
    }
    throw new IllegalStateException("No method with " + regexAssociation
            + " found for '" + languageString + "' in " + matchingClass);
  }

  public List<NaturalLanguageMethod> getNaturalLanguageMethods(MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Class<?> matchingClass) {
    List<Object> key = Arrays.asList(regexAssociation, transformRegexAssociation, matchingClass);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(regexAssociation, transformRegexAssociation, matchingClass);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Class<?> matchingClass) {
    Method[] methods = matchingClass.getDeclaredMethods();
    List<NaturalLanguageMethod> naturalLanguageMethods = new ArrayList<NaturalLanguageMethod>(methods.length);
    for (Method method : methods) {
      NaturalLanguageMethod naturalLanguageMethod = calculateNaturalLanguageMethod(method, regexAssociation, transformRegexAssociation);
      if (naturalLanguageMethod != null) {
        naturalLanguageMethods.add(naturalLanguageMethod);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found methods with " + regexAssociation + " in "
              + matchingClass + ": " + naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  /**
   * Tries to convert a method into a NaturalLanguageMethod.
   * @param method the Method
   * @param regexAssociation a matcher for candidate methods
   * @param transformRegexAssociation a matcher for methods that can transform values
   * @return a NaturalLanguageMethod
   */
  private NaturalLanguageMethod calculateNaturalLanguageMethod(Method method, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.FINEST, "Seeing if method " + method + " has " + regexAssociation);
    }
    String regex = regexAssociation.findRegex(method);
    if (regex == null) {
      return null;
    }
    return toNaturalLanguageMethod(method, regex, transformRegexAssociation);
  }

  private NaturalLanguageMethod toNaturalLanguageMethod(Method method, String regexWithTokens, MethodRegexAssociation transformRegexAssociation) {
    Pattern pattern = Pattern.compile(regexWithTokens);
    return new NaturalLanguageMethod(pattern, method, createArgumentConverters(method, transformRegexAssociation));
  }

  private List<ArgumentConverter> createArgumentConverters(Method method, MethodRegexAssociation transformRegexAssociation) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    List<ArgumentConverter> converters = new ArrayList<ArgumentConverter>(parameterTypes.length);
    for (Class<?> parameterType : parameterTypes) {
      converters.add(new TransformArgumentConverter(parameterType, this, transformRegexAssociation));
    }
    return converters;
  }

  Object invokeMethod(NaturalLanguageMethodMatch match, Class<?> matchingClass) {
    Object objectToInvoke = addAndGetComponent(matchingClass);
    return match.invokeMethod(objectToInvoke);
  }

  NaturalLanguageMethodMatch findMatchingNaturalLanguageMethod(String languageString, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Class<?> matchingClass) {
    List<NaturalLanguageMethod> naturalLanguageMethods = getNaturalLanguageMethods(regexAssociation, transformRegexAssociation, matchingClass);
    NaturalLanguageMethodMatch match = null;
    for (NaturalLanguageMethod naturalLanguageMethod : naturalLanguageMethods) {
      match = match(naturalLanguageMethod, languageString);
      if (match != null) {
        if (LOGGER.isLoggable(DEBUG_LEVEL)) {
          LOGGER.log(DEBUG_LEVEL, "Found match for '" + languageString + "': " + naturalLanguageMethod.getPattern());
        }
        break;
      }
    }
    return match;
  }

  private NaturalLanguageMethodMatch match(NaturalLanguageMethod naturalLanguageMethod, String inputString) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.FINEST, "Checking " + naturalLanguageMethod.getPattern() + " for match with '" + inputString + "'");
    }
    Matcher matcher = naturalLanguageMethod.getPattern().matcher(inputString);
    if (matcher.matches()) {
      return new NaturalLanguageMethodMatch(naturalLanguageMethod, matcher);
    }
    return null;
  }

  <T> T addAndGetComponent(Class<T> componentClass) {
    //noinspection ConstantIfStatement
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
