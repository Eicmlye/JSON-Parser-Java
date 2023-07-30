package jsonparser.parser;

import java.lang.IndexOutOfBoundsException;
import java.nio.charset.StandardCharsets;

import jsonparser.exception.*;

/**
 * 
 */
public class JsonParser {
	private String context;
	private JsonValue value;
	private int cur;
	
	/* constructor */
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
	
	/* JavaBeans */
	public JsonValue getValue() {
		return this.value;
	}
	/* cursor manipulation functions */
	private void rewind() {
		this.cur = 0;
		
		return;
	}
	private boolean isEndOfContext() {
		return cur == context.length();
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
		int len = context.length();
		
		for (; cur < len; ++cur) {
			if (!isCurWhitespace()) {
				break;
			}
		}
		
		return this;
	}
	private JsonParser parseDigitComponent() {
		int len = context.length();
		
		for (; cur < len; ++cur) {
			if (!isCurDigit()) {
				break;
			}
		}
		
		return this;
	}
	private JsonParser parseExponentComponent()
			throws IncompleteNumberException
	{
		assert getCurChar() == 'e' || getCurChar() == 'E';
		++cur;
		
		if (!isEndOfContext() && (getCurChar() == '+' || getCurChar() == '-')) {
			++cur;
		}
		else if (isEndOfContext()) {
			throw new IncompleteNumberException("Incomplete exponential part. ");
		}
		
		if (!isEndOfContext() && isCurDigit()) {
			parseDigitComponent();
		}
		else {
			throw new IncompleteNumberException("Incomplete exponential part. ");
		}
		
		return this;
	}
	private JsonParser parseFractionalComponent()
			throws IncompleteNumberException
	{
		assert getCurChar() == '.';
		++cur;
		
		if (!isEndOfContext() && isCurDigit()) {
			parseDigitComponent();
		}
		else {
			throw new IncompleteNumberException("Incomplete fractional part. ");
		}
		
		return this;
	}
	private String parseEscapeChar() {
		assert !isEndOfContext();
		assert getCurChar() == '\\';
		++cur;
		
		String result = "";
		
		if (isEndOfContext()) {
			throw new InvalidValueException("Unknown escape character. ");
		}
		
		result = switch (getCurChar()) {
		case '\"' -> "\"";
		case '\\' -> "\\";
		case '/' -> "/";
		case 'b' -> "\b";
		case 'f' -> "\f";
		case 'n' -> "\n";
		case 'r' -> "\r";
		case 't' -> "\t";
		case 'u' -> parseUChar();
		/* No need to ++cur here, parseString() will do it later. */
		default -> {
			throw new InvalidValueException("Unknown escape character. ");
		}
		};
		
		return result;
	}
	private String parseNormalChar() {
		assert !isEndOfContext();
		
		String result = "";
		
		char ch = getCurChar();
		if ((ch >= '\u0000' && ch <= '\u001F') || ch == '\"' || ch == '\\') {
			throw new InvalidValueException("Unsupported character detected. ");
		}
		else {
			result += ch;
			/* No need to ++cur here, parseString() will do it later. */
		}
		
		return result;
		
	}
	private String parseCodepoint(long codepoint) {
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
			throw new InvalidValueException("Invalid Unicode codepoint. ");
		}
		
