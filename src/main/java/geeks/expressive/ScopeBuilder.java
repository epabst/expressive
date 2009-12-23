package geeks.expressive;

import org.reflections.util.ClasspathHelper;
import static org.reflections.util.DescriptorHelper.qNameToResourceName;
import org.reflections.util.Utils;

import java.net.URL;
import java.util.Collection;

/**
 * A builder for a Scope.
 *
 * @author pabstec
 */
public class ScopeBuilder {
  private final URLScopeBuilder urlScopeBuilder = new URLScopeBuilder();

  public Scope build() {
    return urlScopeBuilder.build();
  }

  public ScopeBuilder with(Class<?> aClass) {
    String description = aClass.getName();
    String classResourceName = qNameToResourceName(description) + ".class";
    urlScopeBuilder.add(Utils.getEffectiveClassLoader().getResource(classResourceName), description);
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
