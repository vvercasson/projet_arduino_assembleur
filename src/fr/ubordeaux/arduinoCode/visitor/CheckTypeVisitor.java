package fr.ubordeaux.arduinoCode.visitor;

import fr.ubordeaux.arduinoCode.ast.Expr;
import fr.ubordeaux.arduinoCode.ast.ExprCONSTANT;
import fr.ubordeaux.arduinoCode.ast.ExprFUNCTION;
import fr.ubordeaux.arduinoCode.ast.StmAFF;
import fr.ubordeaux.arduinoCode.ast.StmIF;
import fr.ubordeaux.arduinoCode.type.TypeException;

public class CheckTypeVisitor extends ConcreteVisitor {

	private String name;
	
	public CheckTypeVisitor(String name) {
		super();
		this.name = name;
	}

	@Override
	public void visit(StmAFF stm) throws TypeException {
		System.err.println("*** visit(Stm) with " + this);
		if (!stm.getLeft().getType().equivalent(stm.getRight().getType()))
			throw new TypeException("types should be equivalent");
	}

	@Override
	public void visit(StmIF stm) throws TypeException {
		System.err.println("*** visit(StmIF) with " + this);
		if (!stm.getExpr().getType().isBoolean())
			throw new TypeException(stm.getToken(), "IF statement should have boolean expression");
	}

	@Override
	public void visit(ExprFUNCTION expr) throws Exception {
		System.err.println("*** visit(ExprFUNCTION) with " + this);
		if (expr.getArguments() != null) {
			for (Expr arg : expr.getArguments()) {
				arg.accept(this);
			}
		}
	}

	@Override
	public void visit(ExprCONSTANT expr) throws Exception {
		System.err.println("*** visit(ExprCONSTANT) with " + this);
	}

	@Override
	public String toString() {
		return name;
	}

}
