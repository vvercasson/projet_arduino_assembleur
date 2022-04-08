package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class ExprBinary extends Expr {

	public static enum Op {PLUS, MUL, MINUS, PERC, GT, GE, LT, LE, EQ, NE, BSL, BSR, BAND, BOR, AND, OR};

	private Expr left;
	private Expr right;

	public ExprBinary(Op op, Expr left, Expr right) {
		super(null);
		this.left = left;
		this.right = right;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

	public Expr getLeft() {
		return left;
	}

	public Expr getRight() {
		return right;
	}
}
