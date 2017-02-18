package bear.panda.zeddy.randomalarm;

public enum Day {
  MONDAY("Mon"),
  TUESDAY("Tue"),
  WEDNESDAY("Wed"),
  THURSDAY("Thu"),
  FRIDAY("Fri"),
  SATURDAY("Sat"),
  SUNDAY("Sun");

  private String abbr;

  Day(String _abbr) {
    abbr = _abbr;
  }

  public String getAbbr() {
    return abbr;
  }
}
