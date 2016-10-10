package vincent.rpi.molesensor;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class Adxl345Reader {

    private I2CDevice device;

    public Adxl345Reader() {
    }

    public void initDevice() throws Exception {
        // Create I2C bus
        I2CBus Bus = I2CFactory.getInstance(I2CBus.BUS_1);
        // Get I2C device, device I2C address is 0x53(83)
        device = Bus.getDevice(0x53);

        // Select Bandwidth rate register
        // Normal mode, Output data rate = 100 Hz
        device.write(0x2C, (byte) 0x0A);
        // Select Power control register
        // Auto-sleep disable
        device.write(0x2D, (byte) 0x08);
        // Select Data format register
        // Self test disabled, 4-wire interface, Full resolution, range = +/-2g
        device.write(0x31, (byte) 0x08);
        Thread.sleep(500);
    }

    public synchronized Acceleration readState() throws IOException {
        // Read 6 bytes of data
        // xAccl lsb, xAccl msb, yAccl lsb, yAccl msb, zAccl lsb, zAccl msb
        byte[] data = new byte[6];
        data[0] = (byte) device.read(0x32);
        data[1] = (byte) device.read(0x33);
        data[2] = (byte) device.read(0x34);
        data[3] = (byte) device.read(0x35);
        data[4] = (byte) device.read(0x36);
        data[5] = (byte) device.read(0x37);

        Acceleration a = new Acceleration();

        a.x = (data[1]) << 8 | (data[0] & 0xFF);
        a.y = (data[3]) << 8 | (data[2] & 0xFF);
        a.z = (data[5]) << 8 | (data[4] & 0xFF);

        if (a.x > 511) {
            a.x -= 1024;
        }
        if (a.y > 511) {
            a.y -= 1024;
        }
        if (a.z > 511) {
            a.z -= 1024;
        }
        return a;
    }

}
