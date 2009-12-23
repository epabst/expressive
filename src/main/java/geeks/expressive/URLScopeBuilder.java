package geeks.expressive;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.AbstractConfiguration;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * A builder for a Scope.
 *
 * @author pabstec
 */
class URLScopeBuilder {
  private final Map<URL, String> descriptionByUrl = new LinkedHashMap<URL, String>();

  Scope build() {
    if (descriptionByUrl.isEmpty()) {
      return Scope.EMPTY;
    }
    return asScope(new ArrayList<String>(descriptionByUrl.values()), descriptionByUrl.keySet());
  }

  URLScopeBuilder add(URL url, String description) {
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