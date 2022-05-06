package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.Token;
import fr.ubordeaux.arduinoCode.visitor.Visitor;

// L'instruction if (<expr>) <stm> [else <stm>]
public class StmIF extends Stm {

	private Expr expr;
	private Stm stmTrue;
	private Stm stmFalse;

	public StmIF(Token token, Expr expr, Stm stm1, Stm stm2) {
		super(token);
		this.expr = expr;
		this.stmTrue = stm1;
		this.stmFalse = stm2;
	}

	public StmIF(Token token, Expr expr, Stm stm1) {
		super(token);
		this.expr = expr;
		this.stmTrue = stm1;
	}

	public Expr getExpr() {
		return expr;
	}

	public Stm getStm1() {
		return stmTrue;
	}

	public Stm getStm2() {
		return stmFalse;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

}
