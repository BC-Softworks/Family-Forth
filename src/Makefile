## FamiForth Makefile

JAVAC=javac
JAVAC_FLAGS=-sourcepath .
JAVA=java
JAVA_FLAGS=
PARSER=${JAVA} ${JAVA_FLAGS} java_cup.Main
PARSER_FLAGS=-nopositions -expect 100
LEXER=${JAVA} ${JAVA_FLAGS} JLex.Main
LEXER_FLAGS=
all : Main

Main : absyn forthcompiler/Yylex.class \
	forthcompiler/PrettyPrinter.class \
	forthcompiler/Main.class \
	forthcompiler/VisitSkel.class \
	forthcompiler/ComposVisitor.class \
    forthcompiler/AllVisitor.class \ 
    forthcompiler/parser.class \
    forthcompiler/sym.class \
    forthcompiler/Main.class

.PHONY : absyn

%.class : %.java
	${JAVAC} ${JAVAC_FLAGS} $^

absyn : main/com/forthcompiler/Absyn/Prg.java \
		main/com/forthcompiler/Absyn/Program.java \
		main/com/forthcompiler/Absyn/ListStatement.java \
		main/com/forthcompiler/Absyn/Statement.java \
		main/com/forthcompiler/Absyn/Literal.java \
		main/com/forthcompiler/Absyn/Expression.java \
		main/com/forthcompiler/Absyn/SinlgeWord.java \
		main/com/forthcompiler/Absyn/Expr.java \
		main/com/forthcompiler/Absyn/Conditional.java \
		main/com/forthcompiler/Absyn/Loop.java \
		main/com/forthcompiler/Absyn/Define.java \ 
		main/com/forthcompiler/Absyn/Word.java \
		main/com/forthcompiler/Absyn/Name.java \
		main/com/forthcompiler/Absyn/Constant.java \ 
		main/com/forthcompiler/Absyn/Number.java \
		main/com/forthcompiler/Absyn/ListExpr.java \
	${JAVAC} ${JAVAC_FLAGS} $^

forthcompiler/Yylex.java : forthcompiler/Yylex
	${LEXER} ${LEXER_FLAGS} forthcompiler/Yylex

forthcompiler/parser.java forthcompiler/sym.java : forthcompiler/_cup.cup
	${PARSER} ${PARSER_FLAGS} forthcompiler/_cup.cup
	mv parser.java sym.java forthcompiler/

forthcompiler/Yylex.class : forthcompiler/Yylex.java forthcompiler/parser.java forthcompiler/sym.java

forthcompiler/sym.class : forthcompiler/sym.java

forthcompiler/parser.class : forthcompiler/parser.java forthcompiler/sym.java

forthcompiler/PrettyPrinter.class : forthcompiler/PrettyPrinter.java

clean : rm -f forthcompiler/Absyn/*.class forthcompiler/*.class
