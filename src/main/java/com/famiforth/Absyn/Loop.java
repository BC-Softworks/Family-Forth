
package com.famiforth.Absyn;

public class Loop extends Expr {
  public final Statement statement_;

  public Loop(Statement p1) {
    statement_ = p1;
  }

  public <R, A> R accept(com.famiforth.Absyn.Expr.Visitor<R, A> v, A arg) {
    return v.visit(this, arg);
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o instanceof com.famiforth.Absyn.Loop) {
      com.famiforth.Absyn.Loop x = (com.famiforth.Absyn.Loop) o;
      return this.statement_.equals(x.statement_);
    }
    return false;
  }

  public int hashCode() {
    return this.statement_.hashCode();
  }

}
