/*
 * Copyright 2018 Jeremy KUHN
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
package io.inverno.core.compiler.socket;

import java.util.Set;

import io.inverno.core.compiler.spi.BeanQualifiedName;
import io.inverno.core.compiler.spi.SocketBeanInfo;

/**
 * <p>
 * Wirable socket beans should be created when building a compiled module info
 * so that module info builder can resolve and set the wired beans in the socket
 * bean info.
 * </p>
 * 
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 *
 */
public interface WirableSocketBeanInfo extends SocketBeanInfo {

	void setWiredBeans(Set<BeanQualifiedName> wiredBeans);
	
	void setWired(boolean wired);
}
