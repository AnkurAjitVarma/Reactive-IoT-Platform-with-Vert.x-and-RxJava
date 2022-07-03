package discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.Optional;

public class DiscoveryManager {

  private final Vertx vertx;
  private final ServiceDiscovery discovery;

  public DiscoveryManager(Vertx vertx) {
    this.vertx=vertx;
    var redisHost = Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("localhost");
    var redisPort = Optional.ofNullable(System.getenv("REDIS_PORT")).orElse("6379");
    this.discovery=ServiceDiscovery.create(this.vertx,
                                            new ServiceDiscoveryOptions()
                                              .setBackendConfiguration(
                                                new JsonObject()
                                                  .put("connectionString", "redis://"+redisHost+":"+redisPort)
                                                  .put("key", "records")
                                              ));
  }


  public ServiceDiscovery get() {
    return discovery;
  }

  public void close()
  {
    discovery.close();
  }
}
