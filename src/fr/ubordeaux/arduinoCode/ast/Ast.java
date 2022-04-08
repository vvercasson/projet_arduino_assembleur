package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.visitor.Visitor;

public interface Ast {

	void accept(Visitor visitor) throws Exception;

}
