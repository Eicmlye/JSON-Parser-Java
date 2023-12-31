package pers.ericmonlye.jsonparser.parser;

import java.lang.IndexOutOfBoundsException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import pers.ericmonlye.jsonparser.exception.*;

/**
 * 
 */
public class JsonParser {
	private String context;
	private JsonValue value;
	private int cur;
	
	/* constructors */
	public JsonParser() {
		this.context = "";
		this.value = new JsonValue(JsonType.NULL);
		this.cur = 0;
	}
	public JsonParser(String context) {
		this.context = context;
		this.value = new JsonValue(JsonType.NULL);
		this.cur = 0;
	}
	
	/* user APIs */
	public JsonValue getValue() {
		return this.value;
	}
	/**
	 * {@code parse()} is the user API for parsing JSON context.
	 * 
	 * @return JsonParser
	 * @throws ExpectValueException
	 * @throws MissingDelimiterException
	 * @throws RootNotSingularException
	 */
	public JsonParser parse()
			throws	ExpectValueException,
					MissingDelimiterException,
					RootNotSingularException
	{
		rewind();
		
		if (isEndOfContext()) {
			throw new ExpectValueException("Empty input. ");
		}
		
		parseWhitespace();

		if (isEndOfContext()) {
			throw new ExpectValueException("The input context contains only whitespace. ");
		}
		
		parseJson();
		
		if (!isEndOfContext()) {
			if (!isCurWhitespace()) {
				throw new MissingDelimiterException("Context not in JSON format. "
								+ "Separate by whitespaces if more than 1 item is entered. ");
			}

			parseWhitespace();
		}
		
		if (!isEndOfContext()) {
			/* 
			 * Private parser functions should NOT deal with this exception.
			 * This exception can be made into an interface of a future multi-parser.
			 */
			throw new RootNotSingularException("Extra context detected after the first valid JSON. ");
		}
		
		return this;
	}
	public JsonParser parse(String context) {
		this.context = context;
		
		return parse();
	}

	/* cursor manipulation methods */
	private void rewind() {
		this.cur = 0;
		
		return;
	}
	private boolean isEndOfContext() {
		return cur >= context.length();
	}
	private char getCurChar() {
		assert !isEndOfContext();
		
		return context.charAt(cur);
	}

