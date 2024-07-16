package java.famicompiler.Absyn;

public class Conditional  extends Expr {
  public final Statement statement_;
  public Conditional(Statement p1) { statement_ = p1; }

  public <R,A> R accept(java.famicompiler.Absyn.Expr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof java.famicompiler.Absyn.Conditional) {
      java.famicompiler.Absyn.Conditional x = (java.famicompiler.Absyn.Conditional)o;
      return this.statement_.equals(x.statement_);
    }
    return false;
  }

  public int hashCode() {
    return this.statement_.hashCode();
  }

}
