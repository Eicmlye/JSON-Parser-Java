package pers.ericmonlye.jsonparser.parser;

import java.lang.String;

/**
 * 
 */
public enum JsonType {
	NULL(1, "NULL"),
	TRUE(2, "TRUE"),
	FALSE(3, "FALSE"),
	NUMBER(4, "NUMBER"),
	STRING(5, "STRING"),
	ARRAY(6, "ARRAY"),
	OBJECT(7, "OBJECT");
	
	public final int typeValue;
	public final String typeName;
	
	private JsonType(int typeValue, String typeName) {
		this.typeValue = typeValue;
		this.typeName = typeName;
	}
	
	/**
	 * Overridden {@code toString()} method is a descriptive method
	 * for an {@code enum} variable. To get the precise name of the
	 * {@code enum} variable, always use {@code name()} method.
	 */
	@Override
	public String toString() {
		return this.typeName;
	}
}