	/* booleans */
	private boolean isCurWhitespace() {
		assert !isEndOfContext();
		
		char ch = getCurChar();
		
		return (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r');
	}
	private boolean isCurDigit() {
		assert !isEndOfContext();
		
		char ch = getCurChar();
		
		return (ch >= '0' && ch <= '9');
	}

	/* tool parsers */
	private JsonParser parseWhitespace() {
		while (!isEndOfContext() && isCurWhitespace()) {
			++cur;
		}
		
		return this;
	}
	private JsonParser parseDigitComponent() {
		while (!isEndOfContext() && isCurDigit()) {
			++cur;
		}
		
		return this;
	}
	private JsonParser parseExponentComponent()
			throws IncompleteItemException
	{
		assert getCurChar() == 'e' || getCurChar() == 'E';
		++cur;
		
		if (!isEndOfContext() && (getCurChar() == '+' || getCurChar() == '-')) {
			++cur;
		}
		else if (isEndOfContext()) {
			throw new IncompleteItemException("Incomplete exponential part. ");
		}
		
		if (!isEndOfContext() && isCurDigit()) {
			parseDigitComponent();
		}
		else {
			throw new IncompleteItemException("Incomplete exponential part. ");
		}
		
		return this;
	}
	private JsonParser parseFractionalComponent()
			throws IncompleteItemException
	{
		assert getCurChar() == '.';
		++cur;
		
		if (!isEndOfContext() && isCurDigit()) {
			parseDigitComponent();
		}
		else {
			throw new IncompleteItemException("Incomplete fractional part. ");
		}
		
		return this;
	}
	private String parseCodepoint(long codepoint)
			throws InvalidCharacterException 
	{
		String result = "";
		
		if (codepoint <= 0x7F) {
			byte[] byteArray = {(byte)codepoint};
			
			result = new String(byteArray, 0, 1, StandardCharsets.UTF_8);
		}
		else if (codepoint <= 0x7FF) {
			byte[] byteArray = {
					(byte)(0xC0 | ((codepoint >> 6) & 0xFF)),
					(byte)(0x80 | (codepoint & 0x3F))
				};
			
			result = new String(byteArray, 0, 2, StandardCharsets.UTF_8);
		}
		else if (codepoint <= 0xFFFF) {
			byte[] byteArray = {
					(byte)(0xE0 | ((codepoint >> 12) & 0xFF)),
					(byte)(0x80 | ((codepoint >> 6) & 0x3F)),
					(byte)(0x80 | (codepoint & 0x3F))
				};
			
			result = new String(byteArray, 0, 3, StandardCharsets.UTF_8);
		}
		else if (codepoint <= 0x10FFFF) {
			byte[] byteArray = {
					(byte)(0xF0 | ((codepoint >> 18) & 0xFF)),
					(byte)(0x80 | ((codepoint >> 12) & 0x3F)),
					(byte)(0x80 | ((codepoint >> 6) & 0x3F)),
					(byte)(0x80 | (codepoint & 0x3F))
				};

			result = new String(byteArray, 0, 4, StandardCharsets.UTF_8);
		}
		else {
			throw new InvalidCharacterException("Invalid Unicode codepoint. ");
		}
		
		return result;
	}
	private String parseUChar()
			throws InvalidCharacterException, IncompleteItemException 
	{
		assert !isEndOfContext();
		assert getCurChar() == 'u';
		
		String result = "";
		
		/* May throw NumberFormatException */
		long highSurrogate = 0L;
		try {
			highSurrogate = (long)Integer.parseInt(context.substring(cur + 1, cur + 5), 16);
		}
		catch (IndexOutOfBoundsException | NumberFormatException e) {
			throw new IncompleteItemException("Incomplete escape character. "
								+ "Should only use hex-digits and exactly 4 digits are allowed.");
		}
		long codepoint = 0L;
		
		if (highSurrogate >= 0xD800 && highSurrogate <= 0xDBFF) {
			if (cur + 7 >= context.length() || !context.substring(cur + 5, cur + 7).equals("\\u")) {
				throw new InvalidCharacterException("Missing low surrogate. ");
			}
			
			/* May throw NumberFormatException */
			long lowSurrogate = 0L;
			try {
				lowSurrogate = (long)Integer.parseInt(context.substring(cur + 7, cur + 11), 16);
			}
			catch (IndexOutOfBoundsException | NumberFormatException e) {
				throw new IncompleteItemException("Incomplete escape character. "
									+ "Should only use hex-digits and exactly 4 digits are allowed.");
			}
			
			if (!(lowSurrogate >= 0xDC00 && lowSurrogate <= 0xDFFF)) {
				throw new InvalidCharacterException("Low surrogate should be in range 0xDC00 - 0xDFFF. ");
			}
			codepoint = 0x10000 + (highSurrogate - 0xD800) * 0x400 + (lowSurrogate - 0xDC00);

			/* 
			 * Cursor move to the last hex digit. 
			 * {@code parseEscapeChar()} will move it 1 char further. 
			 */ 
			cur += 10; 
		}
		else {
			codepoint = (long)highSurrogate;

			/* 
			 * Cursor move to the last hex digit. 
			 * {@code parseEscapeChar()} will move it 1 char further. 
			 */ 
			cur += 4;
		}
		
		result = parseCodepoint(codepoint);
		
		return result;
	}
	private String parseEscapeChar() 
			throws IncompleteItemException
	{
		/* 
		 * Only \u0000 - \u001F, \u0022 ('\"') and \u005C ('\\')
		 * MUST be written in escape char. 
		 * The other char in \u0020 - \u10FFFF are all legal in JSON string
		 * but still CAN be written in escape char. 
		 */
		assert !isEndOfContext();
		assert getCurChar() == '\\';
		++cur;
		
		String result = "";
		char ch = getCurChar();
		
		if (isEndOfContext() || ch == '\"') {
			throw new IncompleteItemException("Incomplete escape character. ");
		}
		
		result = switch (ch) {
		case '\"' -> "\"";
		case '\\' -> "\\";
		case '/' -> "/";
		case 'b' -> "\b";
		case 'f' -> "\f";
		case 'n' -> "\n";
		case 'r' -> "\r";
		case 't' -> "\t";
		case 'u' -> parseUChar();
		default -> {
			throw new InvalidCharacterException("Unknown escape character. ");
		}
		};
		
		++cur;
		
		return result;
	}
	private String parseNormalChar()
			throws InvalidCharacterException 
	{
		assert !isEndOfContext();
		
		String result = "";
		
		char ch = getCurChar();
		if ((ch >= '\u0000' && ch <= '\u001F') || ch == '\"' || ch == '\\') {
			throw new InvalidCharacterException("Illegal character detected. "
							+ "\\u0000 - \\u001F, the backslash and quotation mark "
							+ "should be entered as escape char.");
		}

		result += ch;
		
		++cur;
		
		return result;
		
	}
	private String parseRawString() 
			throws IncompleteItemException, InvalidCharacterException, MissingEndTagException
	{
		assert !isEndOfContext();
		assert getCurChar() == '\"';
		++cur;
		
		String result = "";
		
		for (char ch = '\0'; !isEndOfContext() && (ch = getCurChar()) != '\"'; /* ++cur is done by the subparsers */) {
			result += switch (ch) {
			case '\\' -> parseEscapeChar();
			default -> parseNormalChar();
			};
		}
		
		if (isEndOfContext()) {
			throw new MissingEndTagException("Missing closing quotation mark. ");
		}

		++cur; /* skip ending '\"' */
		
		return result;
	}
	private void parseObjectMember(HashMap<String, JsonValue> result)
			throws InvalidObjectException,
					MissingDelimiterException, 
					IncompleteItemException, 
					InvalidCharacterException, 
					MissingEndTagException
	{
		assert !isEndOfContext();
		assert getCurChar() == '\"';
		
		String key = parseRawString();
		if (result.containsKey(key)) {
			throw new InvalidObjectException("Duplicate key items found. ");
		}
		
		parseWhitespace();
		if (isEndOfContext()) { // {"key"\t
			throw new InvalidObjectException("A key requires a corresponding value seqarated by colon. ");
		}
		if (getCurChar() != ':') { // {"key"123
			throw new MissingDelimiterException("Missing colon. ");
		}
		
		++cur; /* skip colon */
		
		parseWhitespace();
		if (isEndOfContext()) { // {"key":\t
			throw new InvalidObjectException("A key requires a corresponding value. ");
		}
		
		JsonValue keyValue = parseJson().getValue();
		
		result.put(key, (JsonValue)keyValue.clone());
		
		return;
	}
	
	/* JSON item parsers */
	/**
	 * {@code parseNull()} parses {@code JsonType.NULL}, which in JSON
	 * context is literally "null".
	 * 
	 * @return JsonParser
	 * @throws InvalidLiteralException
	 * @throws IncompleteItemException
	 */
	private JsonParser parseNull()
			throws InvalidLiteralException, IncompleteItemException
	{
		assert !isEndOfContext();
		assert getCurChar() == 'n';
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + 4}
		 *  is greater than {@code length()} of this string.
		 */
		String item = "";
		try {
			item = context.substring(cur, cur + 4);
		}
		catch (IndexOutOfBoundsException e) {
			throw new IncompleteItemException("The \"null\" item is incomplete. ");
		}
		
		if (!item.equals("null")) {
			throw new InvalidLiteralException("Misspelling \"null\" as \""
					+ item + "\". ");
		}
		
		value.setValue();
		cur += 4;
		
		return this;
	}
	/**
	 * {@code parseTrue()} parses {@code JsonType.TRUE}, which in JSON
	 * context is literally "true".
	 * 
	 * @return JsonParser
	 * @throws InvalidLiteralException
	 * @throws IncompleteItemException
	 */
	private JsonParser parseTrue()
			throws InvalidLiteralException, IncompleteItemException
	{
		assert !isEndOfContext();
		assert getCurChar() == 't';
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + 4}
		 *  is greater than {@code length()} of this string.
		 */
		String item = "";
		try {
			item = context.substring(cur, cur + 4);
		}
		catch (IndexOutOfBoundsException e) {
			throw new IncompleteItemException("The \"true\" item is incomplete. ");
		}
		
