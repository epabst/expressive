package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.lang.reflect.Method;
import java.util.regex.Matcher;

/**
 * A success match of a NaturalLanguageMethod to a string.
*
* @author pabstec
*/
class NaturalLanguageMethodMatch {
  private final NaturalLanguageMethod naturalLanguageMethod;
  private final Matcher matcher;

  public NaturalLanguageMethodMatch(NaturalLanguageMethod naturalLanguageMethod, Matcher matcher) {
    this.naturalLanguageMethod = naturalLanguageMethod;
    this.matcher = matcher;
  }

  public NaturalLanguageMethod getNaturalLanguageMethod() {
    return naturalLanguageMethod;
  }

  public Matcher getMatcher() {
    return matcher;
  }

  Object invokeMethod(Object objectWithAnnotations, MutablePicoContainer container) {
    Method method = getNaturalLanguageMethod().getMethod();
    Matcher matcher = getMatcher();
    Object[] args = new Object[matcher.groupCount()];
    for (int i = 0; i < matcher.groupCount(); i++) {
      String group = matcher.group(i + 1);
      args[i] = naturalLanguageMethod.getArgumentConverters().get(i).convertArgument(group, objectWithAnnotations, container);
    }
    return ReflectionUtil.invokeWithArgs(method, objectWithAnnotations, args);
  }

}
