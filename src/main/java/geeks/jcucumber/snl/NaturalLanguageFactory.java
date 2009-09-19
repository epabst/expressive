package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

/**
 * A factory for {@link geeks.jcucumber.snl.NaturalLanguageMethod}s.
 *
 * @author pabstec
 */
class NaturalLanguageFactory {
  private static final Logger LOGGER = Logger.getLogger(NaturalLanguageFactory.class.getName());
  private static final Level DEBUG_LEVEL = Level.INFO;
  private static final char TOKEN_START_DELIM = '{';
  private static final char TOKEN_END_DELIM = '}';
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();
  private final StructuredNaturalLanguageExecuter executer;

  public NaturalLanguageFactory(StructuredNaturalLanguageExecuter executer) {
    this.executer = executer;
  }

  public List<NaturalLanguageMethod> getNaturalLanguageMethods(Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    List<Class<?>> key = Arrays.asList(annotationClass, classWithAnnotations);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(annotationClass, classWithAnnotations);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    Method[] methods = classWithAnnotations.getDeclaredMethods();
    List<NaturalLanguageMethod> naturalLanguageMethods = new ArrayList<NaturalLanguageMethod>(methods.length);
    for (Method method : methods) {
      NaturalLanguageMethod naturalLanguageMethod = calculateNaturalLanguageMethod(method, annotationClass);
      if (naturalLanguageMethod != null) {
        naturalLanguageMethods.add(naturalLanguageMethod);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found @" + annotationClass.getSimpleName() + " methods with regular expressions in "
              + classWithAnnotations + ": " + naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  /**
   * Tries to convert a method into a NaturalLanguageMethod.
   * @param method the Method
   * @param annotationClass the Annotation that the method should have
   * @return a NaturalLanguageMethod
   */
  private NaturalLanguageMethod calculateNaturalLanguageMethod(Method method, Class<? extends Annotation> annotationClass) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.FINEST, "Looking for annotation " + annotationClass.getSimpleName() + " in method: " + method);
    }
    Annotation annotation = method.getAnnotation(annotationClass);
    NaturalLanguageMethod naturalLanguageMethod = null;
    if (annotation != null) {
      Method valueMethod = getValueMethod(annotationClass);
      String regex = (String) ReflectionUtil.invokeWithArgs(valueMethod, annotation);
      naturalLanguageMethod = toNaturalLanguageMethod(method, regex);
    }
    return naturalLanguageMethod;
  }

  private Method getValueMethod(Class<? extends Annotation> annotationClass) {
    Method valueMethod;
    try {
      valueMethod = annotationClass.getMethod("value");
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
    return valueMethod;
  }

  private NaturalLanguageMethod toNaturalLanguageMethod(Method method, String regexWithTokens) {
    String regex = regexWithTokens;
    Map<Integer, UsesToken> tokenByIndex = new TreeMap<Integer, UsesToken>();
    int groupIndexInOriginal = -1;
    //identify all of the pre-existing non-token groups
    while ((groupIndexInOriginal = regexWithTokens.indexOf("(", groupIndexInOriginal + 1)) >= 0) {
      tokenByIndex.put(groupIndexInOriginal, null);
    }
    for (UsesToken token : getTokens(method.getDeclaringClass())) {
      String tokenWithDelims = TOKEN_START_DELIM + token.token() + TOKEN_END_DELIM;
      int indexInOriginal = -1;
      while ((indexInOriginal = regexWithTokens.indexOf(tokenWithDelims, indexInOriginal + 1)) >= 0) {
        regex = regex.replace(tokenWithDelims, '(' + token.regex() + ')');
        tokenByIndex.put(indexInOriginal, token);
      }
    }
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, "Established tokenByIndex: " + tokenByIndex);
    }
    return new NaturalLanguageMethod(method, Pattern.compile(regex), createConverters(tokenByIndex, executer));
  }

  private List<TokenArgumentConverter> createConverters(Map<Integer, UsesToken> tokenByIndex, StructuredNaturalLanguageExecuter executer) {
    List<UsesToken> orderedTokensAndNulls = new ArrayList<UsesToken>(tokenByIndex.values());
    List<TokenArgumentConverter> converters = new ArrayList<TokenArgumentConverter>(orderedTokensAndNulls.size());
    for (UsesToken token : orderedTokensAndNulls) {
      converters.add(new TokenArgumentConverter(token, executer));
    }
    return converters;
  }

  private List<UsesToken> getTokens(Class<?> classWithAnnotations) {
    UsesTokens usesTokenAnnotation = classWithAnnotations.getAnnotation(UsesTokens.class);
    if (usesTokenAnnotation != null) {
      return Arrays.asList(usesTokenAnnotation.value());
    }
    else {
      return Collections.emptyList();
    }
  }
}