		if (!item.equals("true")) {
			throw new InvalidLiteralException("Misspelling \"true\" as \""
						+ item + "\". ");
		}
		
		value.setValue(true);
		cur += 4;
		
		return this;
	}
	/**
	 * {@code parseFalse()} parses {@code JsonType.FALSE}, which in JSON
	 * context is literally "false".
	 * 
	 * @return JsonParser
	 * @throws InvalidLiteralException
	 * @throws IncompleteItemException
	 */
	private JsonParser parseFalse()
			throws InvalidLiteralException, IncompleteItemException
	{
		assert !isEndOfContext();
		assert getCurChar() == 'f';
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + 5}
		 *  is greater than {@code length()} of this string.
		 */
		String item = "";
		try {
			item = context.substring(cur, cur + 5);
		}
		catch (IndexOutOfBoundsException e) {
			throw new IncompleteItemException("The \"false\" item is incomplete. ");
		}
		
		if (!item.equals("false")) {
			throw new InvalidLiteralException("Misspelling \"false\" as \""
					+ item + "\". ");
		}
		
		value.setValue(false);
		cur += 5;
		
		return this;
	}
	/**
	 * {@code parseLiteral(JsonType)} can deal with {@code JsonType.NULL}, 
	 * {@code JsonType.TRUE} and {@code JsonType.FALSE}. 
	 * 
	 * @param type
	 * @return JsonParser
	 * @throws InvalidLiteralException
	 * @throws IncompleteItemException
	 */
	@SuppressWarnings("unused")
	private JsonParser parseLiteral(JsonType type) 
			throws InvalidLiteralException, IncompleteItemException
	{
		assert !isEndOfContext();
		String typeName = type.toString().toLowerCase();
		int len = typeName.length();
		
		assert getCurChar() == typeName.charAt(0);
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + len}
		 *  is greater than {@code length()} of this string.
		 */
		String item = "";
		try {
			item = context.substring(cur, cur + len);
		}
		catch (IndexOutOfBoundsException e) {
			throw new IncompleteItemException("The \"" + typeName + "\" item is incomplete. ");
		}
		
		if (!item.equals(typeName)) {
			throw new InvalidLiteralException("Misspelling \"" + typeName + "\" as \""
					+ item + "\". ");
		}
		
		value.clear();
		value.setType(type);
		cur += len;
		
		return this;
	}
	/**
	 * {@code parseNumber()} parses {@code JsonType.NUMBER}. 
	 * 
	 * @return JsonParser
	 * @throws IncompleteItemException
	 */
	private JsonParser parseNumber()
			throws IncompleteItemException
	{
		assert !isEndOfContext();
		assert getCurChar() == '-' || isCurDigit();
		
		int tmpCur = cur;
		
		if (getCurChar() == '-') {
			++cur;
		}
		
		if (!isEndOfContext() && getCurChar() >= '1' && getCurChar() <= '9') {
			parseDigitComponent();
		}
		else if (!isEndOfContext() && getCurChar() == '0') {
			++cur;
		}
		else { /* "-abcd", "-.3", "-"; positive numbers will never come here. */
			throw new IncompleteItemException("There is no digit following the negative sign. ");
		}
		
		if (!isEndOfContext() && getCurChar() == '.') {
			parseFractionalComponent();
		}
		
		if (!isEndOfContext() && (getCurChar() == 'e' || getCurChar() == 'E')) {
			parseExponentComponent();
		}
		
		value.setValue(Double.parseDouble(context.substring(tmpCur, cur)));
		
		/* 
		 * -0.0 is less than 0.0 in Java Double type, which is not the case
		 * in JSON. Here we change signed 0s to positive 0s.
		 */
		double cache = value.getNum();
		
		if (cache == -0.0) {
			value.setNum(0.0);
		}

		if (cache == Double.POSITIVE_INFINITY || cache == Double.NEGATIVE_INFINITY) {
			throw new InvalidNumberException("The absolute value is too large. ");
		}
		
		return this;
	}
	/**
	 * {@code parseString()} parses {@code JsonType.STRING}. 
	 * 
	 * @return JsonParser
	 * @throws IncompleteItemException
	 * @throws InvalidCharacterException
	 * @throws MissingEndTagException
	 */
	private JsonParser parseString()
			throws IncompleteItemException, InvalidCharacterException, MissingEndTagException
	{
		/* 
		 * Only \u0000 - \u001F, \u0022 ('\"') and \u005C ('\\')
		 * MUST be written in escape char. 
		 * The other char in \u0020 - \u10FFFF are all legal in JSON string
		 * but still CAN be written in escape char. 
		 */
		assert !isEndOfContext();
		assert getCurChar() == '\"';
		
		String result = parseRawString();
		
		value.setValue(result);
		
		return this;
	}
	/**
	 * {@code parseArray()} parses {@code JsonType.ARRAY}. 
	 * 
	 * @return JsonParser
	 * @throws MissingEndTagException
	 * @throws InvalidValueException
	 * @throws ExpectValueException
	 * @throws IndexOutOfBoundsException
	 * @throws InvalidArrayException
	 */
	private JsonParser parseArray() 
			throws MissingEndTagException,
					InvalidArrayException, 
					InvalidValueException, 
					ExpectValueException
	{
		assert !isEndOfContext();
		assert getCurChar() == '[';
		
		ArrayList<JsonValue> result = new ArrayList<JsonValue>();
		char ch = '\0';
		
		do {
			++cur; /* skip comma or '[' */
			
			parseWhitespace();
			if (isEndOfContext()) {
				if (result.size() == 0) { // [\t
					throw new MissingEndTagException("Missing ending bracket. ");
				}
				
				// [1,2,\t
				throw new InvalidArrayException("JSON dose not allow ending commas. ");
			}
			ch = getCurChar(); /* This line is necessary for the empty array [] to pass the test.
			 					* Empty array will invoke {@code break}, and the {@code while}
			 					* statement will not be triggered, so we must {@code getCurChar()}
			 					* here before the empty array goes out of {@code do...while}. */
			if (ch == ']') {
				if (result.size() != 0) { // [1,2,]
					throw new InvalidArrayException("JSON dose not allow ending commas. ");
				}
				
				break; // Empty array [].
			}
			
			result.add((JsonValue)(parseJson().getValue().clone()));
			
			parseWhitespace();
			if (isEndOfContext()) { // [1,2,3\t
				throw new MissingEndTagException("Missing ending bracket. ");
			}
		}
		while ((ch = getCurChar()) == ',');
		
		if (ch != ']' ) { // [1,2 \t 3]
			throw new InvalidArrayException("Missing comma. ");
		}
		
		++cur;
		
		value.setValue(result);
		
		return this;
	}
	/**
	 * {@code parseObject()} parses {@code JsonType.OBJECT}.
	 * 
	 * @return JsonValue
	 * @throws MissingEndTagException
	 * @throws InvalidObjectException
	 * @throws InvalidValueException
	 * @throws ExpectValueException
	 */
	private JsonParser parseObject()
			throws MissingEndTagException,
					InvalidObjectException, 
					InvalidValueException, 
					ExpectValueException
	{ /* {@code parseObjectMember()} ensures that the keys are distinct from each other. */
		assert !isEndOfContext();
		assert getCurChar() == '{';
		
		HashMap<String, JsonValue> result = new HashMap<String, JsonValue>();
		char ch = '\0';
		
		do {
			++cur; /* skip comma or '{' */
			
			parseWhitespace();
			if (isEndOfContext()) {
				if (result.size() == 0) { // {\t
					throw new MissingEndTagException("Missing ending brace. ");
				}
				
				// {"key":123,\t
				throw new InvalidObjectException("JSON dose not allow ending commas. ");
			}
			ch = getCurChar();/* This line is necessary for the empty object {} to pass the test.
							   * Empty object will invoke {@code break}, and the {@code while}
							   * statement will not be triggered, so we must {@code getCurChar()}
							   * here before the empty object goes out of {@code do...while}. */
			if (ch == '}') {
				if (result.size() != 0) { // {"key":123,\t}
					throw new InvalidObjectException("JSON dose not allow ending commas. ");
				}
				
				break; // Empty object {}.
			}
			if (ch != '\"') { // {123:456}
				throw new InvalidObjectException("Object key must be a string surrounded by quotation marks. ");
			}
			
			parseObjectMember(result);
			
			parseWhitespace();
			if (isEndOfContext()) { // {"key":123
				throw new MissingEndTagException("Missing ending brace. ");
			}
		}
		while ((ch = getCurChar()) == ',');
		
		if (ch != '}' ) { // {"key":123\t "key2":456}
			throw new InvalidObjectException("Missing comma. ");
		}
		
		++cur;
		
		value.setValue(result);
		
		return this;
	}
	/**
	 * {@code parseJson()} parses a single JSON format string into JsonValue. 
	 * 
	 * @return JsonParser
	 * @throws InvalidValueException
	 * @throws ExpectValueException
	 */
	private JsonParser parseJson()
			throws InvalidValueException, ExpectValueException
	{
		assert !isEndOfContext();
		
		switch (getCurChar()) {
		case 'n':
			parseNull();
			break;
		case 't':
			parseTrue();
			break;
		case 'f':
			parseFalse();
			break;
		case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
			parseNumber();
			break;
		case '\"':
			parseString();
			break;
		case '[':
			parseArray();
			break;
		case '{':
			parseObject();
			break;
		default:
			throw new InvalidValueException("Invalid JSON context. ");
		}
		
		return this;
	}
}
