package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
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
public class StructuredNaturalLanguageExecuter {
  private static final Logger LOGGER = Logger.getLogger(StructuredNaturalLanguageExecuter.class.getName());
  static final Level DEBUG_LEVEL = Level.INFO;
  private static final char TOKEN_START_DELIM = '{';
  private static final char TOKEN_END_DELIM = '}';
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();

  public Object execute(String inputString, Class<? extends Annotation> annotationClass, Object objectWithAnnotations,
                        MutablePicoContainer container) {
    List<NaturalLanguageMethod> naturalLanguageMethods = getNaturalLanguageMethods(annotationClass, objectWithAnnotations.getClass());
    for (NaturalLanguageMethod naturalLanguageMethod : naturalLanguageMethods) {
      NaturalLanguageMethodMatch match = naturalLanguageMethod.match(inputString);
      if (match != null) {
        return match.invokeMethod(objectWithAnnotations, container);
      }
    }
    throw new IllegalStateException("No matching @" + annotationClass.getSimpleName()
            + " method found for '" + inputString + "' in " + objectWithAnnotations);
  }

  protected List<NaturalLanguageMethod> getNaturalLanguageMethods(Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    List<Class<?>> key = Arrays.asList(annotationClass, classWithAnnotations);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(annotationClass, classWithAnnotations);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    List<UsesToken> tokenList = getTokens(classWithAnnotations);
    Method valueMethod = getValueMethod(annotationClass);
    Method[] methods = classWithAnnotations.getDeclaredMethods();
    List<NaturalLanguageMethod> naturalLanguageMethods = new ArrayList<NaturalLanguageMethod>(methods.length);
    for (Method method : methods) {
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.log(Level.FINEST, "Looking for annotation " + annotationClass.getSimpleName() + " in method: " + method);
      }
      Annotation annotation = method.getAnnotation(annotationClass);
      if (annotation != null) {
        String regex = (String) ReflectionUtil.invokeWithArgs(valueMethod, annotation);
        naturalLanguageMethods.add(toNaturalLanguageMethod(method, regex, tokenList));
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found @" + annotationClass.getSimpleName() + " methods with regular expressions in "
              + classWithAnnotations + ": " + naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private NaturalLanguageMethod toNaturalLanguageMethod(Method method, String regexWithTokens, List<UsesToken> tokenList) {
    String regex = regexWithTokens;
    Map<Integer, UsesToken> tokenByIndex = new TreeMap<Integer, UsesToken>();
    int groupIndexInOriginal = -1;
    //identify all of the pre-existing non-token groups
    while ((groupIndexInOriginal = regexWithTokens.indexOf("(", groupIndexInOriginal + 1)) >= 0) {
      tokenByIndex.put(groupIndexInOriginal, null);
    }
    for (UsesToken token : tokenList) {
      String tokenWithDelims = TOKEN_START_DELIM + token.token() + TOKEN_END_DELIM;
      int indexInOriginal = -1;
      while ((indexInOriginal = regexWithTokens.indexOf(tokenWithDelims, indexInOriginal + 1)) >= 0) {
        regex = regex.replace(tokenWithDelims, '(' + token.regex() + ')');
        tokenByIndex.put(indexInOriginal, token);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Established tokenByIndex: " + tokenByIndex);
    }
    return new NaturalLanguageMethod(method, Pattern.compile(regex), createConverters(tokenByIndex));
  }

  List<TokenArgumentConverter> createConverters(Map<Integer, UsesToken> tokenByIndex) {
    List<UsesToken> orderedTokensAndNulls = new ArrayList<UsesToken>(tokenByIndex.values());
    List<TokenArgumentConverter> converters = new ArrayList<TokenArgumentConverter>(orderedTokensAndNulls.size());
    for (UsesToken token : orderedTokensAndNulls) {
      converters.add(new TokenArgumentConverter(token, this));
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

  private Method getValueMethod(Class<? extends Annotation> annotationClass) {
    Method valueMethod;
    try {
      valueMethod = annotationClass.getMethod("value");
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
    return valueMethod;
  }

  static <T> T addAndGetComponent(Class<T> componentClass, MutablePicoContainer container) {
    if (container.getComponent(componentClass) == null) {
      container.addComponent(componentClass);
    }
    return container.getComponent(componentClass);
  }
}
