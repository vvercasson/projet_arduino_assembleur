package fr.ubordeaux.arduinoCode.ast;

import java.util.List;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public class ExprFUNCTION extends Expr {

	public enum Defined {
		//pinMode, digitalWrite, analogInput, analogWrite, peek, flush, putc, puts, digitalRead, analogRead, getc, available, delay, outputPin, inputPin
		digitalRead, digitalWrite, analogReference, analogRead, analogWrite, pinMode,
		peek, flush, putc, puts, getc, available, delay_s, delay_ms, delay_1s, delay
	};
	
	private String name;
	private List<Expr> arguments;
	private Defined defined;

	public ExprFUNCTION(String name, List<Expr> arguments) {
		super(null);
		this.name = name;
		this.arguments = arguments;
	}

	public ExprFUNCTION(Defined defined, List<Expr> arguments) {
		super(null);
		this.defined = defined;
		this.arguments = arguments;
	}

	public ExprFUNCTION(Defined defined) {
		super(null);
		this.defined = defined;
	}

	public void accept(Visitor visitor) throws Exception {
		visitor.visit(this);
	}

	public Defined getDefined() {
		return defined;
	}

	public List<Expr> getArguments() {
		return arguments;
	}

	public String getName() {
		return name;
	}

}
