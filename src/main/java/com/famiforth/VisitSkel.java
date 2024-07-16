package java.com.famiforth;

import java.com.famiforth.Absyn.Program;
import java.com.famiforth.Absyn.Statement;
import java.com.famiforth.Absyn.EntryPoint.Visitor;

/*** Visitor Design Pattern Skeleton. ***/

/*
 * This implements the common visitor design pattern.
 * Replace the R and A parameters with the desired return
 * and context types.
 */

public class VisitSkel {
  public class PrgVisitor<R, A> implements Visitor<R, A> {
    public R visit(Program p, A arg) {
      /* Code for Program goes here */
      for (Statement x : p.liststatement_) {
        x.accept(new StatementVisitor<R, A>(), arg);
      }
      return null;
    }
  }

  public class StatementVisitor<R, A> implements java.com.famiforth.Absyn.Statement.Visitor<R, A> {
    public R visit(java.com.famiforth.Absyn.Literal p, A arg) {
      /* Code for Literal goes here */
      p.constant_.accept(new ConstantVisitor<R, A>(), arg);
      return null;
    }

    public R visit(java.com.famiforth.Absyn.Expression p, A arg) {
      /* Code for Expression goes here */
      p.expr_.accept(new ExprVisitor<R, A>(), arg);
      return null;
    }

    public R visit(java.com.famiforth.Absyn.SinlgeWord p, A arg) {
      /* Code for SinlgeWord goes here */
      p.word_.accept(new WordVisitor<R, A>(), arg);
      return null;
    }
  }

  public class ExprVisitor<R, A> implements java.com.famiforth.Absyn.Expr.Visitor<R, A> {
    public R visit(java.com.famiforth.Absyn.Conditional p, A arg) {
      /* Code for Conditional goes here */
      p.statement_.accept(new StatementVisitor<R, A>(), arg);
      return null;
    }

    public R visit(java.com.famiforth.Absyn.Loop p, A arg) {
      /* Code for Loop goes here */
      p.statement_.accept(new StatementVisitor<R, A>(), arg);
      return null;
    }

    public R visit(java.com.famiforth.Absyn.Define p, A arg) {
      /* Code for Define goes here */
      p.word_.accept(new WordVisitor<R, A>(), arg);
      p.statement_.accept(new StatementVisitor<R, A>(), arg);
      return null;
    }
  }

  public class WordVisitor<R, A> implements java.com.famiforth.Absyn.Word.Visitor<R, A> {
    public R visit(java.com.famiforth.Absyn.Name p, A arg) {
      /* Code for Name goes here */
      // p.forthword_;
      return null;
    }
  }

  public class ConstantVisitor<R, A> implements java.com.famiforth.Absyn.Constant.Visitor<R, A> {
    public R visit(java.com.famiforth.Absyn.Numeral p, A arg) {
      /* Code for Number goes here */
      // p.integer_;
      return null;
    }
  }
}
