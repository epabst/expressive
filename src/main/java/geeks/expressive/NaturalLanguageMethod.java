package geeks.expressive;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.List;

/**
 * A structured natural language method.
*
* @author pabstec
*/
public class NaturalLanguageMethod {
  private final Pattern pattern;
  private final Method method;
  private final List<ArgumentConverter> converters;

  public NaturalLanguageMethod(Pattern pattern, Method method, List<ArgumentConverter> converters) {
    this.pattern = pattern;
    this.method = method;
    this.converters = converters;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public Method getMethod() {
    return method;
  }

  public List<ArgumentConverter> getArgumentConverters() {
    return converters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NaturalLanguageMethod that = (NaturalLanguageMethod) o;
    return pattern.pattern().equals(that.pattern.pattern()) && method.equals(that.method);
  }

  @Override
  public int hashCode() {
    int result = pattern.pattern().hashCode();
    result = 31 * result + method.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "NaturalLanguageMethod{" +
            "pattern=" + pattern +
            ", method=" + method +
            ", converters=" + converters +
            '}';
  }
}
