package sensors;

public class eCO2Sensor implements Sensor {


  private String name="eCO2";
  private String unit="ppm";

  @Override
  public double getLevel(int t) {
    double mineCO2 = 400.0d;
    double maxeCO2= 60_000.0d;
    return simulate(t,mineCO2, maxeCO2);
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
