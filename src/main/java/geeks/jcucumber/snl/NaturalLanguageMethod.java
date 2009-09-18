package geeks.jcucumber.snl;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.logging.Logger;

/**
 * A structured natural language method.
*
* @author pabstec
*/
public class NaturalLanguageMethod {
  private static final Logger LOGGER = Logger.getLogger(NaturalLanguageMethod.class.getName());
  private final Method method;
  private final Pattern pattern;
  private final List<TokenArgumentConverter> converters;

  public NaturalLanguageMethod(Method method, Pattern pattern, List<TokenArgumentConverter> converters) {
    this.method = method;
    this.pattern = pattern;
    this.converters = converters;
  }

  public Method getMethod() {
    return method;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public List<TokenArgumentConverter> getArgumentConverters() {
    return converters;
  }

  public NaturalLanguageMethodMatch match(String inputString) {
    if (LOGGER.isLoggable(StructuredNaturalLanguageExecuter.DEBUG_LEVEL)) {
      LOGGER.log(StructuredNaturalLanguageExecuter.DEBUG_LEVEL, "Checking " + getPattern() + " for match with '" + inputString + "'");
    }
    Matcher matcher = getPattern().matcher(inputString);
    if (matcher.matches()) {
      return new NaturalLanguageMethodMatch(this, matcher);
    }
    return null;
  }
}
