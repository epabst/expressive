package geeks.expressive;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.AbstractConfiguration;
import org.reflections.util.FilterBuilder;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.Utils;
import static org.reflections.util.DescriptorHelper.qNameToResourceName;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.net.URL;

/**
 * A factory for scopes.
 *
 * @author pabstec
 */
public class Scopes {
  public static Scope asScope(final Class<?> aClass) {
    String classResourceName = qNameToResourceName(aClass.getName()) + ".class";
    final List<URL> urls = Arrays.asList(Utils.getEffectiveClassLoader().getResource(classResourceName));
    return asScope(Arrays.asList(aClass.getName()), urls);
  }

  public static Scope asScope(final Package aPackage) {
    return asScope(Arrays.asList(aPackage.getName()), ClasspathHelper.getUrlsForPackagePrefix(aPackage.getName()));
  }

  private static Scope asScope(List<String> pathsForDescription, final Collection<URL> urls) {
    return asScope(pathsForDescription, new Reflections(new AbstractConfiguration() {{
      setFilter(new FilterBuilder().include(".*"));
      setUrls(urls);
      setScanners(new MethodAnnotationsScanner());
    }}));
  }

  private static Scope asScope(final List<String> pathsForDescription, final Reflections reflections) {
    return new Scope() {
      public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return reflections.getMethodsAnnotatedWith(annotationClass);
      }

      @Override
      public String toString() {
        return new StringBuilder().append("Scope").append("{paths=").append(pathsForDescription).append('}').toString();
      }
    };
  }
}
