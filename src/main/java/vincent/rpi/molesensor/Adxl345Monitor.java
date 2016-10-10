package vincent.rpi.molesensor;

import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Adxl345Monitor {

    public static void main(String[] args) throws Exception  {
        Adxl345Monitor monitor = new Adxl345Monitor();
        EventHandler eventHandler = monitor.buildEventHandler();
        Adxl345Reader reader = new Adxl345Reader();
        reader.initDevice();

        if (args.length != 4) {
            System.err.println("Usage: Adxl345Monitor [sensitivity] [frequency] [triggerWindow] [triggerThreshold]");
            System.err.println("Example: java -jar target/mole-sensor-1.0-SNAPSHOT.jar 50 1000 30000 3");
        }

        double sensitivity = Double.valueOf(args[0]);
        long frequency = Long.parseLong(args[1]);
        long triggerWindow = Long.parseLong(args[2]);
        int triggerThreshold = Integer.parseInt(args[3]);
        Acceleration initialState = reader.readState();
        TreeSet<Long> triggerTimes = new TreeSet<>();
        while (true) {
            Thread.sleep(frequency);
            Acceleration state = reader.readState();
            double deltaX = Math.abs(state.x - initialState.x);
            double deltaY = Math.abs(state.y - initialState.y);
            double deltaZ = Math.abs(state.z - initialState.z);
            System.out.printf("current = %d\t%d\t%d \t\tstart = %d\t%d\t%d\n",
                    state.x, state.y, state.z, initialState.x, initialState.y, initialState.z);

            if (deltaX > sensitivity || deltaY > sensitivity || deltaZ > sensitivity) {
                long now = System.currentTimeMillis();
                triggerTimes.add(now);
                System.out.println("Movement detected: Trigger count= " + triggerTimes.size());
                triggerTimes.removeIf(triggerTime -> now - triggerTime > triggerWindow);
            }
            if (triggerTimes.size() > triggerThreshold) {
                initialState = state;
                System.out.println("ITS CLOBBER'N TIME");
                eventHandler.handleEvent();
                triggerTimes.clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public EventHandler buildEventHandler() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> config =
                mapper.readValue(getClass().getClassLoader().getResource("configuration.json"), Map.class);
        String handlerClass = (String) config.get("handler");
        EventHandler eventHandler = (EventHandler) Class.forName(handlerClass).newInstance();
        eventHandler.configure((Map<String, String>) config.get("configuration"));
        return eventHandler;
    }
}
