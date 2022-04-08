package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.Token;
import fr.ubordeaux.arduinoCode.visitor.Visitor;

public abstract class Stm implements Ast {

	private Token token;

	public Stm() {
	}
	
	public Stm(Token token) {
		this.token = token;
	}

	public Token getToken() {
		return token;
	}

}
