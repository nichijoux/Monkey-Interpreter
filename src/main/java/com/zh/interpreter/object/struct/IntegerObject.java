package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Hashable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.Objects;

/**
 * 整数类型
 */
public class IntegerObject extends Object implements Hashable, Computable, Cloneable {
    /**
     * 整数对象的实际值
     */
    public Long value;

    public IntegerObject() {
    }

    public IntegerObject(Long value) {
        this.value = value;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.INTEGER_OBJECT;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerObject that = (IntegerObject) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public IntegerObject cloneObject() {
        IntegerObject integerObject = new IntegerObject();
        integerObject.value = value;
        return integerObject;
    }
}
