package data;

import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.Map;

public class MongoStore {
  private static MongoClient mongoClient;

  public static void initialize(Vertx vertx, String connectionString, String dataBaseName) {
    // Create a MongoClient instance
    mongoClient = MongoClient.createShared(
      vertx,
      new JsonObject()
        .put("db_name",dataBaseName)
        .put("useObjectId", false)
        .put("connection_string", connectionString)
    );
  }

  public static Flowable<JsonObject> getLastDevicesMetricsFlowable(int howMany)
  {
    return mongoClient.findBatchWithOptions("devices", new JsonObject(), new FindOptions().setLimit(howMany).setSort(
      new JsonObject(Map.of("date", -1, "hour", -1))
    )).toFlowable();
  }

}
