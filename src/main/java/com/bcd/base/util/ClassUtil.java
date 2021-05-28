package com.bcd.base.util;


import com.bcd.base.exception.BaseRuntimeException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ClassUtil {
    public static Type getParentUntil(Class startClass, Class... endClasses) {
        Type parentType = startClass.getGenericSuperclass();
        while (true) {
            if (parentType instanceof ParameterizedType) {
                Class rawType = (Class) ((ParameterizedType) parentType).getRawType();
                boolean isMatch = false;
                for (Class endClass : endClasses) {
                    if (rawType.equals(endClass)) {
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    break;
                } else {
                    parentType = rawType.getGenericSuperclass();
                }
            } else {
                parentType = ((Class) parentType).getGenericSuperclass();
            }
        }
        return parentType;
    }

    /**
     * 递归扫描, 找出所有此注解及其标注的子注解所标注的所有类, 结果根据注解类型分类
     *
     * @param annoClass
     * @param packages
     * @return
     */
    public static Map<String, List<Class>> findWithSub(Class annoClass, String... packages) {
        try {
            Map<String, List<Class>> annoNameToClassListMap = new HashMap<>();
            //1、找出所有带注解的类
            List<Class> classList = ClassUtil.getClassesWithAnno(annoClass, packages);
            //2、找出其中的 注解,并从集合中移除
            List<Class> subAnnoList = new ArrayList<>();
            for (int i = 0; i <= classList.size() - 1; i++) {
                Class clazz = classList.get(i);
                if (clazz.isAnnotation()) {
                    subAnnoList.add(clazz);
                    classList.remove(i);
                    i--;
                }
            }
            //3、找出所有子注解的类
            for (Class subAnno : subAnnoList) {
                //3.1、将子注解扫描出来的类添加进去
                Map<String, List<Class>> tempMap = findWithSub(subAnno, packages);
                annoNameToClassListMap.putAll(tempMap);
            }
            //4、返回此注解和其子注解 标注的类
            annoNameToClassListMap.put(annoClass.getName(), classList);
            return annoNameToClassListMap;
        } catch (IOException | ClassNotFoundException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    /**
     * 找出所有带注解的类
     *
     * @param annoClass
     * @param packageNames
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Class> getClassesWithAnno(Class annoClass, String... packageNames) throws IOException, ClassNotFoundException {
        Set<Class> classSet = new HashSet<>();
        for (String packageName : packageNames) {
            classSet.addAll(getClasses(packageName));
        }
        return classSet.stream().filter(e -> e.getAnnotation(annoClass) != null).collect(Collectors.toList());
    }

    /**
     * 根据父类找出所有子类,去除接口和抽象类
     *
     * @param parentClass
     * @param packageNames
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Class> getClassesByParentClass(Class parentClass, String... packageNames) throws IOException, ClassNotFoundException {
        Set<Class> classSet = new HashSet<>();
        for (String packageName : packageNames) {
            classSet.addAll(getClasses(packageName));
        }
        return classSet.stream().filter(e -> {
            int modifiers = e.getModifiers();
            return !Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers) && parentClass.isAssignableFrom(e);
        }).collect(Collectors.toList());
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param packageName
     * @return
     */
    public static List<Class> getClasses(String packageName) throws IOException, ClassNotFoundException {
        // 第一个class类的集合
        List<Class> classes = new ArrayList<>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        // 循环迭代下去
        while (dirs.hasMoreElements()) {
            // 获取下一个元素
            URL url = dirs.nextElement();
            // 得到协议的名称
            String protocol = url.getProtocol();
            // 如果是以文件的形式保存在服务器上
            if ("file".equals(protocol)) {
                // 获取包的物理路径
                String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name());
                // 以文件的方式扫描整个包下的文件 并添加到集合中
                findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
            } else if ("jar".equals(protocol)) {
                // 如果是jar包文件
                // 定义一个JarFile
                JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                // 从此jar包 得到一个枚举类
                Enumeration<JarEntry> entries = jar.entries();
                // 同样的进行循环迭代
                while (entries.hasMoreElements()) {
                    // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    // 如果是以/开头的
                    if (name.charAt(0) == '/') {
                        // 获取后面的字符串
                        name = name.substring(1);
                    }
                    // 如果前半部分和定义的包名相同
                    if (name.startsWith(packageDirName)) {
                        int idx = name.lastIndexOf('/');
                        // 如果以"/"结尾 是一个包
                        if (idx != -1) {
                            // 获取包名 把"/"替换成"."
                            packageName = name.substring(0, idx).replace('/', '.');
                        }
                        // 如果可以迭代下去 并且是一个包
                        if ((idx != -1) || recursive) {
                            // 如果是一个.class文件 而且不是目录
                            if (name.endsWith(".class") && !entry.isDirectory()) {
                                // 去掉后面的".class" 获取真正的类名
                                String className = name.substring(packageName.length() + 1, name.length() - 6);
                                // 添加到classes
                                classes.add(Class.forName(packageName + '.' + className));
                            }
                        }
                    }
                }
            }
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class> classes) throws ClassNotFoundException {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                // 添加到集合中去
                classes.add(Class.forName(packageName + '.' + className));

            }
        }
    }
}
