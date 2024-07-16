
package java.com.famiforth.Absyn;

import java.io.Serializable;

public abstract class Expr implements Serializable {
  public abstract <R,A> R accept(Expr.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(Conditional p, A arg);
    public R visit(Loop p, A arg);
    public R visit(Define p, A arg);

  }

}
