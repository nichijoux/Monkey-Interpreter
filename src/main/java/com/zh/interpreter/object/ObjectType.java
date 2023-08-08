package com.zh.interpreter.object;

/**
 * 对象类型
 */
public enum ObjectType {
    INTEGER_OBJECT("Integer"),
    BOOLEAN_OBJECT("Boolean"),
    DOUBLE_OBJECT("Double"),
    STRING_OBJECT("String"),
    ARRAY_OBJECT("Array"),
    HASH_OBJECT("Hash"),
    JAVA_OBJECT("Java"),
    NULL_OBJECT("Null"),
    FUNCTION_OBJECT("Function"),
    BUILT_IN_FUNCTION_OBJECT("BuiltInFunction"),
    RETURN_OBJECT("ReturnValue"),
    ERROR_OBJECT("Error"),
    QUOTE_OBJECT("Quote"),
    MACRO_FUNCTION_OBJECT("MacroFunction");

    private final String type;

    ObjectType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
