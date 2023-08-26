package de.hdawg.rankinginfo.model;

/**
 * Enum containing all German Tennis federations.
 */
public enum Federation {
  BAD("Baden"),
  BB("Berlin-Brandenburg"),
  BTV("Bayern"),
  HAM("Hamburg"),
  HTV("Hessen"),
  RPF("Rheinland-Pfalz"),
  SLH("Schleswig-Holstein"),
  STB("Saarland"),
  STV("Sachsen"),
  TMV("Mecklenburg-Vorpommern"),
  TNB("Niedersachsen-Bremen"),
  TSA("Sachsen-Anhalt"),
  TTV("Thüringen"),
  TVM("Mittelrhein"),
  TVN("Niederrhein"),
  WTB("Würtemberg"),
  WTV("Westfalen");

  private final String longName;

  private Federation(String longName) {
    this.longName = longName;
  }

  public String value() {
    return longName;
  }
}
