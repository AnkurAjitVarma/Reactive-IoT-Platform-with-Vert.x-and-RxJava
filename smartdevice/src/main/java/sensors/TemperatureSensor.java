package sensors;

public class TemperatureSensor implements Sensor {

  private String name="temperature";
  private String unit="Celsius";

  @Override
  public double getLevel(int t) {
    double minTemperature = -3.0;
    double maxTemperature = 25.0;
    return simulate(t,minTemperature, maxTemperature);
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
