package devices;

import io.vertx.core.json.JsonObject;
import sensors.Sensor;

public interface Device {
  String getName();
  String getLocation();
  String getCategory();
  void addSensor(Sensor s);
  void setLocation(String loc);
  JsonObject jsonValue();
}
