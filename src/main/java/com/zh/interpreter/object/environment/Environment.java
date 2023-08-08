package com.zh.interpreter.object.environment;

import com.zh.interpreter.object.Object;

import java.util.HashMap;
import java.util.Map;

/**
 * 环境,用于跟踪标识符及其值
 */
public class Environment {
    private final Map<String, Object> stores;

    /**
     * 外层环境
     */
    public Environment outerEnvironment;

    public Environment() {
        stores = new HashMap<>();
        outerEnvironment = null;
    }

    public Environment(Environment outerEnvironment) {
        stores = new HashMap<>();
        this.outerEnvironment = outerEnvironment;
    }

    /**
     * 设置标识符及其对应的数据对象
     *
     * @param identifier 标识符
     * @param dataObject 数据对象
     */
    public void setObject(String identifier, Object dataObject) {
        stores.put(identifier, dataObject);
    }

    /**
     * 获取标识符对应的数据对象,如果当前环境中不存在对应标识符则向外层环境中寻找
     *
     * @param identifier 标识符
     * @return 数据对象
     */
    public Object getObject(String identifier) {
        Object object = stores.get(identifier);
        if (object == null && outerEnvironment != null) {
            object = outerEnvironment.getObject(identifier);
        }
        return object;
    }
}
