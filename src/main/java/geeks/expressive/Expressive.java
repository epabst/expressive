package geeks.expressive;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A parser for structured natural language that executes it based on mapping it to methods.
 * This is based on <a href="http://cukes.info">Cucumber</a>.
 *
 * @author pabstec
 */
public class Expressive {
  static final Logger LOGGER = Logger.getLogger(Expressive.class.getName());
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();
  private static final Level DEBUG_LEVEL = Level.FINE;
  public static final String EVERYTHING_ELSE_REGEX = "^(.*)$";
  private final ObjectFactory objectFactory;

  public Expressive(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  public void execute(BufferedReader reader, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Scope scope) throws IOException {
    String string;
    while ((string = reader.readLine()) != null) {
      execute(string, regexAssociation, transformRegexAssociation, scope);
    }
  }

  public Object execute(String languageString, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    NaturalLanguageMethodMatch match = findMatchingNaturalLanguageMethod(languageString, regexAssociation, transformRegexAssociation, scope);
    if (match != null) {
      return invokeMethod(match);
    }
    throw new IllegalStateException("No method with " + regexAssociation
            + " found for '" + languageString + "' in " + scope);
  }

  NaturalLanguageMethodMatch findMatchingNaturalLanguageMethod(String languageString, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    List<NaturalLanguageMethod> naturalLanguageMethods = getNaturalLanguageMethods(regexAssociation, transformRegexAssociation, scope);
    NaturalLanguageMethodMatch defaultMatch = null;
    for (NaturalLanguageMethod naturalLanguageMethod : naturalLanguageMethods) {
      NaturalLanguageMethodMatch match = match(naturalLanguageMethod, languageString);
      if (match != null) {
        NaturalLanguageMethod method = match.getNaturalLanguageMethod();
        if (isCatchAllMethod(method)) {
          defaultMatch = match;
        }
        else {
          if (LOGGER.isLoggable(DEBUG_LEVEL)) {
            LOGGER.log(DEBUG_LEVEL, "Found match for '" + languageString + "': " + match.getNaturalLanguageMethod());
          }
          return match;
        }
      }
    }
    if (defaultMatch != null) {
      if (LOGGER.isLoggable(DEBUG_LEVEL)) {
        LOGGER.log(DEBUG_LEVEL, "Found match for '" + languageString + "': " + defaultMatch.getNaturalLanguageMethod());
      }
    }
    return defaultMatch;
  }

  private boolean isCatchAllMethod(NaturalLanguageMethod method) {
    return method.getPattern().pattern().equals(EVERYTHING_ELSE_REGEX);
  }

  private List<NaturalLanguageMethod> getNaturalLanguageMethods(MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    List<Object> key = Arrays.asList(regexAssociation, transformRegexAssociation, scope);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(regexAssociation, transformRegexAssociation, scope);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    List<NaturalLanguageMethod> naturalLanguageMethods = new LinkedList<NaturalLanguageMethod>();
    for (Method method : regexAssociation.getMethods(scope)) {
      NaturalLanguageMethod naturalLanguageMethod = calculateNaturalLanguageMethod(method, regexAssociation, transformRegexAssociation, scope);
      if (naturalLanguageMethod != null) {
        naturalLanguageMethods.add(naturalLanguageMethod);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found methods with " + regexAssociation + " in "
              + scope + ": " + naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  /**
   * Tries to convert a method into a NaturalLanguageMethod.
   * @param method the Method
   * @param regexAssociation a matcher for candidate methods
   * @param transformRegexAssociation a matcher for methods that can transform values
   * @param scope which classes to consider
   * @return a NaturalLanguageMethod
   */
  private NaturalLanguageMethod calculateNaturalLanguageMethod(Method method, MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.FINEST, "Seeing if method " + method + " has " + regexAssociation);
    }
    String regex = regexAssociation.findRegex(method);
    if (regex == null) {
      return null;
    }
    return toNaturalLanguageMethod(method, regex, transformRegexAssociation, scope);
  }

  private NaturalLanguageMethod toNaturalLanguageMethod(Method method, String regexWithTokens, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    Pattern pattern = Pattern.compile(regexWithTokens);
    return new NaturalLanguageMethod(pattern, method, createArgumentConverters(method, transformRegexAssociation, scope));
  }

  private List<ArgumentConverter> createArgumentConverters(Method method, MethodRegexAssociation transformRegexAssociation, Scope scope) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    List<ArgumentConverter> converters = new ArrayList<ArgumentConverter>(parameterTypes.length);
    for (Class<?> parameterType : parameterTypes) {
      converters.add(new TransformArgumentConverter(parameterType, this, transformRegexAssociation, scope));
    }
    return converters;
  }

  Object invokeMethod(NaturalLanguageMethodMatch match) {
    return invokeMethod(match, match.getNaturalLanguageMethod().getMethod());
  }

  Object invokeMethod(NaturalLanguageMethodMatch match, Method method) {
    Object objectToInvoke = addAndGetComponent(method.getDeclaringClass());
    return match.invokeMethod(objectToInvoke);
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
    return objectFactory.getInstance(componentClass);
  }

  public void executeEvent(MethodSpecifier eventMethodSpecifier, Scope scope) {
    Set<Method> eventMethods = eventMethodSpecifier.getMethods(scope);
    for (Method eventMethod : eventMethods) {
      ReflectionUtil.invokeWithArgs(eventMethod, addAndGetComponent(eventMethod.getDeclaringClass()));
    }
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
