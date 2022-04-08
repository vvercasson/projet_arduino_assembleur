package fr.ubordeaux.arduinoCode.type;

// Type:
// Représentation d'un type simple ou d'un type complexe

//Pour l'instant cet arbre contient:
//- Le code (qu'on trouve dans TypeCode)
//- Sa structure (left, right)
//- Le type (par exemple d'une expression)
//- La ligne et la colonne du code source correspondant


// Pour vérifier un type:
// assertType vérifie que deux types sont égaux ou
// qu'un type soit égal à un type donné

// Cette interface s'utilise avec l'implémentation
// TreeType
//
// Exemple:
// Type type = new TreeType(TypeCode.STRING);

public interface Type {

	enum Tag {FIELD, BOOLEAN, LIST, SET, RANGE, STRUCT, STRING, UINT8_T, UINT16_T, INT16_T, INT32_T, UINT32_T, ENUM, INT8_T, FUNCTION, PROCEDURE, NC};
	
	Type getLeft();
	Type getRight();
	int getSize();
	int getOffset();
	boolean equivalent(Type type);
	boolean isBoolean();
	Tag getTag();
	void cast(Tag tag);
}
