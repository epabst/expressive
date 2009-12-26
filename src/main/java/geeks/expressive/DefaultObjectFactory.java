package geeks.expressive;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.monitors.ComposingMonitor;

import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Modifier;

/**
 * A wrapper for picocontainer.
 *
 * @author pabstec
 */
public class DefaultObjectFactory implements ObjectFactory {
  private final MutablePicoContainer container;
  private final Set<Class<?>> registeredComponentClasses = new HashSet<Class<?>>();
  private final Set<Object> addedInstances = new HashSet<Object>();

  public DefaultObjectFactory() {
    this.container = new PicoBuilder().withCaching().withConstructorInjection().withMonitor(new ComposingMonitor(new ComposingMonitor.Composer() {
      @Override
      public Object compose(PicoContainer picoContainer, Object o) {
        if (o instanceof Class) {
          Class<?> componentClass = (Class<?>) o;
          if (registeredComponentClasses.add(componentClass)) {
            if (Modifier.isAbstract(componentClass.getModifiers())) {
              for (Object addedInstance : addedInstances) {
                if (componentClass.isInstance(addedInstance)) {
                  //auto-register the matching instance since it hasn't been registered yet
                  container.addComponent(componentClass, addedInstance);
                }
              }
              return getInstance(componentClass);
            }
            else {
              //auto-register the class since it hasn't been registered yet
              container.addComponent(componentClass);
              return getInstance(componentClass);
            }
          }
        }
        return null;
      }
    })).build();
  }

  public DefaultObjectFactory(MutablePicoContainer container) {
    this.container = container;
  }

  public <T> T getInstance(Class<T> componentClass) {
    return container.getComponent(componentClass);
  }

  public void addInstance(Object instance) {
    if (instance instanceof Class) {
      throw new IllegalArgumentException("a class is not a component instance: " + instance);
    }
    addedInstances.add(instance);
    container.addComponent(instance);
  }
}
