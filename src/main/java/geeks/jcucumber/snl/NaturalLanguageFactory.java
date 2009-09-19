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
  private static final char TOKEN_START_DELIM = '{';
  private static final char TOKEN_END_DELIM = '}';
  private final Map<List<?>,List<NaturalLanguageMethod>> cachedNaturalLanguageMethodsByClasses = new HashMap<List<?>,List<NaturalLanguageMethod>>();
  private final StructuredNaturalLanguageExecuter executer;

  public NaturalLanguageFactory(StructuredNaturalLanguageExecuter executer) {
    this.executer = executer;
  }

  public List<NaturalLanguageMethod> getNaturalLanguageMethods(MethodRegexIdentifier regexIdentifier, Class<?> classWithAnnotations) {
    List<Object> key = Arrays.asList(regexIdentifier, classWithAnnotations);
    List<NaturalLanguageMethod> naturalLanguageMethods = cachedNaturalLanguageMethodsByClasses.get(key);
    if (naturalLanguageMethods == null) {
      naturalLanguageMethods = findNaturalLanguageMethods(regexIdentifier, classWithAnnotations);
      cachedNaturalLanguageMethodsByClasses.put(key, naturalLanguageMethods);
    }
    return naturalLanguageMethods;
  }

  private List<NaturalLanguageMethod> findNaturalLanguageMethods(MethodRegexIdentifier regexIdentifier, Class<?> classWithAnnotations) {
    Method[] methods = classWithAnnotations.getDeclaredMethods();
    List<NaturalLanguageMethod> naturalLanguageMethods = new ArrayList<NaturalLanguageMethod>(methods.length);
    for (Method method : methods) {
      NaturalLanguageMethod naturalLanguageMethod = calculateNaturalLanguageMethod(method, regexIdentifier);
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
   * @param regexIdentifier the Annotation that the method should have
   * @return a NaturalLanguageMethod
   */
  private NaturalLanguageMethod calculateNaturalLanguageMethod(Method method, MethodRegexIdentifier regexIdentifier) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(Level.FINEST, "Seeing if method " + method + " matches " + regexIdentifier);
    }
    String regex = regexIdentifier.getRegex(method);
    if (regex == null) {
      return null;
    }
    return toNaturalLanguageMethod(method, regex);
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

  private List<ArgumentConverter> createConverters(Map<Integer, UsesToken> tokenByIndex, StructuredNaturalLanguageExecuter executer) {
    List<UsesToken> orderedTokensAndNulls = new ArrayList<UsesToken>(tokenByIndex.values());
    List<ArgumentConverter> converters = new ArrayList<ArgumentConverter>(orderedTokensAndNulls.size());
    for (UsesToken token : orderedTokensAndNulls) {
      converters.add(token != null ? new TokenArgumentConverter(token, executer) : ArgumentConverter.IDENTITY);
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
