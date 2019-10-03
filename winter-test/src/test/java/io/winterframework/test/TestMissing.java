package io.winterframework.test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.winterframework.core.test.AbstractWinterTest;
import io.winterframework.core.test.WinterCompilationException;

public class TestMissing extends AbstractWinterTest {

	private static final String MODULE = "io.winterframework.test.missing";

	@Test
	public void testMissing() throws IOException {
		try {
			this.getWinterCompiler().compile(MODULE);
			Assertions.fail("Should throw a WinterCompilationException");
		}
		catch(WinterCompilationException e) {
			Assertions.assertEquals(2, e.getDiagnotics().size());
			
			String missingSingleMessage = "No bean was found matching required socket io.winterframework.test.missing:beanA:dataSource of type javax.sql.DataSource, consider defining a bean or a socket bean matching the socket in module io.winterframework.test.missing";
			String missingMultiMessage = "No bean was found matching required socket io.winterframework.test.missing:beanB:dataSources of type javax.sql.DataSource, consider defining a bean or socket bean matching the socket in module io.winterframework.test.missing";
			
			Assertions.assertTrue(e.getDiagnotics().stream().map(d -> d.getMessage(Locale.getDefault())).collect(Collectors.toList()).containsAll(List.of(missingSingleMessage, missingMultiMessage)));
		}
	}
}
