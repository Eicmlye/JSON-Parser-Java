package jsonparser.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import jsonparser.exception.*;

/**
 * 
 */
public class JsonValue implements Cloneable {
	private JsonType type;
	private double num;
	private String str; /* char[] does not override the equals() method of Object class. 
						 * So do not use char[] to implement JSON string. */
	private ArrayList<JsonValue> arr;
	private HashMap<String, JsonValue> obj; /* For the same reason as JSON string, 
											 * do not use char[] here. */
	
	/* constructors */
	public JsonValue() {
		this.type = JsonType.NULL;
		this.num = 0;
		this.str = "";
		this.arr = null;
		this.obj = null;
	}
	public JsonValue(JsonType type) {
		this.type = type;
		this.num =  0;
		this.str = "";
		this.arr = (type == JsonType.ARRAY ? new ArrayList<JsonValue>() : null);
		this.obj = (type == JsonType.OBJECT ? new HashMap<String, JsonValue>() : null);
	}
	public JsonValue(double num) {
		this.type = JsonType.NUMBER;
		this.num = num;
		this.str = "";
		this.arr = null;
		this.obj = null;
	}
	public JsonValue(String str) {
		this.type = JsonType.STRING;
		this.num = 0;
		this.str = str;
		this.arr = null;
		this.obj = null;
	}
	public JsonValue(ArrayList<JsonValue> arr) {
		this.type = JsonType.ARRAY;
		this.num = 0;
		this.str = "";
		this.arr = arr;
		this.obj = null;
	}
	public JsonValue(HashMap<String, JsonValue> obj){
		this.type = JsonType.OBJECT;
		this.num = 0;
		this.str = "";
		this.arr = null;
		this.obj = obj; 
	}

	
	/* JavaBeans */
	void setType(JsonType type) {
		this.type = type;
		
		return;
	}
	double getNum() {
		assert type == JsonType.NUMBER;
		
		return this.num;
	}
	void setNum(double num) {
		assert type == JsonType.NUMBER;
		this.num = num;
		
		return;
	}
	String getStr() {
		assert type == JsonType.STRING;
		
		return this.str;
	}
	void setStr(String str) {
		assert type == JsonType.STRING;
		this.str = str;
		
		return;
	}
	ArrayList<JsonValue> getArr() {
		assert type == JsonType.ARRAY;
		
		return this.arr;
	}
	JsonValue getArrElem(int index) {
		assert type == JsonType.ARRAY;
		
		return this.arr.get(index);
	}
	void setArr(ArrayList<JsonValue> arr) {
		assert type == JsonType.ARRAY;
		this.arr = arr;
		
		return;
	}
	HashMap<String, JsonValue> getObj() {
		assert type == JsonType.OBJECT;
		
		return this.obj;
	}
	JsonValue getObjValue(String key) {
		assert type == JsonType.OBJECT;
		
		return this.obj.get(key);
	}
	void setObj(HashMap<String, JsonValue> obj) {
		assert type == JsonType.OBJECT;
		this.obj = obj;
		
		return;
	}

	/* user APIs */
	public JsonType getType() {
		return this.type;
	}
	public Object getValue() {
		return switch(type) {
				case NULL -> null;
				case TRUE -> true;
				case FALSE -> false;
				case NUMBER -> num;
				case STRING -> str;
				case ARRAY -> arr;
				case OBJECT -> obj;
				default -> throw new JsonParserException("Unknown JSON type. ");
				};
	}
	public void setValue() { // set NULL;
		clear();
		
		return;
	}
	public void setValue(boolean val) {
		clear();
		type = val ? JsonType.TRUE : JsonType.FALSE;
		
		return;
	}
	public void setValue(double val) {
		clear();
		type = JsonType.NUMBER;
		num = val;
		
		return;
	}
	public void setValue(String val) {
		clear();
		type = JsonType.STRING;
		str = val;
		
		return;
	}
	public void setValue(ArrayList<JsonValue> val) {
		clear();
		type = JsonType.ARRAY;
		arr = val;
		
		return;
	}
	public void setValue(HashMap<String, JsonValue> val) {
		clear();
		type = JsonType.OBJECT;
		obj = val;
		
		return;
	}
	public void clear() {
		type = JsonType.NULL;
		num = 0;
		str = "";
		arr = null;
		obj = null;
		
		return;
	}
	
	/* overridden methods */
	@Override
	public boolean equals(Object other) { 
        // If the object is compared with itself then return true.
        if (other == this) {
            return true;
        }
 
        /* Check if {@code other} is an instance of JsonValue or not
          {@code null instanceof [type]} also returns {@code false} */
        if (!(other instanceof JsonValue)) {
            return false;
        }

        // typecast {@code other} to JsonValue to compare data members.
        JsonValue casted = (JsonValue) other;
        
        /* compare type */
        if (!type.equals(casted.getType())) {
        	return false;
        }
        /* compare data members */
        switch (type) {
        case NULL, TRUE, FALSE: 
        	return true;
        case NUMBER:
        	return num == casted.getNum();
        case STRING:
        	return str.equals(casted.getStr());
        case ARRAY:
        	return arr.equals(casted.getArr());
        case OBJECT:
        	/* 
        	 * OBJECTs of different sizes can never be equal. And since
        	 * both the constructor and parser ensure that the keys
        	 * are distinct from each other, equal-sized OBJECTs need
        	 * to compare only one round to check the equivalence. 
        	 */
        	HashMap<String, JsonValue> castedObj = casted.getObj();
        	
        	if (obj.entrySet().size() == castedObj.entrySet().size()) {
        		if (obj.entrySet().size() == 0) {
        			return true;
        		}
        		
            	for (Entry<String, JsonValue> e : obj.entrySet()) {
            		boolean foundKey = false;
            		
            		for (Entry<String, JsonValue> f : castedObj.entrySet()) {
            			String key = e.getKey();
            			String castedKey = f.getKey();
            			
            			if (key.equals(castedKey)) {
            				foundKey = true;
            				
            				if (!obj.get(key).equals(castedObj.get(castedKey))) {
            					return false;
            				}
            			}
            			
            			if (foundKey) {
            				break;
            			}
            		}
            		
            		if (!foundKey) {
            			return false;
            		}
            	}
            	
            	return true;
        	}
        	else {
        		return false;
        	}
        default:
        	return false;
        }
	}
	@Override
	public Object clone() {
		/* This is NOT shallow copy. Every subelement is copied. */
        try {
        	JsonValue v = (JsonValue) super.clone();
        	v.type = type;
        	v.num = num; 
            v.str = (type == JsonType.STRING) ? String.valueOf(str.toCharArray()) : "";
            
            if (type != JsonType.ARRAY) {
            	v.arr = null;
            }
            else {
            	v.arr = new ArrayList<JsonValue>();
            	for (JsonValue item : arr) {
            		v.arr.add((JsonValue)item.clone());
            	}
            }
            
            if (type != JsonType.OBJECT) {
            	v.obj = null;
            }
            else {
            	v.obj = new HashMap<String, JsonValue>();
            	for (Entry<String, JsonValue> e : obj.entrySet()) {
            		v.obj.put(String.valueOf(e.getKey().toCharArray()), (JsonValue)e.getValue().clone());
            	}
            }
            
            return v;
        }
        catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
	}
}
