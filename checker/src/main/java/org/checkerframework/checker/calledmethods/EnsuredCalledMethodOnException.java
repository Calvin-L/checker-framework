package org.checkerframework.checker.calledmethods;

import java.util.Objects;

class EnsuredCalledMethodOnException {

  public final String expression;
  public final String method;

  public EnsuredCalledMethodOnException(String expression, String method) {
    this.expression = expression;
    this.method = method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EnsuredCalledMethodOnException)) return false;
    EnsuredCalledMethodOnException that = (EnsuredCalledMethodOnException) o;
    return expression.equals(that.expression) && method.equals(that.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expression, method);
  }
}
