package http;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Registration {

  private final ServiceDiscovery discovery;

  public Registration(ServiceDiscovery discovery) {
    this.discovery = discovery;
  }

  private Optional<RegistrationData> extractData(JsonObject json)
  {
    if(json == null) return Optional.empty();
    RegistrationData data = new RegistrationData();
    String host = json.getString("host"); json.remove("host");
    String id = json.getString("id"); json.remove("id");
    String name = json.getString("name"); json.remove("name");
    if((name==null || host==null) && id==null) return Optional.empty();
    if(host!=null)
    {
      String root = Optional.ofNullable(json.getString("root")).orElse("/");
      try
      {
        int port = Optional.ofNullable(json.getInteger("port")).orElse(8080);
        data.setLocation(new HttpLocation().setHost(host).setRoot(root).setPort(port).setSsl(true).toJson());
      }
      catch (ClassCastException e)
      {
        int port = Integer.parseInt(Optional.ofNullable(json.getString("port")).orElse("8080"));
        data.setLocation(new HttpLocation().setHost(host).setRoot(root).setPort(port).setSsl(true).toJson());
      }
    }
    json.remove("port");
    json.remove("root");
    data.setId(id);
    data.setName(name);
    data.setMetadata(json);
    return Optional.of(data);
  }


  public void handle(RoutingContext context)
  {
    String token = context.request().getHeader("smart-token");
    if(token==null || !token.equals("smart.home"))
      context.response().end();
    else
      extractData(context.getBodyAsJson())
        .ifPresentOrElse(data ->
        {
          Supplier<Record> record = () ->
          {
            var location = new HttpLocation(data.getLocation());
            return HttpEndpoint.createRecord(data.getName(), location.isSsl(), location.getHost(), location.getPort(), location.getRoot(), data.getMetadata());
          };
          Function<Record, Future<Record>> isFound = r -> {
            if (r == null) return Future.failedFuture("Record not found.");
            else return Future.succeededFuture(r);
          };
          Function<Record, Future<Record>> foundHandler = r -> {
            if (data.getId().equals(r.getRegistration())) {
              if (data.getName() != null) r.setName(data.getName());
              if (data.getLocation() != null) r.setLocation(data.getLocation());
              if (!data.getMetadata().isEmpty()) r.setMetadata(data.getMetadata());
              return discovery.update(r);
            } else return Future.failedFuture("Another endpoint with the same URI exists.");
          };
          Function<Throwable, Future<Record>> notFoundHandler = e -> {
            return discovery.getRecord(r -> r.getRegistration().equals(data.getId()))
              .compose(isFound);
          };
          Function<Record, Boolean> filter = r -> {
            JsonObject record_location = r.getLocation(), location = data.getLocation();
            return record_location.getString("host").equals(location.getString("host"))
              && record_location.getInteger("port").equals(location.getInteger("port"))
              && record_location.getString("root").equals(location.getString("root"));
          };

          if(data.getId()!=null) {
            if (data.getLocation() != null)
              discovery.getRecord(filter)
                .compose(isFound)
                .recover(notFoundHandler)
                .compose(foundHandler)
                .onSuccess(r -> context.response().end("Update Successful"))
                .onFailure(e -> context.response().end(e.getMessage()));
            else
              discovery.getRecord(r -> r.getRegistration().equals(data.getId()))
                .compose(isFound)
                .compose(foundHandler)
                .onSuccess(r -> context.response().end("Update Successful"))
                .onFailure(e -> context.response().end(e.getMessage()));
          }
          else
            discovery.getRecord(filter)
              .compose(isFound)
              .compose(r -> Future.failedFuture("Another endpoint with the same URI exists."),
                e ->  discovery.publish(record.get()))
              .onSuccess(r -> context.json(new JsonObject().put("id", r.getRegistration())))
              .onFailure(e -> context.response().end(e.getMessage()));
      }, () -> context.response().end("Bad Request"));
  }
}
