package fr.ubordeaux.arduinoCode.ast;

public class ExprARRAY extends ExprUnary {

	private ExprVAR var;

	public ExprARRAY(Expr expr, ExprVAR var) {
		super(Op.ARRAY, expr);
		this.var = var;
	}

	public ExprVAR getVar() {
		return var;
	}

}
