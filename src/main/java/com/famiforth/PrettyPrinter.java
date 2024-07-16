package java.com.famiforth;

import java.com.famiforth.Absyn.Conditional;
import java.com.famiforth.Absyn.Constant;
import java.com.famiforth.Absyn.Define;
import java.com.famiforth.Absyn.EntryPoint;
import java.com.famiforth.Absyn.Expr;
import java.com.famiforth.Absyn.Expression;
import java.com.famiforth.Absyn.Literal;
import java.com.famiforth.Absyn.Loop;
import java.com.famiforth.Absyn.Name;
import java.com.famiforth.Absyn.Numeral;
import java.com.famiforth.Absyn.Program;
import java.com.famiforth.Absyn.SinlgeWord;
import java.com.famiforth.Absyn.Statement;
import java.com.famiforth.Absyn.Word;
import java.util.Iterator;
import java.util.List;

public class PrettyPrinter {
  private static final int INITIAL_BUFFER_SIZE = 256;
  private static final int INDENT_WIDTH = 2;
  private static final String _L_PAREN = "(";
  private static final String _R_PAREN = ")";

  private static void render(String s) {
    if (s.equals("")){
      return;
    }

    if (s.equals("{")) {
      onEmptyLine();
      builder.append(s);
      num += INDENT_WIDTH;
      builder.append("\n");
      indent();
    } else if (s.equals("(") || s.equals("["))
      builder.append(s);
    else if (s.equals(")") || s.equals("]")) {
      removeTrailingWhitespace();
      builder.append(s + " ");
    } else if (s.equals("}")) {
      num -= INDENT_WIDTH;
      onEmptyLine();
      builder.append(s + "\n");
      indent();
    } else if (s.equals(",")) {
      removeTrailingWhitespace();
      builder.append(s + " ");
    } else if (s.equals(";")) {
      removeTrailingWhitespace();
      builder.append(s + "\n");
      indent();
    } else if (s.trim().equals("")) {
      backup();
      builder.append(s);
    } else {
      builder.append(s + " ");
    }
  }

