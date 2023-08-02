package jsonparser.parser;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import jsonparser.exception.*;

public class JsonGenerator {
	private String context;
	private JsonValue value;
	private boolean isGenerated;
	
	/* constructors */
	public JsonGenerator() {
		this.context = "";
		this.value = new JsonValue();
		this.isGenerated = false;
	}
	public JsonGenerator(JsonValue val) {
		this.context = "";
		this.value = val;
		this.isGenerated = false;
	}
	
	/* user APIs */
	public JsonGenerator generate() {
		refreshContext();
		generateJson();
		this.isGenerated = true;
		
		return this;
	}
	public JsonGenerator generate(JsonValue val) {
		this.value = val;
		generate();
		
		return this;
	}
	public JsonGenerator print() {
		if (!isGenerated) {
			generate();
		}

		System.out.println(context);
		
		return this;
	}
	public String getContext() {
		if (!isGenerated) {
			generate();
		}
		
		return this.context;
	}
	
	/* context manipulation methods */
	private JsonGenerator refreshContext() {
		this.context = "";
		this.isGenerated = false;
		
		return this;
	}
	
	/* generators for JSON items */
	private void generateNull() {
		context += "null";
		
		return;
	}
	private void generateTrue() {
		context += "true";
		
		return;
	}
	private void generateFalse() {
		context += "false";
		
		return;
	}
	private void generateNumber() {
		DecimalFormat formatter = new DecimalFormat("#################E0", 
											DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
		formatter.setRoundingMode(RoundingMode.DOWN); // turn off auto-rounding
		formatter.setGroupingUsed(false);
		
		context += formatter.format(value.getValue());
		
		return;
	}
	private void generateString() {
		context += '\"';
		
		String str = (String)value.getValue();
		for (int index = 0; index < str.length(); ++index) {
			char ch = str.charAt(index);
			
			context += switch(ch) {
			case '\"' -> "\\\""; 
			case '\\' -> "\\\\"; 
			case '/' -> "\\/";
			case '\b' -> "\\b";
			case '\f' -> "\\f";
			case '\n' -> "\\n";
			case '\r' -> "\\r";
			case '\t' -> "\\t";
			default -> {
				if (ch < 0x20) {
					String hex = Integer.toHexString(ch);
					while (hex.length() < 4) {
						hex = "0" + hex;
					}
					
					yield "\\u" + hex;
				}
				
				yield ch;
			}
			};
		}

		context += '\"';
		
		return;
	}
	private void generateArray() {
		context += '[';
		
		@SuppressWarnings("unchecked")
		ArrayList<JsonValue> arr = (ArrayList<JsonValue>)value.getValue();
		JsonGenerator gen = new JsonGenerator();
		
		for (JsonValue val : arr) {
			context += (val == arr.get(0) ? "" : ',');
			
			context += gen.generate(val).getContext();
		}
		
		context += ']';
		
		return;
	}
	private void generateObject() {
		context += '{';
		
		@SuppressWarnings("unchecked")
		HashMap<String, JsonValue> hmp = (HashMap<String, JsonValue>)value.getValue();
		JsonGenerator gen = new JsonGenerator();
		boolean isFirstEntry = true;
		
		for (Entry<String, JsonValue> e : hmp.entrySet()) {
			context += (isFirstEntry ? "" : ',');
			isFirstEntry = false;
			
			context += '\"' + e.getKey() + "\":";
			
			context += gen.generate(e.getValue()).getContext();
		}
		
		context += '}';
		
		return;
	}
	private void generateJson() {
		switch(value.getType()) {
		case NULL:
			generateNull();
			break;
		case TRUE:
			generateTrue();
			break;
		case FALSE:
			generateFalse();
			break;
		case NUMBER:
			generateNumber();
			break;
		case STRING:
			generateString();
			break;
		case ARRAY:
			generateArray();
			break;
		case OBJECT:
			generateObject();
			break;
		default:
			throw new JsonParserException();
		}
		
		return;
	}
}
