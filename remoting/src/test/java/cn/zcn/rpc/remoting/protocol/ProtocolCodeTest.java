package cn.zcn.rpc.remoting.protocol;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ProtocolCodeTest {

    @Test
    public void testFrom() {
        ProtocolCode protocolCode = ProtocolCode.from((byte) 3, (byte) 2);

        Assertions.assertThat(protocolCode.getCode()).isEqualTo((byte) 3);
        Assertions.assertThat(protocolCode.getVersion()).isEqualTo((byte) 2);
        Assertions.assertThat(protocolCode.toString()).isEqualTo("Protocol{code=3,version=2}");
    }
}