  // print and show methods are defined for each category.
  public static String print(EntryPoint token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(EntryPoint token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String print(List<Statement> token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(List<Statement> token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String print(Statement token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(Statement token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String print(Expr token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(Expr token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String print(Word token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(Word token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String print(Constant token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(Constant token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String print(List<Expr> token) {
    pp(token, 0);
    trim();
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  public static String show(List<Expr> token) {
    sh(token);
    String temp = builder.toString();
    builder.delete(0, builder.length());
    return temp;
  }

  /*** You shouldn't need to change anything beyond this point. ***/

  private static void pp(EntryPoint token, int _i_) {
    if (token instanceof Program) {
      Program _program = (Program) token;
      if (_i_ > 0)
        render(_L_PAREN);
      pp(_program.liststatement_, 0);
      if (_i_ > 0)
        render(_R_PAREN);
    }

  }

  private static void pp(List<Statement> token, int _i_) {
    ppListStatement(token.iterator(), _i_);
  }

  private static void ppListStatement(java.util.Iterator<Statement> it, int _i_) {
    if (it.hasNext()) {
      Statement el = it.next();
      if (!it.hasNext()) { /* last */
        pp(el, _i_);
      } else { /* cons */
        pp(el, _i_);
        ppListStatement(it, _i_);
      }
    }
  }

  private static void pp(Statement token, int _i_) {
    if (token instanceof Literal) {
      Literal _literal = (Literal) token;
      if (_i_ > 0)
        render(_L_PAREN);
      pp(_literal.constant_, 0);
      if (_i_ > 0)
        render(_R_PAREN);
    } else if (token instanceof Expression) {
      Expression _expression = (Expression) token;
      if (_i_ > 0)
        render(_L_PAREN);
      pp(_expression.expr_, 0);
      if (_i_ > 0)
        render(_R_PAREN);
    } else if (token instanceof SinlgeWord) {
      SinlgeWord _sinlgeword = (SinlgeWord) token;
      if (_i_ > 0)
        render(_L_PAREN);
      pp(_sinlgeword.word_, 0);
      if (_i_ > 0)
        render(_R_PAREN);
    }

  }

  private static void pp(Expr token, int _i_) {
    if (token instanceof Conditional) {
      Conditional _conditional = (Conditional) token;
      if (_i_ > 0)
        render(_L_PAREN);
      render("if");
      pp(_conditional.statement_, 0);
      render("then");
      if (_i_ > 0)
        render(_R_PAREN);
    } else if (token instanceof Loop) {
      Loop _loop = (Loop) token;
      if (_i_ > 0)
        render(_L_PAREN);
      render("begin");
      pp(_loop.statement_, 0);
      render("until");
      if (_i_ > 0)
        render(_R_PAREN);
    } else if (token instanceof Define) {
      Define _define = (Define) token;
      if (_i_ > 0)
        render(_L_PAREN);
      render(":");
      pp(_define.word_, 0);
      pp(_define.statement_, 0);
      render(";");
      if (_i_ > 0)
        render(_R_PAREN);
    }

  }

  private static void pp(Word token, int _i_) {
    if (token instanceof Name) {
      Name _name = (Name) token;
      if (_i_ > 0)
        render(_L_PAREN);
      pp(_name.forthword_, 0);
      if (_i_ > 0)
        render(_R_PAREN);
    }

  }

  private static void pp(Constant token, int _i_) {
    if (token instanceof Numeral) {
      Numeral _Numeral = (Numeral) token;
      if (_i_ > 0) {
        render(_L_PAREN);
      }
      pp(_Numeral.integer_, 0);
      if (_i_ > 0)
        render(_R_PAREN);
    }

  }

  private static void pp(List<Expr> token, int _i_) {
    ppListExpr(token.iterator(), _i_);
  }

  private static void ppListExpr(Iterator<Expr> it, int _i_) {
    if (it.hasNext()) {
      Expr el = it.next();
      if (!it.hasNext()) { /* last */
        pp(el, _i_);
        render(" ");
      } else { /* cons */
        pp(el, _i_);
        render(" ");
        ppListExpr(it, _i_);
      }
    }
  }

  private static void sh(EntryPoint token) {
    if (token instanceof Program) {
      Program _program = (Program) token;
      render("(");
      render("Program");
      render("[");
      sh(_program.liststatement_);
      render("]");
      render(")");
    }
  }

  private static void sh(List<Statement> token) {
    for (Iterator<Statement> it = token.iterator(); it.hasNext();) {
      sh(it.next());
      if (it.hasNext())
        render(",");
    }
  }

  private static void sh(Statement token) {
    if (token instanceof Literal) {
      Literal _literal = (Literal) token;
      render("(");
      render("Literal");
      sh(_literal.constant_);
      render(")");
    }
    if (token instanceof Expression) {
      Expression _expression = (Expression) token;
      render("(");
      render("Expression");
      sh(_expression.expr_);
      render(")");
    }
    if (token instanceof SinlgeWord) {
      SinlgeWord _sinlgeword = (SinlgeWord) token;
      render("(");
      render("SinlgeWord");
      sh(_sinlgeword.word_);
      render(")");
    }
  }

  private static void sh(Expr token) {
    if (token instanceof Conditional) {
      Conditional _conditional = (Conditional) token;
      render("(");
      render("Conditional");
      sh(_conditional.statement_);
      render(")");
    }
    if (token instanceof Loop) {
      Loop _loop = (Loop) token;
      render("(");
      render("Loop");
      sh(_loop.statement_);
      render(")");
    }
    if (token instanceof Define) {
      Define _define = (Define) token;
      render("(");
      render("Define");
      sh(_define.word_);
      sh(_define.statement_);
      render(")");
    }
  }

  private static void sh(Word token) {
    if (token instanceof Name) {
      Name _name = (Name) token;
      render("(");
      render("Name");
      sh(_name.forthword_);
      render(")");
    }
  }

  private static void sh(Constant token) {
    if (token instanceof Numeral) {
      Numeral _Numeral = (Numeral) token;
      render("(");
      render("Numeral");
      sh(_Numeral.integer_);
      render(")");
    }
  }

  private static void sh(List<Expr> token) {
    for (java.util.Iterator<Expr> it = token.iterator(); it.hasNext();) {
      sh(it.next());
      if (it.hasNext())
        render(",");
    }
  }

  private static void pp(Integer n, int _i_) {
    builder.append(n + " ");
  }

  private static void pp(String s, int _i_) {
    builder.append(s + " ");
  }

  private static void sh(Integer n) {
    render(n.toString());
  }

  private static void sh(String s) {
    printQuoted(s);
  }

  private static void printQuoted(String s) {
    render("\"" + escape(s) + "\"");
  }

  public static String escape(String s) {
    if (s == null)
      return null;
    return s.replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\b", "\\b")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\f", "\\f")
        .replace("\"", "\\\"");
  }

  private static void indent() {
    int n = num;
    while (n > 0) {
      builder.append(' ');
      n--;
    }
  }

  private static void backup() {
    int prev = builder.length() - 1;
    if (prev >= 0 && builder.charAt(prev) == ' ')
      builder.setLength(prev);
  }

  private static void trim() {
    // Trim initial spaces
    int end = 0;
    int len = builder.length();
    while (end < len && builder.charAt(end) == ' ')
      end++;
    builder.delete(0, end);
    // Trim trailing spaces
    removeTrailingSpaces();
  }

  private static void removeTrailingSpaces() {
    int end = builder.length();
    while (end > 0 && builder.charAt(end - 1) == ' ')
      end--;
    builder.setLength(end);
  }

  private static void removeTrailingWhitespace() {
    int end = builder.length();
    while (end > 0 && (builder.charAt(end - 1) == ' ' || builder.charAt(end - 1) == '\n'))
      end--;
    builder.setLength(end);
  }

  private static void onEmptyLine() {
    removeTrailingSpaces();
    int len = builder.length();
    if (len > 0 && builder.charAt(len - 1) != '\n')
      builder.append("\n");
    indent();
  }

  private static int num = 0;
  private static StringBuilder builder = new StringBuilder(INITIAL_BUFFER_SIZE);
}
