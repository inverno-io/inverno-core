/**
 * 
 */
package io.inverno.core.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * <p>
 * Used on a bean socket variable to inject a bean instance supplier instead of an actual bean instance.
 * </p>
 *
 * <p>
 * Lazy socket must then be of type {@link Supplier} and its formal parameter must designate the type dependency expected by the socket.
 * </p>
 *
 * <p>
 * Lazy sockets allow to lazily retrieve bean instances, this doesn't mean that the bean to inject is created on demand in the module, but that the instance retrieval is called on demand in the
 * dependent bean instance. This is particularly interesting when prototype beans are wired into a lazy socket, the dependent bean can then create new instances on demand during application operation.
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
@Retention(CLASS)
@Target({ PARAMETER })
public @interface Lazy {

}
