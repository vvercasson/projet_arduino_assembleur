package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class ExprUnary extends Expr {

	private Expr expr;
	private Op op;
	public enum Op {NOT, MINUS, PLUSPLUS, MINUSMINUS, PLUSPLUSP, MINUSMINUSP, ARRAY};

	public ExprUnary(Op op, Expr expr) {
		super(null);
		this.expr = expr;
		this.op = op;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

	public Expr getExpr() {
		return expr;
	}

	public Op getOp() {
		return op;
	}
}
