package java.com.famiforth.Absyn;

public class Numeral extends Constant {
  public final Integer integer_;
  public Numeral(Integer p1) { integer_ = p1; }

  public <R,A> R accept(java.com.famiforth.Absyn.Constant.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof java.com.famiforth.Absyn.Numeral) {
      java.com.famiforth.Absyn.Numeral x = (java.com.famiforth.Absyn.Numeral)o;
      return this.integer_.equals(x.integer_);
    }
    return false;
  }

  public int hashCode() {
    return this.integer_.hashCode();
  }


}
