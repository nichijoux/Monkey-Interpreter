package com.zh.interpreter.object.environment;

import com.zh.interpreter.annotation.IgnoreMethod;
import com.zh.interpreter.object.Cloneable;
import com.zh.interpreter.object.Object;
import com.zh.interpreter.object.ObjectType;
import com.zh.interpreter.object.struct.*;
import com.zh.interpreter.object.tools.ErrorObject;
import com.zh.interpreter.object.tools.NullObject;
import com.zh.interpreter.utils.ObjectUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;

/**
 * 内置函数的环境
 */
@SuppressWarnings({"unused", "rawtypes", "DuplicatedCode"})
public class BuiltInEnvironment extends Environment {
    private static final BuiltInEnvironment instance = new BuiltInEnvironment();

    @IgnoreMethod
    public static BuiltInEnvironment getInstance() {
        return instance;
    }

    /**
     * 构造内置函数对象
     */
    private BuiltInEnvironment() {
        Method[] methods = BuiltInEnvironment.class.getDeclaredMethods();
        for (Method method : methods) {
            IgnoreMethod annotation = method.getDeclaredAnnotation(IgnoreMethod.class);
            if (annotation != null || method.isSynthetic()) {
                continue;
            }
            setObject(method.getName(), new BuiltInFunctionObject(method));
        }
    }

    /**
     * 内置函数,获取对象长度<br/>
     * 支持StringObject、ArrayObject、HashObject
     *
     * @param args 传入参数
     * @return IntegerObject或者ErrorObject数据
     */
    private static Object size(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {size} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        Object object = args[0];
        IntegerObject integerObject = new IntegerObject();
        switch (object.getType()) {
            case STRING_OBJECT:
                integerObject.value = (long) ((StringObject) object).value.length();
                break;
            case ARRAY_OBJECT:
                integerObject.value = (long) ((ArrayObject) object).elements.size();
                break;
            case HASH_OBJECT:
                integerObject.value = (long) ((HashObject) object).hashMap.size();
                break;
            default:
                return new ErrorObject(String.format("argument to {size} not supported, get %s", object.getType()));
        }
        return integerObject;
    }

    /**
     * 向数组位处添加新元素
     *
     * @param args 可变参数(应该为2个元素,第一个参数为数组对象,第二个参数为推入新元素)
     * @return 数组对象
     */
    private static Object push(Object... args) {
        if (args.length != 2) {
            return new ErrorObject(String.format("the function {push} get wrong number of arguments,want 2 argument but real get %d", args.length));
        }
        // 获取对象
        if (args[0].getType() != ObjectType.ARRAY_OBJECT) {
            return new ErrorObject(String.format("the object is not a array,it's real type is %s", args[0].getType()));
        }
        ArrayObject array = (ArrayObject) args[0];
        // 推入数据
        array.elements.add(args[1]);
        return array;
    }

    /**
     * 删除数组末尾的元素
     *
     * @param args 可变参数(长度应该为1)
     * @return 被删除的对象
     */
    private static Object pop(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {pop} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        // 获取对象
        if (args[0].getType() != ObjectType.ARRAY_OBJECT) {
            return new ErrorObject(String.format("the object is not a array,it's real type is %s", args[0].getType()));
        }
        ArrayObject array = (ArrayObject) args[0];
        // 删除数据
        return array.elements.remove(array.elements.size() - 1);
    }

    /**
     * 获取参数的类型
     *
     * @param args 可变参数(长度应该为1)
     * @return StringObject对象
     */
    private static Object type(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {type} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        ObjectType type = args[0].getType();
        StringObject stringObject = new StringObject();
        stringObject.value = type.toString();
        return stringObject;
    }

    /**
     * 将数据转换为字符串类型
     *
     * @param args 可变参数(长度应该为1)
     * @return StringObject对象
     */
    private static Object toString(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {toString} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        return ObjectUtils.convertToString(args[0]);
    }

    /**
     * 将数据转换为数字类型
     *
     * @param args 可变参数(长度应该为1)
     * @return IntegerObject对象
     */
    private static Object toInteger(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {toInteger} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        return ObjectUtils.convertToInteger(args[0]);
    }

