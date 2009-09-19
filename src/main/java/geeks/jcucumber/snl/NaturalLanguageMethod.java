package geeks.jcucumber.snl;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.List;

/**
 * A structured natural language method.
*
* @author pabstec
*/
public class NaturalLanguageMethod {
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
}
