package jsonparser.parser;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jsonparser.exception.*;

class JsonParserTest {
	JsonParser parser = new JsonParser();

	private void assertEqualNumbers(double expected, JsonParser actual) {
		assertEquals(JsonType.NUMBER, actual.getValue().getType());
		assertEquals(expected, actual.getValue().getNum());
	}
	private void assertEqualStrings(String expected, JsonParser actual) {
		assertEquals(JsonType.STRING, actual.getValue().getType());
		assertEquals(expected, actual.getValue().getStr());
	}

//	@Disabled
	@Test
	void testNull() {
		assertEquals(JsonType.NULL, parser.parse("null").getValue().getType());
		assertEquals(JsonType.NULL, parser.parse(" null").getValue().getType());
		assertEquals(JsonType.NULL, parser.parse(" \t  \n\rnull").getValue().getType());
		assertEquals(JsonType.NULL, parser.parse(" null\t").getValue().getType());
	}

//	@Disabled
	@Test
	void testTrue() {
		assertEquals(JsonType.TRUE, parser.parse("true").getValue().getType());
		assertEquals(JsonType.TRUE, parser.parse(" true").getValue().getType());
		assertEquals(JsonType.TRUE, parser.parse(" \t  \n\rtrue").getValue().getType());
		assertEquals(JsonType.TRUE, parser.parse(" true\t").getValue().getType());
	}

//	@Disabled
	@Test
	void testFalse() {
		assertEquals(JsonType.FALSE, parser.parse("false").getValue().getType());
		assertEquals(JsonType.FALSE, parser.parse(" false").getValue().getType());
		assertEquals(JsonType.FALSE, parser.parse(" \t  \n\rfalse").getValue().getType());
		assertEquals(JsonType.FALSE, parser.parse(" false\t").getValue().getType());
	}
	
//	@Disabled
	@Test
	void testNumber() {
		assertEqualNumbers(0.0, parser.parse("0"));
		assertEqualNumbers(0.0, parser.parse("-0")); /* CAUTION for signed zeros! */
		assertEqualNumbers(0.0, parser.parse("-0.0")); /* CAUTION for signed zeros! */
		assertEqualNumbers(12, parser.parse("12"));
		assertEqualNumbers(-2, parser.parse("-2"));
		assertEqualNumbers(0.5, parser.parse("0.5"));
		assertEqualNumbers(-12.3, parser.parse("-12.3"));
		assertEqualNumbers(1e7, parser.parse("1e7"));
		assertEqualNumbers(0e-100, parser.parse("0e-100"));
		assertEqualNumbers(-5E-2, parser.parse("-5E-2"));
		assertEqualNumbers(0.53e-5, parser.parse("0.53e-5"));
		assertEqualNumbers(-12.53e+3, parser.parse("-12.53e+3"));
		assertEqualNumbers(Double.POSITIVE_INFINITY, parser.parse("2e400"));
		assertEqualNumbers(Double.NEGATIVE_INFINITY, parser.parse("-2e400"));
		assertEqualNumbers(0.0, parser.parse("1e-10000")); /* underflow */
		assertEqualNumbers(4.9406564584124654e-324, parser.parse("4.9406564584124654e-324")); /* min subnormal positive double */
		assertEqualNumbers(-4.9406564584124654e-324, parser.parse("-4.9406564584124654e-324")); 
		assertEqualNumbers(2.2250738585072009e-308, parser.parse("2.2250738585072009e-308")); /* max subnormal double */
		assertEqualNumbers(-2.2250738585072009e-308, parser.parse("-2.2250738585072009e-308")); 
		assertEqualNumbers(2.2250738585072014e-308, parser.parse("2.2250738585072014e-308")); /* min normal double */
		assertEqualNumbers(-2.2250738585072014e-308, parser.parse("-2.2250738585072014e-308")); 
		assertEqualNumbers(1.7976931348623157e308, parser.parse("1.7976931348623157e308")); /* max double */
		assertEqualNumbers(-1.7976931348623157e308, parser.parse("-1.7976931348623157e308"));
		assertEqualNumbers(1.0000000000000002, parser.parse("1.0000000000000002")); /* min number greater than 1 */
	}

//	@Disabled
	@Test
	void testString() {
		assertEqualStrings("", parser.parse("\"\"")); // "" in JSON context;
		assertEqualStrings("f3al se", parser.parse("\"f3al se\"")); // "f3al se" in JSON context;
		assertEqualStrings("h/a\\pp\ny", parser.parse("\"h\\/a\\\\pp\\ny\"")); // "h\/a\\pp\ny" in JSON context;
		assertEqualStrings("\\\n", parser.parse("\"\\\\\\n\"")); // "\\\n" in JSON context;
		assertEqualStrings("\u0010", parser.parse("\"\\u0010\"")); 
		assertEqualStrings("\uD834\uDD1E", parser.parse("\"\\uD834\\uDD1E\"")); 
	}
	
//	@Disabled
	@Test
	void testInvalidValueException() {
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("   \t \n\r");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("abcde");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("aull");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse(" nulk");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse(" \t  \n\raull");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("ture");
		});

		assertThrows(InvalidValueException.class, () -> {
			parser.parse("\"");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("\"ab 40.(dafr");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("\"\\a\"");
		});
	}

//	@Disabled
	@Test
	void testExpectValueException() {
		assertThrows(ExpectValueException.class, () -> {
			parser.parse("");
		});
	}

//	@Disabled
	@Test
	void testIndexOutOfBoundsException() {
		assertThrows(IndexOutOfBoundsException.class, () -> {
			parser.parse("nul");
		});
		assertThrows(IndexOutOfBoundsException.class, () -> {
			parser.parse(" \n  \tn");
		});
		assertThrows(IndexOutOfBoundsException.class, () -> {
			parser.parse("tr");
		});
		assertThrows(IndexOutOfBoundsException.class, () -> {
			parser.parse("fale");
		});
	}

//	@Disabled
	@Test
	void testRootNotSingularException() {
		assertThrows(RootNotSingularException.class, () -> {
			parser.parse("false \ttrue");
		});
		assertThrows(RootNotSingularException.class, () -> {
			parser.parse("0 a");
		});
		assertThrows(RootNotSingularException.class, () -> {
			parser.parse("-12.34e5 null");
		});
		assertThrows(RootNotSingularException.class, () -> {
			parser.parse("\"\" abc");
		});
	}

//	@Disabled
	@Test
	void testIncompleteNumberException() {
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-");
		});
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-a");
		});
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-.3");
		});

		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-0.");
		});
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-0.e2");
		});
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-0. abcd");
		});

		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-1E");
		});
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("-9e-");
		});
		assertThrows(IncompleteNumberException.class, () -> {
			parser.parse("40e+");
		});
	}

//	@Disabled
	@Test
	void testMissingDelimiterException() {
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("falsed");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("falsenull");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("0a");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("-0.50.6");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("-0.5e2110.6");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("-12.34e5null");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("\"\"null");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("\"he\\nll\\\\o\"null");
		});
	}
}
