package fr.ubordeaux.arduinoCode.ast;

public abstract class ExprStm extends Stm {

	private Stm son;

	public ExprStm(Stm son) {
		this.son = son;
	}

	public Stm getSon() {
		return son;
	}

}
