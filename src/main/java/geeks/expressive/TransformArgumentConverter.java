package geeks.expressive;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Method;

/**
 * An argument converter using methods matching a {@link MethodRegexAssociation}.
 *
 * @author pabstec
 */
class TransformArgumentConverter implements ArgumentConverter {
  private static final Logger LOGGER = Logger.getLogger(TransformArgumentConverter.class.getName());
  private final Expressive executer;
  private final MethodRegexAssociation transformRegexAssociation;

  public TransformArgumentConverter(final Class<?> targetType, Expressive executer, final MethodRegexAssociation transformRegexAssociation) {
    this.executer = executer;
    this.transformRegexAssociation = new MethodRegexAssociation() {
      public String findRegex(Method method) {
        if (targetType.isAssignableFrom(method.getReturnType())) {
          return transformRegexAssociation.findRegex(method);
        } else {
          //if the type doesn't match then don't use the Transform
          return null;
        }
      }
    };
  }

  public Object convertArgument(String argString, final NaturalLanguageMethod naturalLanguageMethod, int index) {
    Class<?> matchingClass = getClassWithTransforms(naturalLanguageMethod);
    MethodRegexAssociation noncircularRegexAssociation = new NoncircularRegexAssociation(naturalLanguageMethod, transformRegexAssociation);
    Expressive.NaturalLanguageMethodMatch match = executer.findMatchingNaturalLanguageMethod(
            argString, transformRegexAssociation, noncircularRegexAssociation, matchingClass);
    if (match != null && !match.getNaturalLanguageMethod().equals(naturalLanguageMethod)) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.log(Level.FINE, "Converting " + argString + " using " + match.getNaturalLanguageMethod().getMethod());
      }
      return executer.invokeMethod(match, matchingClass);
    } else {
      return argString;
    }
  }

  private Class<?> getClassWithTransforms(NaturalLanguageMethod naturalLanguageMethod) {
    return naturalLanguageMethod.getMethod().getDeclaringClass();
  }

  static class NoncircularRegexAssociation implements MethodRegexAssociation {
    private final NaturalLanguageMethod naturalLanguageMethod;
    private final MethodRegexAssociation delegate;

    public NoncircularRegexAssociation(NaturalLanguageMethod naturalLanguageMethod, MethodRegexAssociation delegate) {
      this.naturalLanguageMethod = naturalLanguageMethod;
      this.delegate = delegate;
    }

    public String findRegex(Method method) {
      if (!method.equals(naturalLanguageMethod.getMethod())) {
        return delegate.findRegex(method);
      } else {
        return null;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      NoncircularRegexAssociation that = (NoncircularRegexAssociation) o;
      return delegate.equals(that.delegate) && naturalLanguageMethod.equals(that.naturalLanguageMethod);
    }

    @Override
    public int hashCode() {
      int result = naturalLanguageMethod.hashCode();
      result = 31 * result + delegate.hashCode();
      return result;
    }
  }
}
