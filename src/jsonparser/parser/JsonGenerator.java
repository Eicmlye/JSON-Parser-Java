package jsonparser.parser;

import jsonparser.exception.*;

public class JsonGenerator {
	String context;
	JsonValue value;
	boolean isGenerated;
	
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
		
		
		return;
	}
	private void generateString() {
		
		
		return;
	}
	private void generateArray() {
		
		
		return;
	}
	private void generateObject() {
		
		
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
