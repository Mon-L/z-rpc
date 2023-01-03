package cn.zcn.rpc.bootstrap.extension;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExtensionLoaderTest {

    @Test
    public void testInvalidExtension() {
        assertThatThrownBy(() -> ExtensionLoader.getExtensionLoader(null)).isInstanceOf(ExtensionException.class);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                //ExtensionLoaderTest 不是接口
                ExtensionLoader.getExtensionLoader(ExtensionLoaderTest.class);
            }
        }).isInstanceOf(ExtensionException.class);

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                //Runnable 没有 @ExtensionPoint 注解
                ExtensionLoader.getExtensionLoader(Runnable.class);
            }
        }).isInstanceOf(ExtensionException.class);

        ExtensionLoader<FlyableNotInExtensionFile> ext = ExtensionLoader.getExtensionLoader(FlyableNotInExtensionFile.class);
        assertThat(ext).isNotNull();
    }

    @Test
    public void testNotInExtensionFile() {
        ExtensionLoader<FlyableNotInExtensionFile> ext = ExtensionLoader.getExtensionLoader(FlyableNotInExtensionFile.class);
        assertThat(ext).isNotNull();

        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                ext.getExtension("foo");
            }
        }).isInstanceOf(ExtensionException.class);
    }

    @Test
    public void testGetExtensionWithInterfaceThenSuccess() {
        ExtensionLoader<Flyable> ext = ExtensionLoader.getExtensionLoader(Flyable.class);
        assertThat(ext).isNotNull();

        Flyable instance = ext.getExtension("foo");
        assertThat(instance).isNotNull();
        assertThat(Flyable.class.isAssignableFrom(instance.getClass())).isTrue();
    }

    @Test
    public void testGetExtensionWithAbstractClassThenSuccess() {
        ExtensionLoader<AbstractRegistry> ext = ExtensionLoader.getExtensionLoader(AbstractRegistry.class);
        assertThat(ext).isNotNull();

        AbstractRegistry instance = ext.getExtension("foo", new Class<?>[]{int.class}, new Object[]{1});
        assertThat(instance).isNotNull();
        assertThat(AbstractRegistry.class.isAssignableFrom(instance.getClass())).isTrue();
    }

    @Test
    public void testWithoutExtensionAnnotation() {
        ExtensionLoader<Flyable2> ext = ExtensionLoader.getExtensionLoader(Flyable2.class);
        assertThat(ext).isNotNull();

        assertThatThrownBy(() -> ext.getExtension("foo")).isInstanceOf(ExtensionException.class);
    }

    @Test
    public void testWithoutImplementSpecifiedInterface() {
        ExtensionLoader<Flyable3> ext = ExtensionLoader.getExtensionLoader(Flyable3.class);
        assertThat(ext).isNotNull();

        assertThatThrownBy(() -> ext.getExtension("foo")).isInstanceOf(ExtensionException.class);
    }

    @ExtensionPoint
    public interface FlyableNotInExtensionFile {
        void fly();
    }

    @ExtensionPoint
    public interface Flyable {
        void fly();
    }

    @Extension("foo")
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

    @ExtensionPoint
    public abstract static class AbstractRegistry {
        public AbstractRegistry(int i) {

        }
    }

    @Extension("foo")
    public static class FooRegistry extends AbstractRegistry {

        public FooRegistry(int i) {
            super(i);
        }
    }
}
