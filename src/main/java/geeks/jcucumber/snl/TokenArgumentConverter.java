package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * An argument converter based on a {@link UsesToken}. 
 *
 * @author pabstec
 */
public class TokenArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TokenArgumentConverter.class.getName());
  private final UsesToken token;
  private final StructuredNaturalLanguageExecuter executer;

  public TokenArgumentConverter(UsesToken token, StructuredNaturalLanguageExecuter executer) {
    this.token = token;
    this.executer = executer;
  }

  Object convertArgument(String argString) {
    Object convertedArg;
    if (token != null) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(Level.FINE, "Processing token " + token + " on string: " + argString);
      }
      convertedArg = executer.execute(argString, token.annotation(), getClassWithAnnotations(token));
    }
    else {
      convertedArg = argString;
    }
    return convertedArg;
  }

  private Class<?> getClassWithAnnotations(UsesToken token) {
    return (Class<?>) token.recognizer();
  }
}
