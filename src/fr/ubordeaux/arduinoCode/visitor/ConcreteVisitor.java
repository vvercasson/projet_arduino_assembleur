//
// DO NOT EDIT
//

package fr.ubordeaux.arduinoCode.visitor;

import fr.ubordeaux.arduinoCode.ast.*;

//
// Cette classe ne fait rien d'autre que d'afficher un message quand le développeur n'a pas 
// encore implémenté le visitor
//
public class ConcreteVisitor implements Visitor {

	public void msg(Object object) {
		// uncomment for verbose mode 
		System.err.println("*** unimplemented public void visit(" + object.getClass().getName() + " o) in " + this + " visitor");
	}

	@Override
	public void visit(StmAFF stm) throws Exception {
		msg(stm);
	}

	@Override
	public void visit(StmIF stmIF) throws Exception {
		msg(stmIF);
	}

	@Override
	public void visit(ExprCONSTANT expr) throws Exception {
		msg(expr);
	}

	@Override
	public void visit(ExprFUNCTION expr) throws Exception {
		msg(expr);
	}

	@Override
	public void visit(StmLOOP stm) throws Exception {
		msg(stm);
	}

	@Override
	public void visit(ExprBinary expr) {
		msg(expr);
	}

	@Override
	public void visit(ExprUnary expr) {
		msg(expr);
	}

	@Override
	public void visit(ExprVAR expr) {
		msg(expr);
	}

	@Override
	public void visit(StmBREAK stm) {
		msg(stm);
	}

	@Override
	public void visit(StmCASE stm) {
		msg(stm);
	}

	@Override
	public void visit(StmDO stm) {
		msg(stm);
	}

	@Override
	public void visit(StmFOREACH stm) {
		msg(stm);
	}

	@Override
	public void visit(StmRETURN stm) {
		msg(stm);
	}

	@Override
	public void visit(StmSWITCH stm) {
		msg(stm);
	}

	@Override
	public void visit(StmWHILE stm) {
		msg(stm);
	}

	@Override
	public void visit(Stm stm) throws Exception {
		msg(stm);
	}

	@Override
	public void visit(Expr expr) throws Exception {
		msg(expr);
	}

}
