package com.zh.interpreter.utils;

import java.util.Objects;

public abstract class EqualUtils {

    /**
     * 判断source是否与target中的某一个相同
     *
     * @param source 原数据
     * @param target 目标数据组
     * @return 是否和目标数据组中的某个数据相等
     */
    public static boolean equalsIn(Object source, Object... target) {
        for (Object t : target) {
            if (Objects.equals(source, t)) {
                return true;
            }
        }
        return false;
    }
}
