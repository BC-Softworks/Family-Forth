package java.famicompiler.Absyn;

public class Expression  extends Statement {
  public final Expr expr_;
  public Expression(Expr p1) { expr_ = p1; }

  public <R,A> R accept(java.famicompiler.Absyn.Statement.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof java.famicompiler.Absyn.Expression) {
      java.famicompiler.Absyn.Expression x = (java.famicompiler.Absyn.Expression)o;
      return this.expr_.equals(x.expr_);
    }
    return false;
  }

  public int hashCode() {
    return this.expr_.hashCode();
  }


}
