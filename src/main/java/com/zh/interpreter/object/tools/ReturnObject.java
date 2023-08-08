package com.zh.interpreter.object.tools;

import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

/**
 * 返回值对象
 */
public class ReturnObject extends Object {
    /**
     * 要返回的对象
     */
    public Object value;

    @Override
    public ObjectType getType() {
        return ObjectType.RETURN_OBJECT;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
