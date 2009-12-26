package geeks.expressive;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.ComposingMonitor;

import java.util.Map;
import java.util.HashMap;

/**
 * A wrapper for picocontainer.
 *
 * @author pabstec
 */
public class DefaultObjectFactory implements ObjectFactory {
  private final MutablePicoContainer container;
  private final Map<Class<?>, Object> addedComponents = new HashMap<Class<?>, Object>();

  public DefaultObjectFactory() {
    this.container = new PicoBuilder().withConstructorInjection().withMonitor(new ComposingMonitor(new ComposingMonitor.Composer() {
      @Override
      public Object compose(PicoContainer picoContainer, Object o) {
        if (o instanceof Class) {
          return getInstance((Class<?>) o);
        }
        return null;
      }
    })).build();
  }

  public <T> T getInstance(Class<T> componentClass) {
    @SuppressWarnings({"unchecked"})
    T instance = (T) addedComponents.get(componentClass);
    if (instance != null) {
      return instance;
    }
    //noinspection ConstantIfStatement
    if (false) {
      //todo why doesn't this work?
      container.addComponent(componentClass);
      return container.getComponent(componentClass);
    }
    if (!addedComponents.containsKey(componentClass)) {
      container.addComponent(componentClass);
      addedComponents.put(componentClass, container.getComponent(componentClass));
    }
    //noinspection unchecked
    return (T) addedComponents.get(componentClass);
  }

  public void addInstance(Object instance) {
    container.addComponent(instance);
    addedComponents.put(instance.getClass(), instance);
  }
}
