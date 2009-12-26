package geeks.expressive;

import org.testng.annotations.Test;
import static org.testng.Assert.assertNotNull;

/**
 * A test for {@link DefaultObjectFactory}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestDefaultObjectFactory {
  @Test
  public void testInjectedComponent() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    Component1 instance1 = objectFactory.getInstance(Component1.class);
    assertNotNull(instance1);
    assertNotNull(instance1.component2);
  }

  public static class Component1 {
    private final Component2 component2;

    public Component1(Component2 component2) {
      this.component2 = component2;
    }
  }

  public static class Component2 {

  }
}
