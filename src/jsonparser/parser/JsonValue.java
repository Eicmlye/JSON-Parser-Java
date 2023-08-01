package jsonparser.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import jsonparser.exception.TypeValueCorrespondencyException;

/**
 * 
 */
public class JsonValue implements Cloneable {
	private JsonType type;
	private double num;
	private String str;
	private ArrayList<JsonValue> arr;
	private HashMap<String, JsonValue> obj;
	
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
		this.obj = obj; /* TODO: ensure that the keys are distinct from each other. */
	}

	/* user APIs */
	public JsonType getType() {
		return this.type;
	}
	public void setType(JsonType type) {
		this.type = type;
		
		return;
	}
	
	/* JavaBeans */
	public double getNum()
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.NUMBER) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no number value. ");
		}
		
		return this.num;
	}
	public void setNum(double num)
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.NUMBER) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no number value. ");
		}
		this.num = num;
		
		return;
	}
	public String getStr()
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.STRING) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no string value. ");
		}
		
		return this.str;
	}
	public void setStr(String str)
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.STRING) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no string value. ");
		}
		this.str = str;
		
		return;
	}
	public ArrayList<JsonValue> getArr()
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.ARRAY) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no array element. ");
		}
		
		return this.arr;
	}
	public JsonValue getArrElem(int index)
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.ARRAY) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no array element. ");
		}
		
		return this.arr.get(index);
	}
	public void setArr(ArrayList<JsonValue> arr) 
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.ARRAY) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no array element. ");
		}
		this.arr = arr;
		
		return;
	}
	public HashMap<String, JsonValue> getObj()
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.OBJECT) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no object element. ");
		}
		
		return this.obj;
	}
	public JsonValue getObjValue(String key)
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.OBJECT) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no object element. ");
		}
		
		return this.obj.get(key);
	}
	public void setObj(HashMap<String, JsonValue> obj) 
			throws TypeValueCorrespondencyException
	{
		if (this.type != JsonType.OBJECT) {
			throw new TypeValueCorrespondencyException("JSON " + type.toString()
							+ " type has no object element. ");
		}
		this.obj = obj;
		
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
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		/* TODO: arr.clone() and obj.clone() are still shallow copy. */
        try {
        	JsonValue v = (JsonValue) super.clone();
        	v.type = type;
        	v.num = num; 
            v.str = (type == JsonType.STRING) ? String.valueOf(str.toCharArray()) : "";
            v.arr = (type == JsonType.ARRAY) ? (ArrayList<JsonValue>)arr.clone() : null;
            v.obj = (type == JsonType.OBJECT) ? (HashMap<String, JsonValue>)obj.clone() : null;
            
            return v;
        }
        catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
	}
}
