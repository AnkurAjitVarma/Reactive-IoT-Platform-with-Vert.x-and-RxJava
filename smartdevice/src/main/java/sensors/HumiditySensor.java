package sensors;

public class HumiditySensor implements Sensor {

  private String name="humidity";
  private String unit="%";

  @Override
  public double getLevel(int t) {
    double minHumidity = 40.0;
    double maxHumidity = 60.0;
    return simulate(t, minHumidity, maxHumidity);
  }

  @Override
  public String getUnit() {
    return unit;
  }

  @Override
  public String getName() {
    return name;
  }
}
