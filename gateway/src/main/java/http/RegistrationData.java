package http;

import io.vertx.core.json.JsonObject;


public class RegistrationData {
  private String name;
  private String id;
  private JsonObject location;
  private JsonObject metadata;

  public String getId() {
    return id;
  }

  public JsonObject getLocation() {
    return location;
  }




  public String getName()
  {
    return name;
  }



  public JsonObject getMetadata()
  {
    return metadata;
  }



  public void setName(String name)
  {
    this.name = name;
  }

  public void setMetadata(JsonObject metadata)
  {
    this.metadata = metadata;
  }



  public void setId(String id) {
    this.id = id;
  }

  public void setLocation(JsonObject location) {
    this.location = location;
  }


}
