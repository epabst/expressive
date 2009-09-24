package geeks.expressive;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.reflect.Method;

/**
 * A factory for {@link NaturalLanguageMethod}s.
 *
 * @author pabstec
 */
class NaturalLanguageFactory {
  private static final Logger LOGGER = Logger.getLogger(NaturalLanguageFactory.class.getName());
  private static final Level DEBUG_LEVEL = Level.INFO;
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();
  private final Expressive executer;

  public NaturalLanguageFactory(Expressive executer) {
    this.executer = executer;
  }

  public List<NaturalLanguageMethod> getNaturalLanguageMethods(MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Class<?> classWithAnnotations) {
    List<Object> key = Arrays.asList(regexAssociation, classWithAnnotations);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(regexAssociation, transformRegexAssociation, classWithAnnotations);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(MethodRegexAssociation regexAssociation, MethodRegexAssociation transformRegexAssociation, Class<?> classWithAnnotations) {
    Method[] methods = classWithAnnotations.getDeclaredMethods();
    List<NaturalLanguageMethod> naturalLanguageMethods = new ArrayList<NaturalLanguageMethod>(methods.length);
    for (Method method : methods) {
      NaturalLanguageMethod naturalLanguageMethod = calculateNaturalLanguageMethod(method, regexAssociation, transformRegexAssociation);
      if (naturalLanguageMethod != null) {
        naturalLanguageMethods.add(naturalLanguageMethod);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found methods with " + regexAssociation + " in "
              + classWithAnnotations + ": " + naturalLanguageMethods);
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
    return new NaturalLanguageMethod(pattern, method, createArgumentConverters(method, executer, transformRegexAssociation));
  }

  private List<ArgumentConverter> createArgumentConverters(Method method, Expressive executer, MethodRegexAssociation transformRegexAssociation) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    List<ArgumentConverter> converters = new ArrayList<ArgumentConverter>(parameterTypes.length);
    for (Class<?> parameterType : parameterTypes) {
      converters.add(new TransformArgumentConverter(parameterType, executer, transformRegexAssociation));
    }
    return converters;
  }

}
