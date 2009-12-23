package geeks.expressive;

import org.reflections.util.ClasspathHelper;

import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

/**
 * A builder for a Scope.
 *
 * @author pabstec
 */
public class ScopeBuilder {
  private final Set<ClassScope> classScopes = new HashSet<ClassScope>(); 
  private final URLScopeBuilder urlScopeBuilder = new URLScopeBuilder();
  
  public Scope build() {
    final Scope urlScope = urlScopeBuilder.build();
    if (classScopes.isEmpty()) {
      return urlScope;
    }
    return new Scope() {
      @Override
      public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        Set<Method> methods = new HashSet<Method>(urlScope.getMethodsAnnotatedWith(annotationClass));
        for (ClassScope classScope : classScopes) {
          methods.addAll(classScope.getMethodsAnnotatedWith(annotationClass));
        }
        return methods;
      }

      @Override
      public String toString() {
        Set<Scope> scopes = new HashSet<Scope>(classScopes);
        scopes.add(urlScope);
        return scopes.toString();
      }
    };
  }

  public ScopeBuilder with(Class<?> aClass) {
    classScopes.add(new ClassScope(aClass));
    return this;
  }

  public ScopeBuilder with(Package aPackage) {
    Collection<URL> urls = ClasspathHelper.getUrlsForPackagePrefix(aPackage.getName());
    for (URL url : urls) {
      urlScopeBuilder.add(url, aPackage.getName());
    }
    return this;
  }
}
