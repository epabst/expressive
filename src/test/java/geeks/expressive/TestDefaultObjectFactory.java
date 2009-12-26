package geeks.expressive;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.picocontainer.PicoCompositionException;

/**
 * A test for {@link DefaultObjectFactory}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestDefaultObjectFactory {
  @Test
  public void shouldSupportUnregisteredInjectedComponent() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    Component1 instance1 = objectFactory.getInstance(Component1.class);
    assertNotNull(instance1);
    assertNotNull(instance1.component2);
  }

  @Test
  public void shouldSupportRegisteredComponentInterface() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    objectFactory.addInstance(new Component2());
    ComponentInterface instance1 = objectFactory.getInstance(ComponentInterface.class);
    assertEquals(instance1.getClass(), Component2.class);
  }

  @Test
  public void shouldFailWithComponentInterfaceRegisteredMultipleTimes() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    objectFactory.addInstance(new Component2());
    objectFactory.addInstance(new Component3());
    try {
      objectFactory.getInstance(ComponentInterface.class);
      fail("expected exception");
    }
    catch (PicoCompositionException e) {
      assertTrue(e.getMessage().contains("Duplicate"));
      assertTrue(e.getMessage().contains(ComponentInterface.class.getName()));
    }
  }

  @Test
  public void shouldNotAllowAddingComponentClass() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    try {
      objectFactory.addInstance(Component1.class);
      fail("expected exception");
    }
    catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("class"), e.toString());
    }
  }

  public static class Component1 {

    private final Component2 component2;

    public Component1(Component2 component2) {
      this.component2 = component2;
    }

  }

  public static interface ComponentInterface {
  }

  public static class Component2 implements ComponentInterface {
  }

  public static class Component3 implements ComponentInterface {
  }
}
