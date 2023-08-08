package com.zh.interpreter.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class ReflectUtils {
    /**
     * 查找包下的所有类的名字
     *
     * @param packageName 包名
     * @return List集合, 内容为类的全名
     */
    public static List<Class<?>> getPacketClass(String packageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Class<?>> classes = new ArrayList<>();
        while (resources != null && resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                // 在文件系统中的情况
                File file = new File(resource.getFile());
                if (file.isDirectory()) {
                    String[] files = file.list();
                    if (files != null) {
                        for (String fileName : files) {
                            if (fileName.endsWith(".class")) {
                                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
                                Class<?> clazz = null;
                                try {
                                    clazz = Class.forName(className);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                classes.add(clazz);
                            }
                        }
                    }
                }
            } else if (resource.getProtocol().equals("jar")) {
                // 在jar文件中的情况
                try {
                    JarURLConnection connection = (JarURLConnection) resource.openConnection();
                    JarFile jarFile = connection.getJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                            String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                            Class<?> clazz = null;
                            try {
                                clazz = Class.forName(className);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            classes.add(clazz);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

    /**
     * 反射调用函数
     *
     * @param instance 调用函数实例或者静态函数的字节码对象
     * @param method   要调用的方法
     * @param args     方法参数
     * @return 返回值
     */
    public static Object invokeMethod(Object instance, Method method, Object... args) {
        method.setAccessible(true);
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
