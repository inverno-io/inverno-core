/*
 * Copyright 2019 Jeremy KUHN
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
package io.inverno.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * The Selector annotation is used to identify selector annotations.
 * </p>
 *
 * <pre>{@code
 * @Retention(RetentionPolicy.CLASS)
 * @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
 * @Selector
 * public @interface MySelector {
 *    ...
 * }
 * }</pre>
 *
 * <p>
 * Selectors are particular types of annotations used to annotate bean socket or socket bean in order to filter beans during dependency injection (e.g. beans annotated with a particular annotation,
 * beans whose name matches a particular regular expression...).
 * </p>
 *
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface Selector {

}
