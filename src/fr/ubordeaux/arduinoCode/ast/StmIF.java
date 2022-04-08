package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.Token;
import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class StmIF extends Stm {

	private Expr expr;
	private Stm stm1;
	private Stm stm2;

	public StmIF(Token token, Expr expr, Stm stm1, Stm stm2) {
		super(token);
		this.expr = expr;
		this.stm1 = stm1;
		this.stm2 = stm2;
	}

	public StmIF(Token token, Expr expr, Stm stm1) {
		super(token);
		this.expr = expr;
		this.stm1 = stm1;
	}

	public Expr getExpr() {
		return expr;
	}

	public Stm getStm1() {
		return stm1;
	}

	public Stm getStm2() {
		return stm2;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

}
