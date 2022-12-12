package cn.zcn.rpc.bootstrap.extension;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class ExtensionLoaderTest {

    @Test
    public void testInvalidExtension() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ExtensionLoader.getExtensionLoader(null);
            }
        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                //ExtensionLoaderTest 不是接口
                ExtensionLoader.getExtensionLoader(ExtensionLoaderTest.class);
            }
        });

        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                //Runnable 没有 @ExtensionPoint 注解
                ExtensionLoader.getExtensionLoader(Runnable.class);
            }
        });

        ExtensionLoader<FlyableNotInExtensionFile> ext = ExtensionLoader.getExtensionLoader(FlyableNotInExtensionFile.class);
        assertNotNull(ext);
    }

    @Test
    public void testNotInExtensionFile() {
        ExtensionLoader<FlyableNotInExtensionFile> ext = ExtensionLoader.getExtensionLoader(FlyableNotInExtensionFile.class);
        assertNotNull(ext);

        assertThrows(ExtensionException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ext.getExtension("foo");
            }
        });
    }

    @Test
    public void testGetExtensionThenSuccess() {
        ExtensionLoader<Flyable> ext = ExtensionLoader.getExtensionLoader(Flyable.class);
        assertNotNull(ext);

        Flyable instance = ext.getExtension("foo");
        assertNotNull(instance);
        assertTrue(Flyable.class.isAssignableFrom(instance.getClass()));
    }

    @Test
    public void testWithoutExtensionAnnotation() {
        ExtensionLoader<Flyable2> ext = ExtensionLoader.getExtensionLoader(Flyable2.class);
        assertNotNull(ext);

        assertThrows(ExtensionException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ext.getExtension("foo");
            }
        });
    }

    @Test
    public void testWithoutNoArgsConstructor() {
        ExtensionLoader<Flyable3> ext = ExtensionLoader.getExtensionLoader(Flyable3.class);
        assertNotNull(ext);

        assertThrows(ExtensionException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ext.getExtension("WithoutNoArgsConstructor");
            }
        });
    }

    @Test
    public void testWithoutImplementSpecifiedInterface() {
        ExtensionLoader<Flyable4> ext = ExtensionLoader.getExtensionLoader(Flyable4.class);
        assertNotNull(ext);

        assertThrows(ExtensionException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                ext.getExtension("foo");
            }
        });
    }

    @ExtensionPoint
    public interface FlyableNotInExtensionFile {
        void fly();
    }

    @ExtensionPoint
    public interface Flyable {
        void fly();
    }

    @Extension(name = "foo")
    public static class FlyImpl implements Flyable {
        @Override
        public void fly() {

        }
    }

    @ExtensionPoint
    public interface Flyable2 {
        void fly();
    }

    public static class FlyImplWithoutExtensionAnnotation implements Flyable2 {

        @Override
        public void fly() {

        }
    }

    @ExtensionPoint
    public interface Flyable3 {
        void fly();
    }

    @Extension(name = "WithoutNoArgsConstructor")
    public static class FlyImplWithoutNoArgsConstructor implements Flyable3 {

        public FlyImplWithoutNoArgsConstructor(int i) {

        }

        @Override
        public void fly() {

        }
    }

    @ExtensionPoint
    public interface Flyable4 {
        void fly();
    }
}
