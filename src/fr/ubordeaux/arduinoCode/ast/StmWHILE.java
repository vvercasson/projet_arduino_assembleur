package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

// L'instruction while (<expr>) <stm>
public class StmWHILE extends Stm {

	private Expr expr;
	private Stm stm;

	public StmWHILE(Expr expr, Stm stm) {
		this.expr = expr;
		this.stm = stm;
	}

	public Expr getExpr() {
		return expr;
	}

	public Stm getStm() {
		return stm;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

}
