# JSON parser in Java

## Contents

1. [Introduction](#introduction)
2. [Usage](#usage)

## Introduction

This is a JSON parser in Java. This application is developed according to [the JSON parser tutorial of @miloyip](https://github.com/miloyip/json-tutorial).

## Usage

`jsonparser.parser.JsonParser` is the user API for the parser. Users may call `JsonParser.parse(<JSON_context>)` to parse any legal JSON context to JSON value. The result can be reached by calling `Parser.getValue()`. 

`jsonparser.parser.JsonValue` is a data structure to save JSON value results. Users may call `JsonValue.getType()` to check the type of the JSON item. For non-literal items (NUMBER, STRING, ARRAY and OBJECT), corresponding `getter`s are available for the value of items.

JSON array and object types are implemented by `ArrayList<JsonValue>` and `HashMap<String, JsonValue>`, respectively.

```java
/**
 * This is an example.
 */
import jsonparser.parser.JsonParser

JsonParser parser = new JsonParser();
String JSONContext = "{\"name\":\"Eric\", \"age\":100, \"hobbies\":[\"table tennis\", \"reading\"], \"siblings\":null}";
parser.parse(JSONContext);

JsonValue value = parser.getValue();

value.getObjValue("name"); // "Eric"
```