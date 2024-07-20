package com.famiforth.Absyn;

import java.io.Serializable;

public abstract class Constant implements Serializable {
  public abstract <R,A> R accept(Constant.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.famiforth.Absyn.Numeral p, A arg);

  }

}