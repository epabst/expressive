package geeks.jcucumber.snl;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A default and example annotation for methods that recognize structured natural language.
 *
 * @author pabstec
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Matching {
  /**
   * The regular expression to match against for the target method to be used.
   * @return the regular expression string
   */
  String value();
}
