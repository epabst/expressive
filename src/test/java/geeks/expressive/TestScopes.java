package geeks.expressive;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * A test for {@link Scopes}.
 *
 * @author pabstec
 */
public class TestScopes {
  @Test
  public void shouldProvideMeaningfulToString() {
    Class<?> aClass = ExternalConverterForTesting.class;
    assertTrue(Scopes.asScope(aClass).toString().contains(aClass.getName()));

    Package aPackage = ExternalConverterForTesting.class.getPackage();
    assertTrue(Scopes.asScope(aPackage).toString().contains(aPackage.getName()));
  }
}
