package fr.ubordeaux.arduinoCode.ast;

import fr.ubordeaux.arduinoCode.type.Type;
import fr.ubordeaux.arduinoCode.type.Type.Tag;
import fr.ubordeaux.arduinoCode.visitor.Visitor;

public abstract class Expr implements Ast {
    
    private Type type;
    
    public Expr(Type type) {
    	this.type = type;
    }
    
	public Type getType() {
    	return type;
    }

	public int getSize() {
		return type.getSize();
	}

	public void cast(Tag tag) {
		type.cast(tag);
	};
    
}

