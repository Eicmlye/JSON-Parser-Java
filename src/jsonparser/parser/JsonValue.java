package jsonparser.parser;

import jsonparser.exception.*;

/**
 * 
 */
public class JsonValue {
	private JsonType type;
	private double num;
	private String str;
	
	/* constructors */
	public JsonValue() {
		this.type = JsonType.NULL;
		this.num = 0;
		this.str = "";
	}
	public JsonValue(JsonType type) {
		this.type = type;
		this.num =  0;
		this.str = "";
	}
	public JsonValue(double num) {
		this.type = JsonType.NUMBER;
		this.num = num;
		this.str = "";
	}
	public JsonValue(String str) {
		this.type = JsonType.STRING;
		this.num = 0;
		this.str = str;
	}

	/* JavaBeans */
	public JsonType getType() {
		return this.type;
	}
	public void setType(JsonType type) {
		this.type = type;
		
		return;
	}
	public double getNum() {
		if (this.type != JsonType.NUMBER) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no number value. ");
		}
		
		return this.num;
	}
	public void setNum(double num) {
		if (this.type != JsonType.NUMBER) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no number value. ");
		}
		this.num = num;
		
		return;
	}
	public String getStr() {
		if (this.type != JsonType.STRING) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no string value. ");
		}
		
		return this.str;
	}
	public void setStr(String str) {
		if (this.type != JsonType.STRING) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no string value. ");
		}
		this.str = str;
		
		return;
	}
}
