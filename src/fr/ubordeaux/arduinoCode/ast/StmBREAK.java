package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class StmBREAK extends Stm {

	public StmBREAK() {
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}
}
