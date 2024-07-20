package com.famiforth.Absyn;

import java.io.Serializable;

public abstract class Statement implements Serializable {
  public abstract <R,A> R accept(Statement.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.famiforth.Absyn.Literal p, A arg);
    public R visit(com.famiforth.Absyn.Expression p, A arg);
    public R visit(com.famiforth.Absyn.SinlgeWord p, A arg);

  }

}
