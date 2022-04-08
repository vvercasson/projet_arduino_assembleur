package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class StmAFF extends Stm {

	public static enum Op {
		PLUS, MINUS, MUL, DIV, PER, LAND, LOR, AND, OR};

	private Expr left;
	private Expr right;
	private Op op;

	public StmAFF(Expr left, Expr right) {
		super();
		this.left = left;
		this.right = right;
	}

	public StmAFF(Op op, Expr left, Expr right) {
		this(left, right);
		this.op = op;
	}

	public Expr getLeft() {
		return left;
	}

	public Expr getRight() {
		return right;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

	public Op getOp() {
		return op;
	}
}
