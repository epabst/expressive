package geeks.jcucumber.snl;

import java.lang.annotation.*;

/**
 * Indicates that a class uses a token in its regular expressions.
 *
 * @author pabstec
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsesToken {
  /**
   * Gets the embeddable token string.  This is replaced by {@link #regex()} for the purposes of String matching.
   * @return the embeddable token string
   */
  String token();

  /**
   * Gets the regular expression string to match with.
   * @return the regular expression
   */
  String regex() default ".*?";

  /**
   * Gets the annotation class which recognizing methods will declare.
   * @return the used
   */
  Class<? extends Annotation> annotation() default Matching.class;

  /**
   * Gets the class that recognizes the Strings represented by the token.
   * If this does not specify a class, then the usable classes will be searched for
   * who have methods that declare the {@link #annotation()}.
   * @return the used recognizer class
   */
  Class recognizer() default Void.class;
}