    /**
     * 将数据转换为浮点数类型
     *
     * @param args 可变参数(长度应该为1)
     * @return DoubleObject对象
     */
    private static Object toDouble(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {toDouble} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        return ObjectUtils.convertToDouble(args[0]);
    }

    /**
     * 将数据转换为布尔类型
     *
     * @param args 可变参数(长度应该为1)
     * @return BooleanObject对象
     */
    private static Object toBoolean(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {toBoolean} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        return ObjectUtils.convertToBoolean(args[0]);
    }

    /**
     * 将数据转换为Java类型
     *
     * @param args 可变参数(长度应该为1)
     * @return JavaObject对象
     */
    private static Object toJava(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {toJava} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        JavaObject object = new JavaObject();
        object.object = args[0];
        return object;
    }

    /**
     * 打印文件
     *
     * @param args 可变参数
     * @return NullObject对象
     */
    private static Object puts(Object... args) {
        for (Object arg : args) {
            System.out.println(arg);
        }
        return NullObject.getInstance();
    }

    /**
     * 拷贝对象
     *
     * @param args 对象
     * @return 被克隆的对象
     */
    private static Object clone(Object... args) {
        if (args.length != 1) {
            return new ErrorObject(String.format("the function {clone} get wrong number of arguments,want 1 argument but real get %d", args.length));
        }
        // 判断是否可拷贝
        Object object = args[0];
        if (!Cloneable.class.isAssignableFrom(object.getClass())) {
            return new ErrorObject(String.format("the type of %s not support clone", object.getType()));
        }
        return ((Cloneable) object).cloneObject();
    }

    /**
     * 列举出所有的内置函数
     *
     * @param args 可变参数,无用
     * @return NullObject对象
     */
    private static Object listBuiltin(Object... args) {
        Method[] methods = BuiltInEnvironment.class.getDeclaredMethods();
        for (Method method : methods) {
            IgnoreMethod annotation = method.getDeclaredAnnotation(IgnoreMethod.class);
            if (annotation != null || method.isSynthetic()) {
                continue;
            }
            System.out.println(method.getName());
        }
        return NullObject.getInstance();
    }

    /**
     * 调用Java的函数,参数要求形如<br/>
     * callJava("com.example.JavaClass","javaMethod",instance,args...);
     *
     * @param args 可变参数
     * @return JavaObject对象
     */
    private static Object callJava(Object... args) {
        if (args.length < 3) {
            return new ErrorObject(String.format("the function {callJava} need at least 3 arguments but real get %d", args.length));
        }
        try {
            // 获取类名
            String className = getString(args[0]);
            if (className == null) {
                return new ErrorObject("the function {callJava}'s first argument is className,it needs a string");
            }
            Class<?> clazz = Class.forName(className);
            // 获取函数名
            String methodName = getString(args[1]);
            if (methodName == null) {
                return new ErrorObject("the function {callJava}'s second argument is methodName,it needs a string");
            }
            // 获取参数列表,并解包
            Object[] objects = new Object[args.length - 3];
            System.arraycopy(args, 3, objects, 0, objects.length);
            java.lang.Object[] parameters = getObjects(objects);
            // 获取函数并调用返回
            Class[] array = Arrays.stream(parameters)
                    .map(java.lang.Object::getClass)
                    .toArray(Class[]::new);
            Method method = clazz.getDeclaredMethod(methodName, array);
            method.setAccessible(true);
            JavaObject javaObject = new JavaObject();
            javaObject.object = method.invoke(getObject(args[2]), parameters);
            return javaObject;
        } catch (ClassNotFoundException e) {
            return new ErrorObject("class is not exist," + e.getMessage());
        } catch (NoSuchMethodException e) {
            return new ErrorObject("method is not exist," + e.getMessage());
        } catch (IllegalAccessException e) {
            return new ErrorObject("method is not accessible," + e.getMessage());
        } catch (InvocationTargetException e) {
            return new ErrorObject("method invoke error," + e.getMessage());
        }
    }