		return result;
	}
	private String parseUChar() {
		/* TODO: this function does not check if the surrogates are in proper range. */
		assert !isEndOfContext();
		assert getCurChar() == 'u';
		
		String result = "";
		
		/* May throw NumberFormatException */
		long highSurrogate = (long)Integer.parseInt(context.substring(cur + 1, cur + 5), 16);
		long codepoint = 0L;
		
		if (highSurrogate >= 0xD800 && highSurrogate <= 0xDBFF) {
			if (!context.substring(cur + 5, cur + 7).equals("\\u")) {
				throw new InvalidValueException("Missing low surrogate. ");
			}
			
			/* May throw NumberFormatException */
			long lowSurrogate = (long)Integer.parseInt(context.substring(cur + 7, cur + 11), 16);
			codepoint = 0x10000 + (highSurrogate - 0xD800) * 0x400 + (lowSurrogate - 0xDC00);

			/* 
			 * {@code cur} only need to increase to the last hex digit here,
			 * {@code parseString()} will make {@code cur} increase once more later.
			 */
			cur += 10;
		}
		else {
			codepoint = (long)highSurrogate;

			/* 
			 * {@code cur} only need to increase to the last hex digit here,
			 * {@code parseString()} will make {@code cur} increase once more later.
			 */
			cur += 4;
		}
		
		result = parseCodepoint(codepoint);
		
		return result;
	}
	
	/* JSON item parsers */
	/**
	 * {@code parseNull()} parses {@code JsonType.NULL}, which in JSON
	 * context is literally "null".
	 * 
	 * @return JsonParser
	 * @throws InvalidValueException
	 * @throws IndexOutOfBoundsException
	 */
	private JsonParser parseNull()
			throws InvalidValueException, IndexOutOfBoundsException
	{
		assert !isEndOfContext();
		assert getCurChar() == 'n';
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + 4}
		 *  is greater than {@code length()} of this string.
		 */
		String item = context.substring(cur, cur + 4);
		
		if (!item.equals("null")) {
			throw new InvalidValueException("Misspelling \"null\" as \""
					+ item + "\". ");
		}
		
		value.setType(JsonType.NULL);
		cur += 4;
		
		return this;
	}
	/**
	 * {@code parseTrue()} parses {@code JsonType.TRUE}, which in JSON
	 * context is literally "true".
	 * 
	 * @return JsonParser
	 * @throws InvalidValueException
	 * @throws IndexOutOfBoundsException
	 */
	private JsonParser parseTrue()
			throws InvalidValueException, IndexOutOfBoundsException
	{
		assert !isEndOfContext();
		assert getCurChar() == 't';
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + 4}
		 *  is greater than {@code length()} of this string.
		 */
		String item = context.substring(cur, cur + 4);
		
		if (!item.equals("true")) {
			throw new InvalidValueException("Misspelling \"true\" as \""
						+ item + "\". ");
		}
		
		value.setType(JsonType.TRUE);
		cur += 4;
		
		return this;
	}
	/**
	 * {@code parseFalse()} parses {@code JsonType.FALSE}, which in JSON
	 * context is literally "false".
	 * 
	 * @return JsonParser
	 * @throws InvalidValueException
	 * @throws IndexOutOfBoundsException
	 */
	private JsonParser parseFalse()
			throws InvalidValueException, IndexOutOfBoundsException
	{
		assert !isEndOfContext();
		assert getCurChar() == 'f';
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + 5}
		 *  is greater than {@code length()} of this string.
		 */
		String item = context.substring(cur, cur + 5);
		
		if (!item.equals("false")) {
			throw new InvalidValueException("Misspelling \"false\" as \""
					+ item + "\". ");
		}
		
		value.setType(JsonType.FALSE);
		cur += 5;
		
		return this;
	}
	/**
	 * {@code parseLiteral(JsonType)} can deal with {@code JsonType.NULL}, 
	 * {@code JsonType.TRUE} and {@code JsonType.FALSE}. 
	 * 
	 * @param type
	 * @return JsonParser
	 * @throws InvalidValueException
	 * @throws IndexOutOfBoundsException
	 */
	@SuppressWarnings("unused")
	private JsonParser parseLiteral(JsonType type) 
			throws InvalidValueException, IndexOutOfBoundsException
	{
		assert !isEndOfContext();
		String typeName = type.toString().toLowerCase();
		int len = typeName.length();
		
		assert getCurChar() == typeName.charAt(0);
		/* 
		 * {@code IndexOutOfBoundsException} may be thrown if {@code cur + len}
		 *  is greater than {@code length()} of this string.
		 */
		String item = context.substring(cur, cur + len);
		
		if (!item.equals(typeName)) {
			throw new InvalidValueException("Misspelling \"" + typeName + "\" as \""
					+ item + "\". ");
		}
		
		value.setType(type);
		cur += len;
		
		return this;
	}
	/**
	 * {@code parseNumber()} parses {@code JsonType.NUMBER}. 
	 * 
	 * @return JsonParser
	 */
	private JsonParser parseNumber()
			throws IncompleteNumberException, RootNotSingularException, InvalidValueException
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
			throw new IncompleteNumberException("There is no digit following the negative sign. ");
		}
		
		if (!isEndOfContext() && getCurChar() == '.') {
			parseFractionalComponent();
		}
		
		if (!isEndOfContext() && (getCurChar() == 'e' || getCurChar() == 'E')) {
			parseExponentComponent();
		}
		
		value.setType(JsonType.NUMBER);
		value.setNum(Double.parseDouble(context.substring(tmpCur, cur)));
		
		/* 
		 * -0.0 is less than 0.0 in Java Double type, which is not the case
		 * in JSON. Here we change signed 0s to positive 0s.
		 */
		if (value.getNum() == -0.0) {
			value.setNum(0.0);
		}
		
		return this;
	}
	private JsonParser parseString() {
		assert !isEndOfContext();
		assert getCurChar() == '\"';
		++cur;
		
		String cache = "";
		
		for (; !isEndOfContext() && getCurChar() != '\"'; ++cur) {
			cache += switch (getCurChar()) {
			case '\\' -> parseEscapeChar();
			default -> parseNormalChar();
			};
		}
		
		if (isEndOfContext()) {
			throw new InvalidValueException("Missing closing quotation mark. ");
		}
		else { /* skip ending '\"' */
			++cur;
		}
		
		value.setType(JsonType.STRING);
		value.setStr(cache);
		
		return this;
	}
	private JsonParser parseJson()
			throws InvalidValueException, ExpectValueException, IndexOutOfBoundsException
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
		default:
			throw new InvalidValueException("Invalid JSON context. ");
		}
		
		return this;
	}
	
	/* parser API */
	public JsonParser parse(String context) {
		this.context = context;
		rewind();
		
		/* 
		 * {@code cur} will never be greater than the length of {@code context},
		 * so here {@code IndexOutOfBoundsException} is never thrown by
		 * {@code substring()} in {@code isEndOfContext()}. 
		 */
		if (isEndOfContext()) {
			throw new ExpectValueException("Empty input. ");
		}
		
		parseWhitespace();

		if (isEndOfContext()) {
			throw new InvalidValueException("The input context contains only whitespace. ");
		}
		
		parseJson();
		
		if (!isEndOfContext()) {
			if (isCurWhitespace()) {
				parseWhitespace();
			}
			else {
				throw new MissingDelimiterException("Context not in JSON format. "
								+ "Separate by whitespaces if more than 1 item is entered. ");
			}
		}
		
		if (!isEndOfContext()) {
			/* 
			 * Private parser functions should not deal with this exception.
			 * This exception can be made into an interface of a future multi-parser.
			 */
			throw new RootNotSingularException("Extra context detected after the first valid JSON. ");
		}
		
		return this;
	}
}
