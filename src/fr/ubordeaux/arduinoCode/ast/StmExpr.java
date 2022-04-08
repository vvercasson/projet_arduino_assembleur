package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class StmExpr extends Stm {

	private Expr expr;

	public StmExpr(Expr expr) {
		super();
		this.expr = expr;
	}

	public void accept(Visitor visitor) throws Exception {
		expr.accept(visitor);
	}

}
