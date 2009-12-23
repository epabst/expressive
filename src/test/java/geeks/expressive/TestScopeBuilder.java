package geeks.expressive;

import org.reflections.adapters.ParallelStrategyHelper;
import org.picocontainer.injectors.ProviderAdapter;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.Method;

/**
 * A test for {@link ScopeBuilder}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestScopeBuilder {
  @Test
  public void shouldSupportEmptyScope() {
    Scope scope = new ScopeBuilder().build();
    assertEquals(scope.getMethodsAnnotatedWith(Test.class).size(), 0);
    assertSame(scope, Scope.EMPTY);
  }

  @Test
  public void doesNotSupportPackagesInsideOfJarsYet() {
    //it's ok to change this class as long it has deprecated methods
    Package[] packagesWithDeprecatedMethods = {ProviderAdapter.class.getPackage()};

    ScopeBuilder scopeBuilder = new ScopeBuilder();
    for (Package packageWithDeprecatedMethods : packagesWithDeprecatedMethods) {
      scopeBuilder.with(packageWithDeprecatedMethods);
    }
    try {
      scopeBuilder.build();
    }
    catch (NullPointerException e) {
      assertContainsStackTraceElement(ParallelStrategyHelper.class, "apply", e);
    }
  }

  private void assertContainsStackTraceElement(Class<?> aClass, String methodName, Throwable throwable) {
    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      if (stackTraceElement.getClassName().equals(aClass.getName()) && stackTraceElement.getMethodName().equals(methodName)) {
        return;
      }
    }
    AssertionError error = new AssertionError(String.format("stack trace element %s.%s not found within ", aClass.getName(), methodName) + throwable);
    error.initCause(throwable);
    throw error;
  }

  private Set<Class> getClasses(Set<Method> methods) {
    Set<Class> classes = new HashSet<Class>();
    for (Method method : methods) {
      classes.add(method.getDeclaringClass());
    }
    return classes;
  }
}