package vincent.rpi.molesensor;

import java.util.Map;

public interface EventHandler {
    void handleEvent();

    void configure(Map<String, String> configuration);
}
