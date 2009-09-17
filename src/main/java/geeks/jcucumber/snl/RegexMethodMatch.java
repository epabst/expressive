package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * A regex match to a token.
*
* @author pabstec
*/
class RegexMethodMatch {
  private static final Logger LOGGER = Logger.getLogger(RegexMethodMatch.class.getName());
  private final Method method;
  private final Matcher matcher;
  private final Map<Integer, UsesToken> tokenByIndex;

  public RegexMethodMatch(Method method, Matcher matcher, Map<Integer, UsesToken> tokenByIndex) {
    this.method = method;
    this.matcher = matcher;
    this.tokenByIndex = tokenByIndex;
  }

  public Method getMethod() {
    return method;
  }

  public Matcher getMatcher() {
    return matcher;
  }

  public Map<Integer, UsesToken> getTokenByIndex() {
    return tokenByIndex;
  }

  Object invoke(Object objectWithAnnotations, MutablePicoContainer container, StructuredNaturalLanguageExecuter executer) {
    Method method = getMethod();
    Matcher matcher = getMatcher();
    Object[] args = new Object[matcher.groupCount()];
    List<UsesToken> orderedTokensAndNulls = new ArrayList<UsesToken>(tokenByIndex.values());
    for (int i = 0; i < matcher.groupCount(); i++) {
      String group = matcher.group(i + 1);
      UsesToken token = orderedTokensAndNulls.get(i);
      if (token != null) {
        if (LOGGER.isLoggable(StructuredNaturalLanguageExecuter.DEBUG_LEVEL)) {
          LOGGER.log(StructuredNaturalLanguageExecuter.DEBUG_LEVEL, "Processing token " + token + " on string: " + group);
        }
        Object objectWithAnnotationsFromToken = getObjectWithAnnotations(token, objectWithAnnotations, container);
        args[i] = executer.execute(group, token.annotation(), objectWithAnnotationsFromToken, container);
      }
      else {
        args[i] = group;
      }
    }
    return StructuredNaturalLanguageExecuter.invokeWithArgs(method, objectWithAnnotations, args);
  }

  private Object getObjectWithAnnotations(UsesToken token, Object defaultObjectWithAnnotations, MutablePicoContainer container) {
    Class<?> classWithAnnotations = token.recognizer();
    if (classWithAnnotations == Void.class) {
      return defaultObjectWithAnnotations;
    }
    return StructuredNaturalLanguageExecuter.addAndGetComponent(classWithAnnotations, container);
  }
}
