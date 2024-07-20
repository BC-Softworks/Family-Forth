package com.famiforth.Absyn;

public class Name  extends Word {
  public final String forthword_;
  public Name(String p1) { forthword_ = p1; }

  public <R,A> R accept(com.famiforth.Absyn.Word.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof com.famiforth.Absyn.Name) {
      com.famiforth.Absyn.Name x = (com.famiforth.Absyn.Name)o;
      return this.forthword_.equals(x.forthword_);
    }
    return false;
  }

  public int hashCode() {
    return this.forthword_.hashCode();
  }


}
