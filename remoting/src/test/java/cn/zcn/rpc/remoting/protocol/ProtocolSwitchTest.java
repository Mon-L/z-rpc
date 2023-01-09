package cn.zcn.rpc.remoting.protocol;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

public class ProtocolSwitchTest {

    @Test
    public void testParseWithInvalidByte() {
        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                ProtocolSwitch.parse((byte) -1);
            }
        });

        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {

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
            fail("Should not reach here!");
        } catch (IllegalArgumentException ignored) {
        }

        protocolSwitch.turnOn(0);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 1);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(1);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 2);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(2);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 4);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(3);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 8);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(4);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 16);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(5);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 32);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        protocolSwitch.turnOn(6);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 64);

        protocolSwitch = ProtocolSwitch.parse((byte) 0);
        for (int i = 0; i < 7; i++) {
            protocolSwitch.turnOn(i);
        }
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 127);

        try {
            protocolSwitch.turnOff(7);
            fail("Should not reach here!");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testTurnOff() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 127);

        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                protocolSwitch.turnOff(-1);
            }
        });

        protocolSwitch.turnOff(0);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 126);

        protocolSwitch.turnOff(1);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 124);

        protocolSwitch.turnOff(2);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 120);

        protocolSwitch.turnOff(3);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 112);

        protocolSwitch.turnOff(4);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 96);

        protocolSwitch.turnOff(5);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 64);

        protocolSwitch.turnOff(6);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 0);

        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                protocolSwitch.turnOff(7);
            }
        });
    }

    @Test
    public void testIsOn() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 127);

        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                protocolSwitch.isOn(-1);
            }
        });

        for (int i = 0; i < 7; i++) {
            assertThat(protocolSwitch.isOn(i)).isTrue();
            protocolSwitch.turnOff(i);
            assertThat(protocolSwitch.isOn(i)).isFalse();
        }

        assertThatIllegalArgumentException().isThrownBy(new ThrowableAssert.ThrowingCallable() {

            @Override
            public void call() throws Throwable {
                protocolSwitch.isOn(7);
            }
        });
    }

    @Test
    public void testToByte() {
        ProtocolSwitch protocolSwitch = ProtocolSwitch.parse((byte) 0);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 0);

        protocolSwitch = ProtocolSwitch.parse((byte) 1);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 1);

        protocolSwitch = ProtocolSwitch.parse((byte) 2);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 2);

        protocolSwitch = ProtocolSwitch.parse((byte) 4);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 4);

        protocolSwitch = ProtocolSwitch.parse((byte) 8);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 8);

        protocolSwitch = ProtocolSwitch.parse((byte) 16);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 16);

        protocolSwitch = ProtocolSwitch.parse((byte) 32);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 32);

        protocolSwitch = ProtocolSwitch.parse((byte) 64);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 64);

        protocolSwitch = ProtocolSwitch.parse((byte) 127);
        assertThat(protocolSwitch.toByte()).isEqualTo((byte) 127);
    }
}
