package geeks.expressive;

/**
 * A factory for scopes.
 *
 * @author pabstec
 */
public class Scopes {
  public static Scope asScope(final Class<?> aClass) {
    return new ScopeBuilder().with(aClass).build();
  }

  public static Scope asScope(final Package aPackage) {
    return new ScopeBuilder().with(aPackage).build();
  }
}
