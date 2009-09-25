package geeks.expressive.cucumber;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for methods that indicates it represents a Then step
 *
 * @author pabstec
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Then {
  /**
   * The regular expression to match against for the target method to be used.
   * @return the regular expression string
   */
  public abstract String value();
}