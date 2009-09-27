package geeks.expressive;

/**
 * An Object factory for getting components instances.  This could be implemented using Spring, PicoContainer, etc. 
 *
 * @author pabstec
 */
public interface ObjectFactory {
  /**
   * Gets an instanceo of a component.
   * @param componentClass the class of the Component.
   * @param <T> the type of the component
   * @return the instance
   */
  <T> T getInstance(Class<T> componentClass);

  /**
   * Adds a specific instance of a component to prevent automatically constructing one.
   * @param instance the instance
   */
  void addInstance(Object instance);
}
