package me.maiome.openauth.util;

import me.maiome.openauth.util.LogHandler;

public class RegistryException extends Exception
{
  protected static String error;
  public LogHandler log = new LogHandler();

  public RegistryException() {
    error = "An unknown error occurred.";
    this.log.warning(error);
  }
  public RegistryException(String err) {
    super(err);
    error = err;
    this.log.warning(error);
  }
  public static String getError() {
    return error;
  }
}
