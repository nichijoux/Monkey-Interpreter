package com.zh.interpreter.object.tools;

import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

/**
 * 错误对象
 */
public class ErrorObject extends Object {
    /**
     * 错误信息
     */
    public String message;

    public ErrorObject(String message) {
        this.message = message;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.ERROR_OBJECT;
    }

    @Override
    public String toString() {
        return "Error:" + message;
    }
}
