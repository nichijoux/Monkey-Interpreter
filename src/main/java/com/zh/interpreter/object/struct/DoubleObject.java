package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Hashable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.Objects;

/**
 * 浮点数数据类型
 */
public class DoubleObject extends Object implements Hashable, Computable, Cloneable {
    /**
     * double数值
     */
    public Double value;

    public DoubleObject() {
    }

    public DoubleObject(Double value) {
        this.value = value;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.DOUBLE_OBJECT;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleObject that = (DoubleObject) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public DoubleObject cloneObject() {
        DoubleObject doubleObject = new DoubleObject();
        doubleObject.value = value;
        return doubleObject;
    }
}
