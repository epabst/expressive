package geeks.expressive;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.injectors.AbstractInjector;

import java.io.Serializable;

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
    objectFactory.addInstance(ComponentInterface.class, new Component2());
    ComponentInterface instance1 = objectFactory.getInstance(ComponentInterface.class);
    assertEquals(instance1.getClass(), Component2.class);

    assertNull(objectFactory.getInstance(Serializable.class),
            "shouldn't be able to get the component by a different interface than it was registered with");
  }

  @Test
  public void shouldFailWithComponentInterfaceRegisteredMultipleTimes() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    objectFactory.addInstance(ComponentInterface.class, new Component2());
    try {
      objectFactory.addInstance(ComponentInterface.class, new Component3());
      fail("expected exception");
    }
    catch (PicoCompositionException e) {
      assertTrue(e.getMessage().contains("Duplicate"), e.toString());
      assertTrue(e.getMessage().contains(ComponentInterface.class.getName()), e.toString());
    }
  }

  @Test
  public void shouldNotAllowNullComponentClass() {
    DefaultObjectFactory objectFactory = new DefaultObjectFactory();
    try {
      objectFactory.addInstance(null, new Component2());
      fail("expected exception");
    }
    catch (NullPointerException e) {
      assertTrue(e.getMessage().contains("componentKey"), e.toString());
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

  public static class Component3 implements ComponentInterface, Serializable {
  }
}
