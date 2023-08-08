package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Hashable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.Objects;

/**
 * 字符串对象
 */
public class StringObject extends Object implements Hashable, Computable, Cloneable {
    /**
     * 字符串对象的实际值
     */
    public String value;

    @Override
    public ObjectType getType() {
        return ObjectType.STRING_OBJECT;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringObject that = (StringObject) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public StringObject cloneObject() {
        StringObject stringObject = new StringObject();
        stringObject.value = value;
        return stringObject;
    }
}
