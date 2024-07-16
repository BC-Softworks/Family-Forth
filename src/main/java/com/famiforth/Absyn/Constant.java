package java.famicompiler.Absyn;

import java.io.Serializable;

public abstract class Constant implements Serializable {
  public abstract <R,A> R accept(Constant.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(java.famicompiler.Absyn.Numeral p, A arg);

  }

}
