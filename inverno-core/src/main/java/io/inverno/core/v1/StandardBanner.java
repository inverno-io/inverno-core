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
package io.inverno.core.v1;

import java.io.PrintStream;
import java.util.Optional;

/**
 * <p>
 * A standard {@link Banner} implementation that displays basic useful information about the module and the runtime environment.
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
 * @author <a href="mailto:jeremy.kuhn@inverno.io">Jeremy Kuhn</a>
 * @since 1.0
 */
public class StandardBanner implements Banner {

	/**
	 * The Banner header.
	 */
	private static final String BANNER_HEADER = "Inverno is starting...\n\n\n"
			+ "     ╔════════════════════════════════════════════════════════════════════════════════════════════╗\n"
			+ "     ║                      , ~~ ,                                                                ║\n"
			+ "     ║                  , '   /\\   ' ,                                                            ║\n"
			+ "     ║                 , __   \\/   __ ,      _                                                    ║\n"
			+ "     ║                ,  \\_\\_\\/\\/_/_/  ,    | |  ___  _    _  ___   __  ___   ___                 ║\n"
			+ "     ║                ,    _\\_\\/_/_    ,    | | / _ \\\\ \\  / // _ \\ / _|/ _ \\ / _ \\                ║\n"
			+ "     ║                ,   __\\_/\\_\\__   ,    | || | | |\\ \\/ /|  __/| | | | | | |_| |               ║\n"
			+ "     ║                 , /_/ /\\/\\ \\_\\ ,     |_||_| |_| \\__/  \\___||_| |_| |_|\\___/                ║\n"
			+ "     ║                  ,     /\\     ,                                                            ║\n"
			+ "     ║                    ,   \\/   ,         %35s                  ║\n"
			+ "     ║                      ' -- '                                                                ║\n"
			+ "     ╠════════════════════════════════════════════════════════════════════════════════════════════╣\n"
			+ "";
	
	/**
	 * The banner body format.
	 */
	private static final String BANNER_BODY = "     ║%92s║\n";
	/**
	 * The banner body item format.
	 */
	private static final String BANNER_BODY_ITEM = "     ║  %1s %-87s ║\n";
	/**
	 * The banner body property format.
	 */
	private static final String BANNER_BODY_PROPERTY = "     ║ %-19s %1s %-68s ║\n";
	
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
	
			bannerBuilder.append(this.buildBodyProperty("Java runtime", System.getProperty("java.runtime.name")));
			bannerBuilder.append(this.buildBodyProperty("Java version", System.getProperty("java.runtime.version")));
			bannerBuilder.append(this.buildBodyProperty("Java home", System.getProperty("java.home")));
	
			if (thisModule.getLayer() != null && System.getProperty("jdk.module.main") != null) {
				bannerBuilder.append(String.format(BANNER_BODY, ""));
	
				Optional<java.lang.Module> mainModule = thisModule.getLayer().findModule(System.getProperty("jdk.module.main"));
				if (mainModule.isPresent() && mainModule.get().getDescriptor() != null) {
					bannerBuilder.append(this.buildBodyProperty("Application module", mainModule.get().getDescriptor().name()));
					if (mainModule.get().getDescriptor().rawVersion().isPresent()) {
						bannerBuilder.append(this.buildBodyProperty("Application version", mainModule.get().getDescriptor().rawVersion().get()));
					}
					// Ideally Jar should be well generated module Jars with a module main class
					// added but Maven don't use the Jar utility...
	//				this.banner += String.format(BANNER_BODY, " Application class   : " + mainModule.get().getDescriptor().mainClass().get());
					bannerBuilder.append(this.buildBodyProperty("Application class", System.getProperty("jdk.module.main.class")));
					bannerBuilder.append(String.format(BANNER_BODY, ""));
				}
	
				bannerBuilder.append(this.buildBodyProperty("Modules", " "));
	
				this.getClass().getModule().getLayer().modules().stream()
					.map(m -> m.getDescriptor().toNameAndVersion())
					.sorted().forEach(s -> bannerBuilder.append(this.buildBodyItem(s)));
			}
	
			bannerBuilder.append(BANNER_FOOTER);
	
			this.banner = bannerBuilder.toString();
		}
		out.println(this.banner);
	}
	
	private StringBuilder buildBodyProperty(String name, String value) {
		StringBuilder bodyProperty = new StringBuilder();
		if(value == null || value.length() == 0) {
			bodyProperty.append(String.format(BANNER_BODY_PROPERTY, name, ":", ""));
		}
		else {
			int valueIndex = 0;
			while(valueIndex < value.length()) {
				bodyProperty.append(String.format(BANNER_BODY_PROPERTY, valueIndex == 0 ? name : "", valueIndex == 0 ? ":" : "", value.substring(valueIndex, Math.min(valueIndex + 68, value.length()))));
				valueIndex += 68;
			}
		}
		return bodyProperty;
	}
	
	private StringBuilder buildBodyItem(String value) {
		StringBuilder bodyProperty = new StringBuilder();
		if(value == null || value.length() == 0) {
			bodyProperty.append(String.format(BANNER_BODY_ITEM, "*", ""));
		}
		else {
			int valueIndex = 0;
			while(valueIndex < value.length()) {
				bodyProperty.append(String.format(BANNER_BODY_ITEM, valueIndex == 0 ? "*" : "", value.substring(valueIndex, Math.min(valueIndex + 87, value.length()))));
				valueIndex += 87;
			}
		}
		return bodyProperty;
	}
}
