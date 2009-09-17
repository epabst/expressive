package geeks.jcucumber.snl;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a class uses a set of tokens in its regular expressions.
 *
 * @author pabstec
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsesTokens {
  /**
   * Gets the embeddable tokens used.
   * @return the used tokens
   */
  UsesToken[] value();
}