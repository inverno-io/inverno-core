/*
 * Copyright 2018 Jeremy KUHN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * <p>
 * The Inverno Framework annotation module, providing annotations to be used to
 * create modular applications based on Inversion of Control principle and
 * Dependency Injection.
 * </p>
 * 
 * <p>
 * The framework actually processes these annotations at compile time to
 * statically check bean dependencies. This prevents many common dependency
 * injection issues at runtime like missing dependencies, conflicts...
 * </p>
 * 
 * <p>
 * As a consequence, reflection is not required to instantiate beans which
 * reduce the module loading time. This also goes well with Java modular system
 * as there's no need for a module to be opened.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
module io.inverno.core.annotation {
	exports io.inverno.core.annotation;
}