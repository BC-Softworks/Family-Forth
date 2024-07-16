

package java.com.famiforth.Absyn;

public class SinlgeWord  extends Statement {
  public final Word word_;
  public SinlgeWord(Word p1) { word_ = p1; }

  public <R,A> R accept(java.com.famiforth.Absyn.Statement.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof java.com.famiforth.Absyn.SinlgeWord) {
      java.com.famiforth.Absyn.SinlgeWord x = (java.com.famiforth.Absyn.SinlgeWord)o;
      return this.word_.equals(x.word_);
    }
    return false;
  }

  public int hashCode() {
    return this.word_.hashCode();
  }


}
