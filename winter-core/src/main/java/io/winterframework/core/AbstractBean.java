/**
 * 
 */
package io.winterframework.core;

import io.winterframework.core.Module.Bean;
import io.winterframework.core.Module.BeanBuilder;

/**
 * <p>
 * Base class for {@link Bean} implementations
 * </p>
 * 
 * <p>
 * This class basically specifies {@link #createInstance()} and
 * {@link #destroyInstance(Object)} methods that respectively purely create and
 * destroy bean instances. These methods can then be used in {@link #create()}
 * and {@link #destroy()} methods which should provide higher logic (singleton,
 * prototype...).
 * </p>
 * 
 * <p>A bean should be registered in a module to be used.</p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see Bean
 * @see BeanBuilder
 */
abstract class AbstractBean<T> extends Bean<T> {

	/**
	 * <p>
	 * Create an abstract bean with the specified name.
	 * </p>
	 * 
	 * @param name
	 *            The bean name
	 */
	public AbstractBean(String name) {
		super(name);
	}

	/**
	 * <p>
	 * Create a bean instance.
	 * </p>
	 * 
	 * @return A bean instance
	 */
	protected abstract T createInstance();

	/**
	 * <p>
	 * Destroy the specified bean instance.
	 * </p>
	 * 
	 * @param instance
	 *            The instance to destroy
	 */
	protected abstract void destroyInstance(T instance);
}
