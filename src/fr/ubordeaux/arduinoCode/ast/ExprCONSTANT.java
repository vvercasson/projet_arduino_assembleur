package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.type.Type;
import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class ExprCONSTANT extends Expr {
	
	private static int nextId;
	private int id;
	private Object value;
	private String name;

	public ExprCONSTANT(Type type, Object value) {
		super(type);
		this.value = value;
		id = nextId++;
		name = "Cst_" + String.valueOf(id);
	}

	public Object getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return value.toString();
	}
	
	
}
