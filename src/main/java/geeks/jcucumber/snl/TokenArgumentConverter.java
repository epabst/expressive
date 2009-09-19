package geeks.jcucumber.snl;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.annotation.Annotation;

/**
 * An argument converter based on a {@link UsesToken}. 
 *
 * @author pabstec
 */
public class TokenArgumentConverter implements ArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TokenArgumentConverter.class.getName());
  private final UsesToken token;
  private final StructuredNaturalLanguageExecuter executer;
  private final AnnotationMethodRegexIdentifier<? extends Annotation> regexIdentifier;
  private final Class<?> recognizerFromToken;

  public TokenArgumentConverter(UsesToken token, StructuredNaturalLanguageExecuter executer) {
    this.token = token;
    this.executer = executer;
    this.regexIdentifier = AnnotationMethodRegexIdentifier.getInstance(token.annotation());
    recognizerFromToken = (Class<?>) token.recognizer();
  }

  public Object convertArgument(String argString, NaturalLanguageMethod naturalLanguageMethod, int index) {
    if (token != null) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(Level.FINE, "Processing token " + token + " on string: " + argString);
      }
      return executer.execute(argString, regexIdentifier, getClassWithAnnotations(naturalLanguageMethod));
    }
    else {
      return argString;
    }
  }

  private Class<?> getClassWithAnnotations(NaturalLanguageMethod naturalLanguageMethod) {
    if (recognizerFromToken.equals(Void.class)) {
      return naturalLanguageMethod.getMethod().getDeclaringClass();
    }
    return recognizerFromToken;
  }
}
