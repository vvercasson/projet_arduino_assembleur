package fr.ubordeaux.arduinoCode.visitor;

import fr.ubordeaux.arduinoCode.ast.*;
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
		System.err.println("*** Unimplemented (CheckTypeVisitor.java line 18))");
		//stm.getLeft().getType().attestEquivalentTo(stm.getRight().getType());
		stm.getRight().setType(stm.getLeft().getType());
	}

	@Override
	public void visit(StmIF stm) throws TypeException {
		System.err.println("*** visit(StmIF) with " + this);
		stm.getExpr().getType().attestBoolean();
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
	public void visit(ExprUnary expr) throws Exception {
		System.err.println("*** visit(ExprUnary) with " + this);
		System.err.println("*** Unimplemented (CheckTypeVisitor.java line 47))");
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getPurpose() {
		return " Teste le type de l'objet et déclanche une exception TypeException en cas d'échec";
	}

	@Override
	public String getEffect() {
		return "Déclanche une exception de type TypeException";
	}

}
