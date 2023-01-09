package cn.zcn.rpc.remoting.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ProtocolCodeTest {

    @Test
    public void testFrom() {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 3, (byte) 2);

        assertThat(protocolCode.getCode()).isEqualTo((byte) 3);
        assertThat(protocolCode.getVersion()).isEqualTo((byte) 2);
        assertThat(protocolCode.toString()).isEqualTo("Protocol{code=3,version=2}");
    }
}
