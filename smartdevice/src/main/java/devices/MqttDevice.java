package devices;

import communications.Mqtt;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sensors.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MqttDevice extends Mqtt implements Device{

  private String name;
  private String category;
  private String location;
  private List<Sensor> sensors;



  public MqttDevice(Vertx vertx, String nme, int port, String host)
  {
    super(vertx, port, host, nme);
    this.name = nme;
    this.sensors = new ArrayList<>();
    this.category="MQTT";
  }

  public MqttDevice(Vertx vertx, String nme, int port, String host, String loc)
  {
    super(vertx, port, host, nme);
    this.name = nme;
    this.sensors = new ArrayList<>();
    this.category="MQTT";
    this.location = loc;
  }




  public void sendData(long l)
  {
    super.publish(jsonValue());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public String getCategory() {
    return category;
  }


  @Override
  public void addSensor(Sensor s) {
    sensors.add(s);
  }


  @Override
  public void setLocation(String loc) {
    this.location = loc;
  }

  @Override
  public JsonObject jsonValue() {
    return new JsonObject()
      .put("name", getName())
      .put("location", getLocation())
      .put("category", getCategory())
      .put("sensors", sensors.stream().map(Sensor::jsonValue).collect(Collectors.toList()));

  }

  public void close()
  {
    super.close();
  }
}
