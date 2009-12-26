package geeks.expressive;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.ComposingMonitor;

/**
 * A wrapper for picocontainer.
 *
 * @author pabstec
 */
public class DefaultObjectFactory implements ObjectFactory {
  private final MutablePicoContainer container;

  public DefaultObjectFactory() {
    this.container = new PicoBuilder().withCaching().withConstructorInjection().withMonitor(new ComposingMonitor(new ComposingMonitor.Composer() {
      @Override
      public Object compose(PicoContainer picoContainer, Object o) {
        if (o instanceof Class) {
          Class<?> componentClass = (Class<?>) o;
          container.addComponent(componentClass);
          return getInstance(componentClass);
        }
        return null;
      }
    })).build();
  }

  public <T> T getInstance(Class<T> componentClass) {
    return container.getComponent(componentClass);
  }

  public void addInstance(Object instance) {
    container.addComponent(instance);
  }
}
