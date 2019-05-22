/**
 * 
 */
package io.winterframework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.winterframework.core.Module.BeanBuilder;

/**
 * <p>Base class for {@link BeanBuilder} implementations</p>
 * 
 * @author jkuhn
 * @since 1.0
 * 
 * @see Bean
 * @see BeanBuilder
 */
abstract class AbstractBeanBuilder<T> implements BeanBuilder<T> {

	/**
	 * The bean name.
	 */
	protected String beanName;
	
	/**
	 * The bean constructor.
	 */
	protected Supplier<T> constructor;

	/**
	 * The list of bean initialization operations that must be executed after a
	 * bean instance creation and dependency injection.
	 */
	protected List<Consumer<T>> inits;

	/**
	 * The list of bean destructions operations that must be executed after a
	 * bean instance creation and dependency injection.
	 */
	protected List<Consumer<T>> destroys;
	
	/**
	 * <p>
	 * Create a bean builder with the specified bean name and constructor.
	 * </p>
	 * 
	 * @param beanName
	 *            The bean name
	 * @param constructor
	 *            The bean constructor
	 */
	protected AbstractBeanBuilder(String beanName, Supplier<T> constructor) {
		this.beanName = beanName;
		this.constructor = constructor;

		this.inits = new ArrayList<>();
		this.destroys = new ArrayList<>();
	}
	
	/**
	 * <p>
	 * Add a bean initialization operation.
	 * </p>
	 * 
	 * @param init
	 *            The bean initialization operation.
	 * @return This builder
	 */
	@Override
	public BeanBuilder<T> init(Consumer<T> init) {
		this.inits.add(init);
		return this;
	}
	
	/**
	 * <p>
	 * Add a bean destruction operation.
	 * </p>
	 * 
	 * @param init
	 *            The bean destruction operation.
	 * @return This builder
	 */
	@Override
	public BeanBuilder<T> destroy(Consumer<T> destroy) {
		this.destroys.add(destroy);
		return this;
	}
}
