package geeks.expressive;

/**
 * A argument converter for a NaturalLanguageMethod's parameter.
 *
 * @author pabstec
 */
public interface ArgumentConverter {
  static final ArgumentConverter IDENTITY = new ArgumentConverter() {
    public Object convertArgument(String argString, NaturalLanguageMethod naturalLanguageMethod, int index) {
      return argString;
    }
  };

  /**
   * Converts an argument to the correct type to be able to call <code>naturalLanguageMethod</code>
   * as the paramater at the given index.
   * @param argString the string
   * @param naturalLanguageMethod the method
   * @param index the parameter index
   * @return the converted argument value
   */
  Object convertArgument(String argString, NaturalLanguageMethod naturalLanguageMethod, int index);
}
