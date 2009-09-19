package geeks.jcucumber.snl;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A java reflection utility.
 *
 * @author pabstec
 */
class ReflectionUtil {
  private static final Logger LOGGER = Logger.getLogger(ReflectionUtil.class.getName());
  private static final Level DEBUG_LEVEL = Level.INFO;
         
  static Object invokeWithArgs(Method method, Object instance, Object... args) {
    if (LOGGER.isLoggable(DEBUG_LEVEL)) {
      LOGGER.log(DEBUG_LEVEL, "invoking " + method + " on " + instance + " with args " + Arrays.asList(args));
    }
    try {
      return method.invoke(instance, args);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } catch (InvocationTargetException e) {
      throw toIllegalStateException(e);
    }
  }

  private static IllegalStateException toIllegalStateException(InvocationTargetException exception) {
    if (exception.getCause() instanceof Error) {
      throw (Error) exception.getCause();
    }
    else if (exception.getCause() instanceof RuntimeException) {
      throw (RuntimeException) exception.getCause();
    }
    return new IllegalStateException(exception);
  }
}
