package jsonparser.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

//import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jsonparser.exception.*;

class JsonParserTest {
	JsonParser parser = new JsonParser();

	private void assertEqualItems(JsonType expected, String actual) {
		assertEquals(expected, parser.parse(actual).getValue().getType());
	}
	private void assertEqualItems(double expected, String actual) {
		assertEqualItems(JsonType.NUMBER, actual);
		assertEquals(expected, parser.getValue().getNum());
	}
	private void assertEqualItems(String expected, String actual) {
		assertEqualItems(JsonType.STRING, actual);
		assertEquals(expected, parser.getValue().getStr());
	}
	private void assertEqualItems(ArrayList<JsonValue> expected, String actual) {
		assertEqualItems(JsonType.ARRAY, actual);
		ArrayList<JsonValue> actualArr = parser.getValue().getArr();
		assertEquals(expected, actualArr);
		expected.clear();
	}
	private void assertEqualItems(HashMap<String, JsonValue> expected, String actual) {
		assertEqualItems(JsonType.OBJECT, actual);
		/* 
		 * Here we use JsonValue to compare instead of HashMap
		 * to solve the problem brought by Java object pointers.
		 * String variables in Java are pointers, not concrete
		 * objects, and HashMap compares the equivalence by mapping
		 * the same object and check the equivalence of values.
		 * Different pointers pointing to the same value are considered
		 * different keys, so the JUnit test will never pass if we
		 * use HashMap to do the tests. 
		 * See AbstractMap.equals() for more details. 
		 */
		JsonValue expectedJson = new JsonValue(expected);
		assertEquals(expectedJson, parser.getValue());
		expected.clear();
	}
	
	private void alAdd(ArrayList<JsonValue> arr, JsonType type) {
		assert type == JsonType.NULL || type == JsonType.TRUE || type == JsonType.FALSE;
		JsonValue item = new JsonValue(type);
		
		arr.add(item);
	}
	private void alAdd(ArrayList<JsonValue> arr, double num) {
		JsonValue item = new JsonValue(num);
		
		arr.add(item);
	}
	private void alAdd(ArrayList<JsonValue> arr, String str) {
		JsonValue item = new JsonValue(str);
		
		arr.add(item);
	}
	private void alAdd(ArrayList<JsonValue> arr, ArrayList<JsonValue> contents) {
		JsonValue item = new JsonValue(contents);
		
		arr.add(item);
	}
	private void alAdd(ArrayList<JsonValue> arr, HashMap<String, JsonValue> obj) {
		JsonValue item = new JsonValue(obj);
		
		arr.add(item);
	}
	private void hmAdd(HashMap<String, JsonValue> obj, String key, JsonType type) {
		assert type == JsonType.NULL || type == JsonType.TRUE || type == JsonType.FALSE;
		JsonValue keyValue = new JsonValue(type);
		
		obj.put(key, keyValue);
	}
	private void hmAdd(HashMap<String, JsonValue> obj, String key, double num) {
		JsonValue keyValue = new JsonValue(num);
		
		obj.put(key, keyValue);
	}
	private void hmAdd(HashMap<String, JsonValue> obj, String key, String str) {
		JsonValue keyValue = new JsonValue(str);
		
		obj.put(key, keyValue);
	}
	private void hmAdd(HashMap<String, JsonValue> obj, String key, ArrayList<JsonValue> arr) {
		JsonValue keyValue = new JsonValue(arr);
		
		obj.put(key, keyValue);
	}
	private void hmAdd(HashMap<String, JsonValue> obj, String key, HashMap<String, JsonValue> contents) {
		JsonValue keyValue = new JsonValue(contents);
		
		obj.put(key, keyValue);
	}

