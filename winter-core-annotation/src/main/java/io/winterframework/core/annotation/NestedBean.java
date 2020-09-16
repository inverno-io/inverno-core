/*
 * Copyright 2020 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.winterframework.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Indicates that the return value of a particular method on a module bean or a
 * socket bean should be considered as a bean and considered during dependency
 * injection.
 * </p>
 * 
 * <p>
 * A nested bean can be seen as a lightweight bean as it doesn't follow any
 * particular lifecycle. Unlike regular module bean, it can be null and
 * {@link NullPointerException} can then be thrown at runtime. They have to be
 * used wisely and sparingly when they make sense (eg. cascading configuration
 * data to component modules)
 * </p>
 * 
 * @author jkuhn
 * @since 1.1
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD })
public @interface NestedBean {

}
