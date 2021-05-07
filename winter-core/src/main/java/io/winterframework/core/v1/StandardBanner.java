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
package io.winterframework.core.v1;

import java.io.PrintStream;
import java.util.Optional;

/**
 * <p>
 * A standard {@link Banner} implementation that displays basic useful
 * information about the module and the runtime environment.
 * </p>
 * 
 * <p>
 * It outputs the following information:
 * </p>
 * 
 * <ul>
 * <li>Java runtime, version and home.</li>
 * <li>Name of the root module, version and class.</li>
 * <li>The list of modules in the application module layer.</li>
 * </ul>
 * 
 * @author <a href="mailto:jeremy.kuhn@winterframework.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class StandardBanner implements Banner {

	/**
	 * The Banner header.
	 */
	private static final String BANNER_HEADER = "Winter is starting...\n\n\n"
			+ "     ╔════════════════════════════════════════════════════════════════════════════════════════════╗\n"
			+ "     ║                       , ~~ ,                                                               ║\n"
			+ "     ║                   , '   /\\   ' ,                 _                                         ║\n"
			+ "     ║                  , __   \\/   __ ,       _     _ (_)        _                               ║\n"
			+ "     ║                 ,  \\_\\_\\/\\/_/_/  ,     | | _ | | _   ___  | |_   ___   __                  ║\n"
			+ "     ║                 ,    _\\_\\/_/_    ,     | |/_\\| || | / _ \\ | __| / _ \\ / _|                 ║\n"
			+ "     ║                 ,   __\\_/\\_\\__   ,     \\  / \\  /| || | | || |_ |  __/| |                   ║\n"
			+ "     ║                  , /_/ /\\/\\ \\_\\ ,       \\/   \\/ |_||_| |_| \\__| \\___||_|                   ║\n"
			+ "     ║                   ,     /\\     ,                                                           ║\n"
			+ "     ║                     ,   \\/   ,         %35s                 ║\n"
			+ "     ║                       ' -- '                                                               ║\n"
			+ "     ╠════════════════════════════════════════════════════════════════════════════════════════════╣\n";

	/**
	 * The banner body.
	 */
	private static final String BANNER_BODY = "     ║%-92s║\n";

	/**
	 * The banner footer.
	 */
	private static final String BANNER_FOOTER = "     ╚════════════════════════════════════════════════════════════════════════════════════════════╝\n";

	/**
	 * The displayed banner.
	 */
	private String banner;

	@Override
	public void print(PrintStream out) {
		if(this.banner == null) {
			String version = "<< n/a >>";
	
			java.lang.Module thisModule = this.getClass().getModule();
			if (thisModule.getDescriptor() != null) {
				if (this.getClass().getModule().getDescriptor().rawVersion().isPresent()) {
					version = "-- " + this.getClass().getModule().getDescriptor().rawVersion().get() + " --";
				}
			}
	
			StringBuilder bannerBuilder = new StringBuilder();
	
			bannerBuilder.append(String.format(BANNER_HEADER, version));
	
			bannerBuilder.append(String.format(BANNER_BODY, " Java runtime        : " + System.getProperty("java.runtime.name")));
			bannerBuilder.append(String.format(BANNER_BODY, " Java version        : " + System.getProperty("java.runtime.version")));
			bannerBuilder.append(String.format(BANNER_BODY, " Java home           : " + System.getProperty("java.home")));
	
			if (thisModule.getLayer() != null && System.getProperty("jdk.module.main") != null) {
				bannerBuilder.append(String.format(BANNER_BODY, ""));
	
				Optional<java.lang.Module> mainModule = thisModule.getLayer().findModule(System.getProperty("jdk.module.main"));
				if (mainModule.isPresent() && mainModule.get().getDescriptor() != null) {
					bannerBuilder.append(String.format(BANNER_BODY, " Application module  : " + mainModule.get().getDescriptor().name()));
					if (mainModule.get().getDescriptor().rawVersion().isPresent()) {
						bannerBuilder.append(String.format(BANNER_BODY, " Application version : " + mainModule.get().getDescriptor().rawVersion().get()));
					}
					// Ideally Jar should be well generated module Jars with a module main class
					// added but Maven don't use the Jar utility...
	//				this.banner += String.format(BANNER_BODY, " Application class   : " + mainModule.get().getDescriptor().mainClass().get());
					bannerBuilder.append(String.format(BANNER_BODY, " Application class   : " + System.getProperty("jdk.module.main.class")));
					bannerBuilder.append(String.format(BANNER_BODY, ""));
				}
	
				bannerBuilder.append(String.format(BANNER_BODY, " Modules             : "));
	
				this.getClass().getModule().getLayer().modules().stream()
					.map(m -> m.getDescriptor().toNameAndVersion())
					.sorted().forEach(s -> bannerBuilder.append(String.format(BANNER_BODY, "  * " + s)));
			}
	
			bannerBuilder.append(BANNER_FOOTER);
	
			this.banner = bannerBuilder.toString();
		}
		out.println(this.banner);
	}
}
