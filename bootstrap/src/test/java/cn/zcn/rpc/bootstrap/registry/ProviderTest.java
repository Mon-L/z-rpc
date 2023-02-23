package cn.zcn.rpc.bootstrap.registry;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author zicung
 */
public class ProviderTest {

    @Test
    public void testParseProvider() {
        Provider provider = Provider.parseProvider("127.0.0.1:8888/a.b.c?a=b&c=df");
        assertThat(provider.getIp()).isEqualTo("127.0.0.1");
        assertThat(provider.getPort()).isEqualTo(8888);
        assertThat(provider.getService()).isEqualTo("a.b.c");
        assertThat(provider.getAdditionalParameters().size()).isEqualTo(2);
        assertThat(provider.getAdditionalParameter("a")).isEqualTo("b");
        assertThat(provider.getAdditionalParameter("c")).isEqualTo("df");

        provider = Provider.parseProvider("127.0.0.2:8899/a.b.c");
        assertThat(provider.getIp()).isEqualTo("127.0.0.2");
        assertThat(provider.getPort()).isEqualTo(8899);
        assertThat(provider.getService()).isEqualTo("a.b.c");
        assertThat(provider.getAdditionalParameters().size()).isEqualTo(0);
    }

    @Test
    public void testParseProviderThenException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> Provider.parseProvider("127.0.0.1"))
            .withMessage("Invalid provider url");
    }

    @Test
    public void testToUrl() {
        Provider provider = new Provider();
        provider.setIp("1.2.3.4");
        provider.setPort(9878);
        provider.setWeight(5);
        provider.setWarmup(1000);
        provider.setService("a.b.c");
        provider.setStartTime(987456);

        String url = provider.toUrl();
        assertThat(url).isEqualTo("1.2.3.4:9878/a.b.c?weight=5&warmup=1000&start-time=987456");

        provider.putAdditionalParameter("abc", "dec");
        url = provider.toUrl();
        assertThat(url).isEqualTo("1.2.3.4:9878/a.b.c?weight=5&warmup=1000&start-time=987456&abc=dec");
    }
}
