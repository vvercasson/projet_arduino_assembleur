package fr.ubordeaux.arduinoCode.ast;

import java.util.List;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class StmSeq extends Stm {

	private List<Stm> list;

	public StmSeq(List<Stm> list) {
		this.list = list;
	}

	@Override
	public void accept(Visitor visitor) throws Exception {
		for (Stm stm : list) {
			stm.accept(visitor);
		}
	}

}
