package com.zh.interpreter.object.struct;

import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Computable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 数组对象
 */
public class ArrayObject extends Object implements Computable, Cloneable {
    /**
     * 数组内的元素
     */
    public final List<Object> elements = new ArrayList<>();

    @Override
    public ObjectType getType() {
        return ObjectType.ARRAY_OBJECT;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayObject that = (ArrayObject) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        return elements.toString();
    }

    @Override
    public ArrayObject cloneObject() {
        ArrayObject arrayObject = new ArrayObject();
        arrayObject.elements.addAll(elements);
        return arrayObject;
    }
}
