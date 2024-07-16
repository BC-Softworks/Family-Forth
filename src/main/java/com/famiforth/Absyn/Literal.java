package java.com.famiforth.Absyn;

public class Literal extends Statement {
  public final Constant constant_;

  public Literal(Constant p1) {
    constant_ = p1;
  }

  public <R, A> R accept(Statement.Visitor<R, A> v, A arg) {
    return v.visit(this, arg);
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o instanceof Literal) {
      Literal x = (Literal) o;
      return this.constant_.equals(x.constant_);
    }
    return false;
  }

  public int hashCode() {
    return this.constant_.hashCode();
  }

}
