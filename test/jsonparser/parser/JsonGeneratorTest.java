package jsonparser.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JsonGeneratorTest {
	JsonParser parser = new JsonParser();
	JsonGenerator generator = new JsonGenerator();
	
	private void assertEqualItems(String expected, String actual) {
		assertEquals(expected,generator.generate(parser.parse(actual).getValue()).getContext());
	}

//	@Disabled
	@Test
	void testLiteral() {
		assertEqualItems("null","null");
		assertEqualItems("true","true");
		assertEqualItems("false","false");

		assertEqualItems("null","\t null\n");
		assertEqualItems("true","true\n");
		assertEqualItems("false","   \tfalse");
	}

}
