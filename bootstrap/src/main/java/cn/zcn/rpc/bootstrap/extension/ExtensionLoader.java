package cn.zcn.rpc.bootstrap.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 扩展加载器。该类会加载定义在 {@link ExtensionLoader#EXTENSIONS_DIR} 文件下中的扩展文件。
 *
 * 使用 {@link ExtensionPoint} 定义扩展点。
 * {@code
 *  @ExtensionPoint
 *  public interface Flyable{
 *      public void fly();
 *  }
 * }
 *
 * 使用 {@link Extension} 定义扩展，并实现对应的接口。
 * {@code
 *  @Extension("impl1")
 *  public FlyableImpl implement Flyable{
 *       public void fly(){
 *     }
 *  }
 * }
 *
 * 使用配置文件暴露扩展。文件名必须为接口全类名。e.g. cn.zcn.rpc.bootstrap.register.Registry。
 * 文件内容如下：
 *   cn.zcn.rpc.bootstrap.register.ARegister
 *   cn.zcn.rpc.bootstrap.register.BRegister
 *   cn.zcn.rpc.bootstrap.register.CRegister
 * </pre>
 */
public class ExtensionLoader<T> {

    private static class InstanceHolder {
        private volatile Object instance;
    }

    private final static String EXTENSIONS_DIR = "META-INF/z-rpc/";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked"})
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> clazz) {
        if (clazz == null) {
            throw new ExtensionException("ExtensionPoint class should not be null.");
        }

        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new ExtensionException("ExtensionPoint class " + clazz.getName() + " should be interface or abstract class.");
        }

        ExtensionPoint point = clazz.getDeclaredAnnotation(ExtensionPoint.class);
        if (point == null) {
            throw new ExtensionException("ExtensionPoint class must be annotated by " + ExtensionPoint.class.getSimpleName());
        }

        ExtensionLoader<S> loader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(clazz);
        if (loader == null) {
            loader = new ExtensionLoader<>(clazz);
            ExtensionLoader<S> exist = (ExtensionLoader<S>) EXTENSION_LOADERS.putIfAbsent(clazz, loader);
            if (exist != null) {
                loader = exist;
            }
        }
        return loader;
    }

    private final Class<T> type;
    private final Map<String, InstanceHolder> cachedInstances = new ConcurrentHashMap<>();

    private volatile Map<String, Class<?>> cachedClasses = null;

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    public T getExtension(String name) {
        return getExtension(name, null, null);
    }

    @SuppressWarnings({"unchecked"})
    public T getExtension(String name, Class<?>[] argType, Object[] args) {
        if (name == null) {
            throw new ExtensionException("Extension name should not be null.");
        }

        if ((argType == null && args != null)
                || (argType != null && args == null)
                || (argType != null && argType.length != args.length)) {
            throw new ExtensionException("Length of argType and length of args must be equals.");
        }

        if (cachedClasses == null) {
            synchronized (ExtensionLoader.class) {
                if (cachedClasses == null) {
                    cachedClasses = loadClasses();
                }
            }
        }

        InstanceHolder holder = getOrCreateHolder(name);
        if (holder.instance == null) {
            synchronized (holder) {
                if (holder.instance == null) {
                    createExtension(holder, name, argType, args);
                }
            }
        }

        return (T) holder.instance;
    }

    private InstanceHolder getOrCreateHolder(String name) {
        InstanceHolder holder = cachedInstances.get(name);
        if (holder == null) {
            holder = new InstanceHolder();
            InstanceHolder exist = cachedInstances.putIfAbsent(name, holder);
            if (exist != null) {
                holder = exist;
            }
        }
        return holder;
    }

    private void createExtension(InstanceHolder holder, String name, Class<?>[] argType, Object[] args) {
        Class<?> klass = cachedClasses.get(name);
        if (klass == null) {
            throw new ExtensionException("Extension can not be found. Extension point:{0}, Extension name:{1}", type.getName(), name);
        }

        try {
            if (argType == null || argType.length == 0) {
                holder.instance = klass.newInstance();
            } else {
                Constructor<?> constructor = klass.getConstructor(argType);
                holder.instance = constructor.newInstance(args);
            }
        } catch (Exception e) {
            throw new ExtensionException(e, "Error occurred when create extension instance");
        }
    }

    /**
     * 获取扩展点的所有扩展Class
     */
    private Map<String, Class<?>> loadClasses() {
        Map<String, Class<?>> classes = new HashMap<>();

        String fileName = EXTENSIONS_DIR + type.getName();
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();

        try {
            Enumeration<URL> urls = classLoader.getResources(fileName);
            while (urls.hasMoreElements()) {
                classes.putAll(loadResource(urls.nextElement()));
            }
        } catch (IOException e) {
            throw new ExtensionException("Error occur when analyse extension file. File:{0}", fileName);
        }

        return classes;
    }

    private Map<String, Class<?>> loadResource(URL url) throws IOException {
        Map<String, Class<?>> classes = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String className;
            while ((className = reader.readLine()) != null) {
                className = className.trim();
                if (className.isEmpty()) {
                    continue;
                }

                Class<?> klass = Class.forName(className);
                if (!type.isAssignableFrom(klass)) {
                    throw new ExtensionException("Extension class is not subtype of interface. Extension Class:{0}, Interface:{1}", className, type.getName());
                }

                //是否有 @Extension 注解
                Extension anno = klass.getDeclaredAnnotation(Extension.class);
                if (anno == null) {
                    throw new ExtensionException("Extension class {0} must be annotated by {1}", className, Extension.class.getSimpleName());
                }

                classes.put(anno.value(), klass);
            }
            return classes;
        } catch (ClassNotFoundException e) {
            throw new ExtensionException(e, e.getMessage());
        }
    }
}
