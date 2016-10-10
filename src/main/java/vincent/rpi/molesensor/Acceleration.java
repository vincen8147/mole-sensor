package vincent.rpi.molesensor;

import static java.lang.Math.sqrt;

class Acceleration {
    int x;
    int y;
    int z;

    public double getMagnitude() {
        return sqrt(x * x + y * y + z * z);
    }
}
