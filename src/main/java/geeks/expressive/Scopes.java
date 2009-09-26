package geeks.expressive;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.AbstractConfiguration;
import org.reflections.util.FilterBuilder;
import org.reflections.util.ClasspathHelper;

import java.util.Arrays;
import java.util.Set;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

/**
 * A factory for scopes.
 *
 * @author pabstec
 */
public class Scopes {
  public static Scope asScope(final Class<?> aClass) {
    return asScope(new Reflections(new AbstractConfiguration() {{
      setFilter(new FilterBuilder().include(".*"));
      setUrls(Arrays.asList(ClasspathHelper.getUrlForClass(aClass)));
      setScanners(new MethodAnnotationsScanner());
    }}));
  }

  public static Scope asScope(final Package aPackage) {
    return asScope(new Reflections(new AbstractConfiguration() {{
      setFilter(new FilterBuilder().include(".*"));
      setUrls(ClasspathHelper.getUrlsForPackagePrefix(aPackage.getName()));
      setScanners(new MethodAnnotationsScanner());
    }}));
  }

  private static Scope asScope(final Reflections reflections) {
    return new Scope() {
      public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return reflections.getMethodsAnnotatedWith(annotationClass);
      }
    };
  }
}
