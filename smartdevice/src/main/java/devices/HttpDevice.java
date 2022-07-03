package devices;

import communications.Http;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sensors.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HttpDevice extends Http implements Device {


  private String name;
  private String category;
  private String location;
  private List<Sensor> sensors;

  public HttpDevice(Vertx vertx, String name)
  {
    super(vertx);
    this.name = name;
    this.category = "HTTP";
    this.sensors = new ArrayList<>();
  }

  public HttpDevice(Vertx vertx, String name, String loc)
  {
    super(vertx);
    this.name = name;
    this.category = "HTTP";
    this.sensors = new ArrayList<>();
    this.location = loc;
  }

  public Future<Void> startServer(int port, String host, String root)
  {
    return super.startServer(port, host, root, this::jsonValue);
  }

  public Future<Void> register(int port, String host, String root, String token)
  {
    JsonObject information = new JsonObject()
      .put("name", this.getName())
      .put("category", this.getCategory())
      .put("location", this.getLocation());
    return super.register(port, host, root, token, information);
  }

  public int getPort()
  {
    return super.getServerPort();
  }

  public String getHostName()
  {
    return super.getServerHostName();
  }

  public String getRoot()
  {
    return super.getServerRoot();
  }


  public String getName()
  {
    return name;
  }

  public String getLocation()
  {
    return location;
  }


  public String getCategory()
  {
    return category;
  }
   public void addSensor(Sensor s)
  {
    sensors.add(s);
  }



  @Override
  public JsonObject jsonValue() {
    return new JsonObject()
      .put("name", getName())
      .put("location", getLocation())
      .put("category", getCategory())
      .put("sensors", sensors.stream().map(Sensor::jsonValue).collect(Collectors.toList()));
  }



  public void setLocation(String loc)
  {
    this.location = loc;
  }
  public void close()
  {
    super.close();
  }
}
