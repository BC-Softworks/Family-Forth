package com.famiforth.Absyn;

public abstract class EntryPoint implements java.io.Serializable {
  public abstract <R,A> R accept(EntryPoint.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.famiforth.Absyn.Program p, A arg);

  }

}
