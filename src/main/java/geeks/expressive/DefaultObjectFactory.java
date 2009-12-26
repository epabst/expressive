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

  public DefaultObjectFactory() {
    this.container = new PicoBuilder().withCaching().withConstructorInjection().withMonitor(new ComposingMonitor(new AutoRegisterConcreteClassesComposer())).build();
  }

  public DefaultObjectFactory(MutablePicoContainer container) {
    this.container = container;
  }

  public <T> T getInstance(Class<T> componentClass) {
    return container.getComponent(componentClass);
  }

  public <T> void addInstance(Class<T> componentClass, T instance) {
    container.addComponent(componentClass, instance);
  }

  public static class AutoRegisterConcreteClassesComposer implements ComposingMonitor.Composer {
    private final Set<Class<?>> registeredComponentClasses = new HashSet<Class<?>>();

    @Override
    public Object compose(PicoContainer picoContainer, Object o) {
      if (o instanceof Class) {
        Class<?> componentClass = (Class<?>) o;
        if (!Modifier.isAbstract(componentClass.getModifiers())) {
          //prevent an infinite loop by only registering each componentClass once
          if (registeredComponentClasses.add(componentClass)) {
            //auto-register the class since it hasn't been registered yet
            ((MutablePicoContainer) picoContainer).addComponent(componentClass);
            return picoContainer.getComponent(componentClass);
          }
        }
      }
      return null;
    }
  }
}
