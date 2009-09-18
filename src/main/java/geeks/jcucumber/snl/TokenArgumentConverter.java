package geeks.jcucumber.snl;

import org.picocontainer.MutablePicoContainer;

import java.util.logging.Logger;

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

  Object convertArgument(String argString, Object instanceForMethod, MutablePicoContainer container) {
    Object convertedArg;
    if (token != null) {
      if (LOGGER.isLoggable(StructuredNaturalLanguageExecuter.DEBUG_LEVEL)) {
        LOGGER.log(StructuredNaturalLanguageExecuter.DEBUG_LEVEL, "Processing token " + token + " on string: " + argString);
      }
      Object objectWithAnnotationsFromToken = getObjectWithAnnotations(token, instanceForMethod, container);
      convertedArg = executer.execute(argString, token.annotation(), objectWithAnnotationsFromToken, container);
    }
    else {
      convertedArg = argString;
    }
    return convertedArg;
  }

  Object getObjectWithAnnotations(UsesToken token, Object defaultObjectWithAnnotations, MutablePicoContainer container) {
    Class<?> classWithAnnotations = token.recognizer();
    if (classWithAnnotations == Void.class) {
      return defaultObjectWithAnnotations;
    }
    return StructuredNaturalLanguageExecuter.addAndGetComponent(classWithAnnotations, container);
  }
}