	@Test
	void test() {
		JsonValue val = new JsonValue("hi");
		val.setValue(true);
		System.out.println(val.getValue());
	}
	
//	@Disabled
	@Test
	void testNull() {
		assertEqualItems(JsonType.NULL, "null");
		assertEqualItems(JsonType.NULL, " null");
		assertEqualItems(JsonType.NULL, " \t  \n\rnull");
		assertEqualItems(JsonType.NULL, " null\t");
	}

//	@Disabled
	@Test
	void testTrue() {
		assertEqualItems(JsonType.TRUE, "true");
		assertEqualItems(JsonType.TRUE, " true");
		assertEqualItems(JsonType.TRUE, " \t  \n\rtrue");
		assertEqualItems(JsonType.TRUE, " true\t");
	}

//	@Disabled
	@Test
	void testFalse() {
		assertEqualItems(JsonType.FALSE, "false");
		assertEqualItems(JsonType.FALSE, " false");
		assertEqualItems(JsonType.FALSE, " \t  \n\rfalse");
		assertEqualItems(JsonType.FALSE, " false\t");
	}
	
//	@Disabled
	@Test
	void testNumber() {
		assertEqualItems(0.0, 						"0");
		assertEqualItems(0.0, 						"-0"); /* CAUTION for signed zeros! */
		assertEqualItems(0.0, 						"-0.0"); /* CAUTION for signed zeros! */
		assertEqualItems(12, 						"12");
		assertEqualItems(-2, 						"-2");
		assertEqualItems(0.5, 						"0.5");
		assertEqualItems(-12.3, 					"-12.3");
		assertEqualItems(1e7, 						"1e7");
		assertEqualItems(0e-100, 					"0e-100");
		assertEqualItems(-5E-2, 					"-5E-2");
		assertEqualItems(0.53e-5, 					"0.53e-5");
		assertEqualItems(-12.53e+3, 				"-12.53e+3");
		assertEqualItems(0.0, 						"1e-10000"); /* underflow */
		assertEqualItems(4.9406564584124654e-324, 	"4.9406564584124654e-324"); /* min subnormal positive double */
		assertEqualItems(-4.9406564584124654e-324, 	"-4.9406564584124654e-324"); 
		assertEqualItems(2.2250738585072009e-308, 	"2.2250738585072009e-308"); /* max subnormal double */
		assertEqualItems(-2.2250738585072009e-308, 	"-2.2250738585072009e-308"); 
		assertEqualItems(2.2250738585072014e-308, 	"2.2250738585072014e-308"); /* min normal double */
		assertEqualItems(-2.2250738585072014e-308, 	"-2.2250738585072014e-308"); 
		assertEqualItems(1.7976931348623157e308, 	"1.7976931348623157e308"); /* max double */
		assertEqualItems(-1.7976931348623157e308, 	"-1.7976931348623157e308");
		assertEqualItems(1.0000000000000002, 		"1.0000000000000002"); /* min number greater than 1 */
	}

//	@Disabled
	@Test
	void testString() {
		assertEqualItems("", 						"\"\""); // "" in JSON context;
		assertEqualItems("f3al se", 				"\"f3al se\""); // "f3al se" in JSON context;
		assertEqualItems("h/a\\pp\ny", 				"\"h\\/a\\\\pp\\ny\""); // "h\/a\\pp\ny" in JSON context;
		assertEqualItems("\\\n", 					"\"\\\\\\n\""); // "\\\n" in JSON context;
		assertEqualItems("\u0010", 					"\"\\u0010\""); 
		assertEqualItems("\\", 						"\"\\u005C\""); 
		assertEqualItems("\"", 						"\"\\u0022\""); 
		assertEqualItems("\uD834\uDD1E", 			"\"\\uD834\\uDD1E\""); /* G clef sign U+1D11E */
		assertEqualItems("\uD834\uDD1E", 			"\"\uD834\uDD1E\"");
		assertEqualItems("\uD834\uDD1E", 			"\"\\ud834\\udd1e\""); 
	    assertEqualItems("Hello\0World", 			"\"Hello\\u0000World\"");
	    assertEqualItems("\u0024", 					"\"\\u0024\""); /* Dollar sign U+0024 */
	    assertEqualItems("\u00A2", 					"\"\\u00A2\""); /* Cents sign U+00A2 */
	    assertEqualItems("\u20AC", 					"\"\\u20AC\""); /* Euro sign U+20AC */
	}

//	@Disabled
	@Test
	void testArray() {
		ArrayList<JsonValue> cache = new ArrayList<JsonValue>();
		assertEqualItems(cache, "[]");
		assertEqualItems(cache, "\r  [   \t \n  ]");

		alAdd(cache, JsonType.NULL);
		assertEqualItems(cache, "[null]");
		alAdd(cache, JsonType.NULL);
		assertEqualItems(cache, "  [\n null]");
		alAdd(cache, JsonType.NULL);
		assertEqualItems(cache, "[ null \r  \t]");

		alAdd(cache, JsonType.NULL);
		alAdd(cache, JsonType.TRUE);
		alAdd(cache, JsonType.FALSE);
		alAdd(cache, 1.03);
		alAdd(cache, "hello");
		assertEqualItems(cache, "[null,true,false,1.03,\"hello\"]");

		ArrayList<JsonValue> tmp = new ArrayList<JsonValue>();
		ArrayList<JsonValue> lst = new ArrayList<JsonValue>();
		alAdd(lst, JsonType.TRUE);
		alAdd(tmp, "9d0sf \ti\n");
		alAdd(tmp, lst);
		lst = new ArrayList<JsonValue>();
		alAdd(lst, tmp);
		tmp = new ArrayList<JsonValue>();
		alAdd(cache, tmp);
		tmp = new ArrayList<JsonValue>();
		alAdd(tmp, JsonType.NULL);
		alAdd(tmp, 2);
		alAdd(cache, tmp);
		alAdd(cache, lst);
		assertEqualItems(cache, "[[], [null,2], [[\"9d0sf \\ti\\n\", [true ]\t]  ]\n ]");
	}
	
//	@Disabled
	@Test
	void testObject() {
		HashMap<String, JsonValue> cache = new HashMap<String, JsonValue>();
		assertEqualItems(cache, "{}");
		
		hmAdd(cache, "hello", JsonType.NULL);
		assertEqualItems(cache, "{\"hello\":null}");
		
		hmAdd(cache, "key\uD834\uDD1E", 1);
		assertEqualItems(cache, "{\"key\\uD834\\uDD1E\":1}");
		
		ArrayList<JsonValue> tmp = new ArrayList<JsonValue>();
		ArrayList<JsonValue> lst = new ArrayList<JsonValue>();
		HashMap<String, JsonValue> hmp = new HashMap<String, JsonValue>();
		hmAdd(cache, "1", JsonType.NULL);
		hmAdd(cache, "2", JsonType.TRUE);
		hmAdd(cache, "3", JsonType.FALSE);
		hmAdd(cache, "4", 1.03e-4);
		hmAdd(cache, "5", "hello");
		alAdd(tmp, JsonType.NULL);
		alAdd(tmp, JsonType.TRUE);
		alAdd(tmp, JsonType.FALSE);
		alAdd(tmp, 1.03e-4);
		alAdd(tmp, "fine");
		alAdd(tmp, lst);
		alAdd(tmp, hmp);
		hmAdd(cache, "6", tmp);
		assertEqualItems(cache, "{\"1\":null,"
							+ "\"2\":true,"
							+ "\"3\":false,"
							+ "\"4\":1.03e-4,"
							+ "\"5\":\"hello\","
							+ "\"6\":[null, true, false, 1.03e-4,\"fine\",[], {}]}");
		
		tmp = new ArrayList<JsonValue>();
		hmp = new HashMap<String, JsonValue>();
		hmAdd(hmp, "subkey", 123);
		hmAdd(cache, "key", hmp);
		hmAdd(cache, "seckey", tmp);
		assertEqualItems(cache, "{\"key\":{\"subkey\":123}, \"seckey\":[]}");
	}
	
//	@Disabled
	@Test
	void testExpectValueException() {
		assertThrows(ExpectValueException.class, () -> {
			parser.parse("");
		});
		assertThrows(ExpectValueException.class, () -> {
			parser.parse("   \t \n\r");
		});
	}

//	@Disabled
	@Test
	void testInvalidValueException() {
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("abcde");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("aull");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse(" \t  \n\raull");
		});
		assertThrows(InvalidValueException.class, () -> {
			parser.parse("[aull]");
		});
		
		assertThrows(InvalidLiteralException.class, () -> {
			parser.parse(" nulk");
		});
		assertThrows(InvalidLiteralException.class, () -> {
			parser.parse("ture");
		});
		assertThrows(InvalidLiteralException.class, () -> {
			parser.parse("[ null, ture]");
		});
		
		assertThrows(InvalidNumberException.class, () -> {
			parser.parse("1e309");
		});
		assertThrows(InvalidNumberException.class, () -> {
			parser.parse("-1e309");
		});
		assertThrows(InvalidNumberException.class, () -> {
			parser.parse("-0.5e2110.6");
		});

		assertThrows(InvalidCharacterException.class, () -> {
			parser.parse("\"\\a\"");
		});
		assertThrows(InvalidCharacterException.class, () -> {
			parser.parse("\"\\uD834\"");
		});
		assertThrows(InvalidCharacterException.class, () -> {
			parser.parse("\"\\uD834\\\"");
		});

		assertThrows(InvalidArrayException.class, () -> {
			parser.parse("[1,]");
		});
		assertThrows(InvalidArrayException.class, () -> {
			parser.parse("[1,2,");
		});
		assertThrows(InvalidArrayException.class, () -> {
			parser.parse("[1,2\t3]");
		});
		assertThrows(InvalidArrayException.class, () -> {
			parser.parse("[1\t2");
		});
		assertThrows(InvalidArrayException.class, () -> {
			parser.parse("[1, 2, [3,], 4]");
		});
		assertThrows(InvalidArrayException.class, () -> {
			parser.parse("{\"key\": [3,], \"key2\":4}");
		});

		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{\"\"");
		});
		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{\"key\":");
		});
		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{\"key\":123,\t");
		});
		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{\"key\":123,\t}");
		});
		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{123:\"key\",\t}");
		});
		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{\"key\":123\t \"key2\":456}");
		});
		assertThrows(InvalidObjectException.class, () -> {
			parser.parse("{\"key\":123,\"key\":456}");
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
	void testIncompleteItemException() {
		/* Literals */
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("nul");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse(" \n  \tn");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("tr");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("fale");
		});

		/* Numbers */
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-a");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-.3");
		});

		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-0.");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-0.e2");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-0. abcd");
		});

		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-1E");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("-9e-");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("40e+");
		});

		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uG000\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u0G00\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u00G0\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u000G\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u0\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u0F\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u0F4\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\u0F4");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\u\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uD\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uDD\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uDD1\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uDD1");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uGD1E\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uDG1E\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uDDGE\"");
		});
		assertThrows(IncompleteItemException.class, () -> {
			parser.parse("\"\\uD834\\uDD1G\"");
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
			parser.parse("012");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("-0.50.6");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("-12.34e5null");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("\"\"null");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("\"he\\nll\\\\o\"null");
		});

		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("{\"key\"123");
		});
		assertThrows(MissingDelimiterException.class, () -> {
			parser.parse("[null, {\"\", 123]");
		});
	}
	
//	@Disabled
	@Test
	void testMissingEndTagException() {
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("\"");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("\"ab 40.\\u006F(dafr");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("\"\\u0015");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("\"\\uD834\\uDD1E");
		});

		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("[");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("\n[\t");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("[ 1 , 2 , 3");
		});

		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("{");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("\n{\t");
		});
		assertThrows(MissingEndTagException.class, () -> {
			parser.parse("{\"key\":123, \"\":\"value\"");
		});
	}
}
