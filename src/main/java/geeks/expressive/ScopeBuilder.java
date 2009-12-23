package geeks.expressive;

import org.reflections.util.ClasspathHelper;
import static org.reflections.util.DescriptorHelper.qNameToResourceName;
import org.reflections.util.Utils;
import org.reflections.util.AbstractConfiguration;
import org.reflections.util.FilterBuilder;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.net.URL;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

/**
 * A builder for a Scope.
 *
 * @author pabstec
 */
public class ScopeBuilder {
  private final Map<URL, String> descriptionByUrl = new LinkedHashMap<URL, String>();

  public Scope build() {
    if (descriptionByUrl.isEmpty()) {
      return Scope.EMPTY;
    }
    return asScope(new ArrayList<String>(descriptionByUrl.values()), descriptionByUrl.keySet());
  }

  public ScopeBuilder with(Class<?> aClass) {
    String description = aClass.getName();
    String classResourceName = qNameToResourceName(description) + ".class";
    return add(Utils.getEffectiveClassLoader().getResource(classResourceName), description);
  }

  public ScopeBuilder with(Package aPackage) {
    Collection<URL> urls = ClasspathHelper.getUrlsForPackagePrefix(aPackage.getName());
    for (URL url : urls) {
      add(url, aPackage.getName());
    }
    return this;
  }

  private ScopeBuilder add(URL url, String description) {
    descriptionByUrl.put(url, description);
    return this;
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
