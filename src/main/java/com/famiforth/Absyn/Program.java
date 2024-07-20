package com.famiforth.Absyn;

import java.util.List;

public class Program  extends EntryPoint {
  public final List<Statement> liststatement_;
  public Program(List<Statement> p1) { liststatement_ = p1; }

  public <R,A> R accept(Visitor<R,A> v, A arg) { 
    return v.visit(this, arg); 
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Program) {
      Program x = (Program)o;
      return this.liststatement_.equals(x.liststatement_);
    }
    return false;
  }

  public int hashCode() {
    return this.liststatement_.hashCode();
  }
}
