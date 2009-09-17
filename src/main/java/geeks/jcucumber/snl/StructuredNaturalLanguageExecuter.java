package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;
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
  private static final Logger LOGGER = Logger.getLogger(StructuredNaturalLanguageExecuter.class.getName());
  static final Level DEBUG_LEVEL = Level.INFO;
  private static final char TOKEN_START_DELIM = '{';
  private static final char TOKEN_END_DELIM = '}';

  public Object execute(String inputString, Class<? extends Annotation> annotationClass, Object objectWithAnnotations,
                        MutablePicoContainer container) {
    RegexMethodMatch methodAndMatcher = findMatchingPatternAndMethod(
            inputString, annotationClass, objectWithAnnotations.getClass());
    if (methodAndMatcher == null) {
      throw new IllegalStateException("No matching @" + annotationClass.getSimpleName()
              + " method found for '" + inputString + "' in " + objectWithAnnotations);
    }
    return methodAndMatcher.invoke(objectWithAnnotations, container, this);
  }

  private Object invoke(Method method, Annotation annotation) {
    return invokeWithArgs(method, annotation);
  }

  static Object invokeWithArgs(Method method, Object instance, Object... args) {
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "invoking " + method + " on " + instance + " with args " + Arrays.asList(args));
    }
    try {
      return method.invoke(instance, args);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } catch (InvocationTargetException e) {
      throw toIllegalStateException(e);
    }
  }

  private static IllegalStateException toIllegalStateException(InvocationTargetException exception) {
    if (exception.getCause() instanceof Error) {
      throw (Error) exception.getCause();
    }
    else if (exception.getCause() instanceof RuntimeException) {
      throw (RuntimeException) exception.getCause();
    }
    return new IllegalStateException(exception);
  }

  private RegexMethodMatch findMatchingPatternAndMethod(
          String inputString, Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    List<UsesToken> tokenList = getTokens(classWithAnnotations);
    Map<String, Method> methodByRegex = getMethodByRegexMap(annotationClass, classWithAnnotations);
    for (Map.Entry<String,Method> regexAndMethod : methodByRegex.entrySet()) {
      String regexWithTokens = regexAndMethod.getKey();
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
      String regexWithoutTokens = regex;
      Pattern pattern = Pattern.compile(regexWithoutTokens);
      Matcher matcher = pattern.matcher(inputString);
      if (LOGGER.isLoggable(DEBUG_LEVEL)) {
        LOGGER.log(DEBUG_LEVEL, "Checking " + pattern + " for match with '" + inputString + "'");
      }
      if (matcher.matches()) {
        Method method = regexAndMethod.getValue();
        return new RegexMethodMatch(method, matcher, tokenByIndex);
      }
    }
    return null;
  }

  private Map<String, Method> getMethodByRegexMap(Class<? extends Annotation> annotationClass, Class<?> classWithAnnotations) {
    Map<String, Method> methodByRegex = new LinkedHashMap<String, Method>();
    Method valueMethod = getValueMethod(annotationClass);
    Method[] methods = classWithAnnotations.getDeclaredMethods();
    for (Method method : methods) {
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.log(Level.FINEST, "Looking for annotation " + annotationClass.getSimpleName() + " in method: " + method);
      }
      Annotation annotation = method.getAnnotation(annotationClass);
      if (annotation != null) {
        String regex = (String) invoke(valueMethod, annotation);
        //todo make sure pattern not already defined
        methodByRegex.put(regex, method);
      }
    }
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "Found @" + annotationClass.getSimpleName() + " methods with regular expressions in "
              + classWithAnnotations + ": " + methodByRegex);
    }
    return methodByRegex;
  }

  private List<UsesToken> getTokens(Class<?> classWithAnnotations) {
    UsesTokens usesTokenAnnotation = classWithAnnotations.getAnnotation(UsesTokens.class);
    if (usesTokenAnnotation != null) {
      UsesToken[] tokens = usesTokenAnnotation.value();
      return Arrays.asList(tokens);
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
