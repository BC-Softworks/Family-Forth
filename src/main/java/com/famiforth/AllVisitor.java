package com.famiforth;

import com.famiforth.Absyn.*;

public interface AllVisitor<R, A> extends
    EntryPoint.Visitor<R, A>,
    Statement.Visitor<R, A>,
    Expr.Visitor<R, A>,
    Word.Visitor<R, A>,
    Constant.Visitor<R, A> {
}