    /**
     * 调用Java的函数,参数要求形如<br/>
     * callJava("directory","com.example.JavaClass","javaMethod",instance,args...);
     *
     * @param args 可变参数
     * @return JavaObject对象
     */
    private static Object callJavaD(Object... args) {
        if (args.length < 4) {
            return new ErrorObject(String.format("the function {callJavaD} need at least 4 arguments but real get %d", args.length));
        }
        // 将路径添加到类路径中
        String classPath = getString(args[0]);
        if (classPath == null) {
            return new ErrorObject("the function {callJavaD}'s first argument is directory,it needs a string");
        }
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{new File(classPath).toURI().toURL()});
            // 获取类名
            String className = getString(args[1]);
            if (className == null) {
                return new ErrorObject("the function {callJavaD}'s second argument is className,it needs a string");
            }
            Class<?> clazz = urlClassLoader.loadClass(className);
            // 获取函数名
            String methodName = getString(args[2]);
            if (methodName == null) {
                return new ErrorObject("the function {callJavaD}'s third argument is methodName,it needs a string");
            }
            // 获取参数列表,并解包
            Object[] objects = new Object[args.length - 4];
            System.arraycopy(args, 4, objects, 0, objects.length);
            java.lang.Object[] parameters = getObjects(objects);
            // 获取函数并调用返回
            Class[] array = Arrays.stream(parameters)
                    .map(java.lang.Object::getClass)
                    .toArray(Class[]::new);
            Method method = clazz.getDeclaredMethod(methodName, array);
            method.setAccessible(true);
            JavaObject javaObject = new JavaObject();
            javaObject.object = method.invoke(getObject(args[3]), parameters);
            return javaObject;
        } catch (MalformedURLException e) {
            return new ErrorObject(classPath + " is not a valid class path," + e.getMessage());
        } catch (ClassNotFoundException e) {
            return new ErrorObject("class is not exist," + e.getMessage());
        } catch (NoSuchMethodException e) {
            return new ErrorObject("method is not exist," + e.getMessage());
        } catch (IllegalAccessException e) {
            return new ErrorObject("method is not accessible," + e.getMessage());
        } catch (InvocationTargetException e) {
            return new ErrorObject("method invoke error," + e.getMessage());
        }
    }

    /**
     * 根据object获取指定字符串对象
     *
     * @param object 解释器对象
     * @return string
     */
    @IgnoreMethod
    private static String getString(Object object) {
        String s = null;
        if (object.getType() == ObjectType.JAVA_OBJECT && Objects.equals(((JavaObject) object).object.getClass(), String.class)) {
            s = ((JavaObject) object).object.toString();
        }
        if (object.getType() == ObjectType.STRING_OBJECT) {
            s = object.toString();
        }
        return s;
    }

    /**
     * 解包解释器object将其转换为java.lang.Object
     *
     * @param object 待转换的object对象
     * @return java.lang.Object
     */
    @IgnoreMethod
    private static java.lang.Object getObject(Object object) {
        switch (object.getType()) {
            case JAVA_OBJECT: {
                java.lang.Object o = ((JavaObject) object).object;
                if (o != null && Object.class.isAssignableFrom(o.getClass())) {
                    return getObject((Object) o);
                }
                return o;
            }
            case STRING_OBJECT: {
                return ((StringObject) object).value;
            }
            case INTEGER_OBJECT: {
                return ((IntegerObject) object).value;
            }
            case DOUBLE_OBJECT: {
                return ((DoubleObject) object).value;
            }
            case BOOLEAN_OBJECT: {
                return ((BooleanObject) object).value;
            }
            case HASH_OBJECT: {
                return ((HashObject) object).hashMap;
            }
            case ARRAY_OBJECT: {
                return ((ArrayObject) object).elements;
            }
            case NULL_OBJECT: {
                return null;
            }
        }
        return null;
    }

    /**
     * 解包解释器object将其转换为java.lang.Object
     *
     * @param objects 待转换的object数组
     * @return java.lang.Object[]
     */
    @IgnoreMethod
    private static java.lang.Object[] getObjects(Object[] objects) {
        java.lang.Object[] data = new java.lang.Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            data[i] = getObject(objects[i]);
        }
        return data;
    }
}
