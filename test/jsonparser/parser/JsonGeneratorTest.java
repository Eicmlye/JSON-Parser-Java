package jsonparser.parser;

import static org.junit.jupiter.api.Assertions.*;

//import java.util.ArrayList;
//import java.util.HashMap;

//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JsonGeneratorTest {
	JsonParser parser = new JsonParser();
	JsonGenerator generator = new JsonGenerator();

	private void assertEqualItems(String checkStr) {
		/* test on JsonValue */
		JsonValue parsedValue = parser.parse(checkStr).getValue();
		String generatedContext = generator.generate(parsedValue).getContext();
		JsonValue reparsedValue = parser.parse(generatedContext).getValue();
		assertEquals(parsedValue, reparsedValue);
	}
	private void assertEqualItems(String expected, String actual) {
		/* Round Trip test */
		JsonValue parsedValue = parser.parse(actual).getValue();
		String generatedContext = generator.generate(parsedValue).getContext();
		assertEquals(expected, generatedContext);
	}

//	@Disabled
	@Test
	void testLiteral() {
		assertEqualItems("null");
		assertEqualItems("true");
		assertEqualItems("false");

		assertEqualItems("\t null\n");
		assertEqualItems("true\n");
		assertEqualItems("   \tfalse");
	}

//	@Disabled
	@Test
	void testNumber() {
		assertEqualItems("0");
		assertEqualItems("-0"); /* CAUTION for signed zeros! */
		assertEqualItems("-0.0"); /* CAUTION for signed zeros! */
		assertEqualItems("12");
		assertEqualItems("-2");
		assertEqualItems("0.5");
		assertEqualItems("-12.3");
		assertEqualItems("1e7");
		assertEqualItems("0e-100");
		assertEqualItems("-5E-2");
		assertEqualItems("0.53e-5");
		assertEqualItems("-12.53e+3");
		assertEqualItems("1e-10000"); /* underflow */
		assertEqualItems("4.9406564584124654e-324"); /* min subnormal positive double */
		assertEqualItems("-4.9406564584124654e-324"); 
		assertEqualItems("2.2250738585072009e-308"); /* max subnormal double */
		assertEqualItems("-2.2250738585072009e-308"); 
		assertEqualItems("2.2250738585072014e-308"); /* min normal double */
		assertEqualItems("-2.2250738585072014e-308"); 
		assertEqualItems("1.7976931348623157e308"); /* max double */
		assertEqualItems("-1.7976931348623157e308");
		assertEqualItems("1.0000000000000002"); /* min number greater than 1 */
	}

//	@Disabled
	@Test
	void testString() {
		assertEqualItems("\"\""); // "" in JSON context;
		assertEqualItems("\"f3al se\""); // "f3al se" in JSON context;
		assertEqualItems("\"h\\/a\\\\pp\\ny\""); // "h\/a\\pp\ny" in JSON context;
		assertEqualItems("\"\\\\\\n\""); // "\\\n" in JSON context;
		assertEqualItems("\"\\u0010\""); 
		assertEqualItems("\"\\\\\"", "\"\\u005C\""); 
		assertEqualItems("\"\\\"\"", "\"\\u0022\""); 
		assertEqualItems("\"\uD834\uDD1E\"", "\"\\uD834\\uDD1E\""); /* G clef sign U+1D11E */
		assertEqualItems("\"\uD834\uDD1E\"", "\"\\ud834\\udd1e\""); 
	    assertEqualItems("\"Hello\\u0000World\"");
	    assertEqualItems("\"\u0024\"", "\"\\u0024\""); /* Dollar sign U+0024 */
	    assertEqualItems("\"\u00A2\"", "\"\\u00A2\""); /* Cents sign U+00A2 */
	    assertEqualItems("\"\u20AC\"", "\"\\u20AC\""); /* Euro sign U+20AC */
	}

//	@Disabled
	@Test
	void testArray() {
		assertEqualItems("[]");
		assertEqualItems("[null,true,false,\"hello\",[[],[null,false]]]");
		assertEqualItems("[null,{\"1\":true,\"2\":{}}]");
	}
	
//	@Disabled
	@Test
	void testObject() {
		assertEqualItems("{}");
		assertEqualItems("{\"1\":true,\"2\":{},\"3\":[false,null]}");
	}
}
