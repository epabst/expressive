package geeks.expressive;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;

import java.util.Map;
import java.util.HashMap;

/**
 * A wrapper for picocontainer.
 *
 * @author pabstec
 */
public class ObjectFactory {
  private final MutablePicoContainer container;
  private final Map<Class<?>, Object> addedComponents = new HashMap<Class<?>, Object>();

  public ObjectFactory() {
    this(new DefaultPicoContainer(new ConstructorInjection()));
  }

  public ObjectFactory(MutablePicoContainer container) {
    this.container = container;
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
