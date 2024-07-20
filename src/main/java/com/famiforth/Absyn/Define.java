package com.famiforth.Absyn;

public class Define extends Expr {
  public final Word word_;
  public final Statement statement_;

  public Define(Word p1, Statement p2) {
    word_ = p1;
    statement_ = p2;
  }

  public <R, A> R accept(Expr.Visitor<R, A> v, A arg) {
    return v.visit(this, arg);
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o instanceof com.famiforth.Absyn.Define) {
      Define x = (Define) o;
      return this.word_.equals(x.word_) && this.statement_.equals(x.statement_);
    }
    return false;
  }

  public int hashCode() {
    return 37 * (this.word_.hashCode()) + this.statement_.hashCode();
  }

}
