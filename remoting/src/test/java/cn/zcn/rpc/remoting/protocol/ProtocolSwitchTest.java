package cn.zcn.rpc.remoting.protocol;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Assert;
import org.junit.Test;

public class ProtocolSwitchTest {

    @Test
    public void testParseWithInvalidByte() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                ProtocolSwitch.parse((byte) -1);
            }
        });

        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                ProtocolSwitch.parse((byte) 128);
            }
        });
    }

    @Test
    public void testTurnOn() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);

        try {
            protocolSwitch.turnOff(-1);
            Assert.fail("Should not reach here!");
        } catch (IllegalArgumentException ignored) {
        }

        protocolSwitch.turnOn(0);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 1);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(1);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 2);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(2);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 4);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(3);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 8);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(4);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 16);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(5);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 32);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(6);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 64);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        for (int i = 0; i < 7; i++) {
            protocolSwitch.turnOn(i);
        }
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 127);

        try {
            protocolSwitch.turnOff(7);
            Assert.fail("Should not reach here!");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testTurnOff() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 127);

        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolSwitch.turnOff(-1);
            }
        });

        protocolSwitch.turnOff(0);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 126);

        protocolSwitch.turnOff(1);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 124);

        protocolSwitch.turnOff(2);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 120);

        protocolSwitch.turnOff(3);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 112);

        protocolSwitch.turnOff(4);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 96);

        protocolSwitch.turnOff(5);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 64);

        protocolSwitch.turnOff(6);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 0);

        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolSwitch.turnOff(7);
            }
        });
    }

    @Test
    public void testIsOn() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 127);

        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolSwitch.isOn(-1);
            }
        });

        for (int i = 0; i < 7; i++) {
            Assertions.assertThat(protocolSwitch.isOn(i)).isTrue();
            protocolSwitch.turnOff(i);
            Assertions.assertThat(protocolSwitch.isOn(i)).isFalse();
        }

        Assertions.assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                protocolSwitch.isOn(7);
            }
        });
    }

    @Test
    public void testToByte() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 0);

        protocolSwitch = ProtocolSwitch.parse((byte) 1);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 1);

        protocolSwitch = ProtocolSwitch.parse((byte) 2);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 2);

        protocolSwitch = ProtocolSwitch.parse((byte) 4);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 4);

        protocolSwitch = ProtocolSwitch.parse((byte) 8);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 8);

        protocolSwitch = ProtocolSwitch.parse((byte) 16);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 16);

        protocolSwitch = ProtocolSwitch.parse((byte) 32);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 32);

        protocolSwitch = ProtocolSwitch.parse((byte) 64);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 64);

        protocolSwitch = ProtocolSwitch.parse((byte) 127);
        Assertions.assertThat(protocolSwitch.toByte()).isEqualTo((byte) 127);
    }
}