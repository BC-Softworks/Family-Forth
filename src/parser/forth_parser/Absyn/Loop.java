// File generated by the BNF Converter (bnfc 2.9.5).

package forth_parser.Absyn;

public class Loop  extends Expr {
  public final Statement statement_;
  public Loop(Statement p1) { statement_ = p1; }

  public <R,A> R accept(forth_parser.Absyn.Expr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof forth_parser.Absyn.Loop) {
      forth_parser.Absyn.Loop x = (forth_parser.Absyn.Loop)o;
      return this.statement_.equals(x.statement_);
    }
    return false;
  }

  public int hashCode() {
    return this.statement_.hashCode();
  }


}
