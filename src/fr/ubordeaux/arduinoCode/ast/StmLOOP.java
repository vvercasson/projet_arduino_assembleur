package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class StmLOOP extends Stm {

	private Stm stm;
	private int id;
	private static int nextId;

	public StmLOOP(Stm stm) {
		this.stm = stm;
		this.id = nextId++;
	}

	public void accept(Visitor visitor) throws Exception{
		visitor.visit(this);
	}

	public Stm getStm() {
		return stm;
	}

	public int getId() {
		return id;
	}
}
