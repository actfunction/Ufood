package com.rh.gw.gdjh.exception;

public abstract class AbstractNestedRuntimeException extends RuntimeException
{
  private static final long serialVersionUID = 7534751658054481518L;

  public AbstractNestedRuntimeException()
  {
  }
  

  public AbstractNestedRuntimeException(Throwable cause)
  {
    super(cause);
  }

  public AbstractNestedRuntimeException(String msg)
  {
    super(msg);
  }

  public AbstractNestedRuntimeException(String msg, Throwable cause)
  {
    super(msg, cause);
  }

  public Throwable getRootCause()
  {
    Throwable rootCause = null;
    Throwable cause = getCause();
    while ((cause != null) && (cause != rootCause)) {
      rootCause = cause;
      cause = cause.getCause();
    }
    return rootCause;
  }

  public Throwable getMostSpecificCause()
  {
    Throwable rootCause = getRootCause();
    return ((rootCause != null) ? rootCause : this);
  }

  public boolean contains(Class exType)
  {
    if (exType == null)
      return false;

    if (exType.isInstance(this))
      return true;

    Throwable cause = getCause();
    if (cause == this)
      return false;

    if (cause instanceof AbstractNestedRuntimeException) {
      return ((AbstractNestedRuntimeException)cause).contains(exType);
    }

    while (cause != null) {
      if (exType.isInstance(cause))
        return true;

      if (cause.getCause() == cause)
        break;

      cause = cause.getCause();
    }
    return false;
  }
}