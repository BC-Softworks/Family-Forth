

package com.famiforth.Absyn;

public abstract class Word implements java.io.Serializable {
  public abstract <R,A> R accept(Word.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(com.famiforth.Absyn.Name p, A arg);

  }

}
