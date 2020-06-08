package com.test;

public class GpsData {
  long id;
  String terminalId;
  String dataTime;
  double lon;
  double lat;
  float speed;
  int altitude;
  int locType;
  int gpsStatus;
  float direction;
  int satellite;
  public long getId() { return id; }
  public void setId(long id) { this.id = id; }
  public String getTerminalId() { return terminalId; }
  public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
  public String getDataTime() { return dataTime; }
  public void setDataTime(String dataTime) { this.dataTime = dataTime; }
  public double getLon() { return lon; }
  public void setLon(double lon) { this.lon = lon; }
  public double getLat() { return lat; }
  public void setLat(double lat) { this.lat = lat; }
  public float getSpeed() { return speed; }
  public void setSpeed(float speed) { this.speed = speed; }
  public int getAltitude() { return altitude; }
  public void setAltitude(int altitude) { this.altitude = altitude; }
  public int getLocType() { return locType; }
  public void setLocType(int locType) { this.locType = locType; }
  public int getGpsStatus() { return gpsStatus; }
  public void setGpsStatus(int gpsStatus) { this.gpsStatus = gpsStatus; }
  public float getDirection() { return direction; }
  public void setDirection(float direction) { this.direction = direction; }
  public int getSatellite() { return satellite; }
  public void setSatellite(int satellite) { this.satellite = satellite; }
  @Override
  public String toString() {
    return "GpsData [altitude=" + altitude + ", dataTime=" + dataTime +
        ", direction=" + direction + ", gpsStatus=" + gpsStatus + ", id=" + id +
        ", lat=" + lat + ", locType=" + locType + ", lon=" + lon +
        ", satellite=" + satellite + ", speed=" + speed +
        ", terminalId=" + terminalId + "]";
  }
}