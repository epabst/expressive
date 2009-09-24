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
  private final List<ArgumentConverter> converters;

  public NaturalLanguageMethod(Method method, Pattern pattern, List<ArgumentConverter> converters) {
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

  public List<ArgumentConverter> getArgumentConverters() {
    return converters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NaturalLanguageMethod that = (NaturalLanguageMethod) o;
    return method.equals(that.method) && pattern.pattern().equals(that.pattern.pattern());
  }

  @Override
  public int hashCode() {
    int result = method.hashCode();
    result = 31 * result + pattern.pattern().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "NaturalLanguageMethod{" +
            "method=" + method +
            ", pattern=" + pattern +
            ", converters=" + converters +
            '}';
  }
}
