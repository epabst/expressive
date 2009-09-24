package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.reflect.Method;

/**
 * A factory for {@link geeks.jcucumber.snl.NaturalLanguageMethod}s.
 *
 * @author pabstec
 */
class NaturalLanguageFactory {
  private static final Logger LOGGER = Logger.getLogger(NaturalLanguageFactory.class.getName());
  private static final Level DEBUG_LEVEL = Level.INFO;
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();
  private final StructuredNaturalLanguageExecuter executer;

  public NaturalLanguageFactory(StructuredNaturalLanguageExecuter executer) {
    this.executer = executer;
  }

  public List<NaturalLanguageMethod> getNaturalLanguageMethods(MethodRegexIdentifier regexIdentifier, MethodRegexIdentifier transformMethodIdentifier, Class<?> classWithAnnotations) {
    List<Object> key = Arrays.asList(regexIdentifier, classWithAnnotations);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(regexIdentifier, transformMethodIdentifier, classWithAnnotations);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(MethodRegexIdentifier regexIdentifier, MethodRegexIdentifier transformMethodIdentifier, Class<?> classWithAnnotations) {
    Method[] methods = classWithAnnotations.getDeclaredMethods();
    List<NaturalLanguageMethod> naturalLanguageMethods = new ArrayList<NaturalLanguageMethod>(methods.length);
    for (Method method : methods) {
      NaturalLanguageMethod naturalLanguageMethod = calculateNaturalLanguageMethod(method, regexIdentifier, transformMethodIdentifier);
      if (naturalLanguageMethod != null) {
        naturalLanguageMethods.add(naturalLanguageMethod);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found " + regexIdentifier + " methods with regular expressions in "
              + classWithAnnotations + ": " + naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  /**
   * Tries to convert a method into a NaturalLanguageMethod.
   * @param method the Method
   * @param regexIdentifier a matcher for candidate methods 
   * @param transformMethodIdentifier a matcher for methods that can transform values
   * @return a NaturalLanguageMethod
   */
  private NaturalLanguageMethod calculateNaturalLanguageMethod(Method method, MethodRegexIdentifier regexIdentifier, MethodRegexIdentifier transformMethodIdentifier) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.FINEST, "Seeing if method " + method + " matches " + regexIdentifier);
    }
    String regex = regexIdentifier.getRegex(method);
    if (regex == null) {
      return null;
    }
    return toNaturalLanguageMethod(method, regex, transformMethodIdentifier);
  }

  private NaturalLanguageMethod toNaturalLanguageMethod(Method method, String regexWithTokens, MethodRegexIdentifier transformMethodIdentifier) {
    Pattern pattern = Pattern.compile(regexWithTokens);
    return new NaturalLanguageMethod(method, pattern, createArgumentConverters(method, executer, transformMethodIdentifier));
  }

  private List<ArgumentConverter> createArgumentConverters(Method method, StructuredNaturalLanguageExecuter executer, MethodRegexIdentifier transformMethodIdentifier) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    List<ArgumentConverter> converters = new ArrayList<ArgumentConverter>(parameterTypes.length);
    for (Class<?> parameterType : parameterTypes) {
      converters.add(new TransformArgumentConverter(parameterType, executer, transformMethodIdentifier));
    }
    return converters;
  }

}
