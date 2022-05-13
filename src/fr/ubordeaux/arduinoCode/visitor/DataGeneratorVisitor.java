package fr.ubordeaux.arduinoCode.visitor;


import fr.ubordeaux.arduinoCode.ast.*;

public class DataGeneratorVisitor extends ConcreteVisitor {

	private String name;
	private String sectionFLASHData = "";
	private String sectionSRAMData = "";
	
	public DataGeneratorVisitor(String name) {
		super();
		this.name = name;
	}

	// Purpose: Produire le code d'une DeclVAR
	// Arguments: declVar
	// Effect: sectionData contient le code de la déclaration de variable
	// Uses:
	// - Type declVar.getType() ; type de la variable
	// - String declVar.getName() ; nom de la variable
	//
	@Override
	public void visit(DeclVAR declVar) {
		System.err.println("*** visit(DeclVAR (" + declVar.getType().getTag() + ") with " + this);
		switch (declVar.getType().getTag()) {
		case INT8_T:
		case UINT8_T:
		case PIN:
		case INT16_T:
		case UINT16_T:
		case INT32_T:
		case UINT32_T:
		case F32_T:
			sectionSRAMData += "	;; Déclaration de la variable " + declVar.getName() + " sur " + declVar.size() + " octets \n"; 
			sectionSRAMData += "	.comm " + declVar.getName() + ", " + declVar.size() + "\n"; 
			break;
		case LIST:
			sectionSRAMData += "	;; Déclaration de la liste " + declVar.getName() + " sur 512 octets \n"; 
			sectionSRAMData += "	.comm " + declVar.getName() + ",512"+ "\n"; 
			break;
		case FUNCTION:
		case PROCEDURE:
			sectionSRAMData += declVar.getName() + ":\n";
			sectionSRAMData += "	push r28\n";
			sectionSRAMData += "	push r29\n";
			sectionSRAMData += "	push __tmp_reg__\n";
			sectionSRAMData += "	in r28,__SP_L__\n";
			sectionSRAMData += "	in r29,__SP_H__\n";
			sectionSRAMData += "	; ; <Code de la fonction ici>";
			sectionSRAMData += "	pop __tmp_reg__\n";
			sectionSRAMData += "	pop r29\n";
			sectionSRAMData += "	pop r28\n";
			sectionSRAMData += "	ret\n";
			break;
		default:
			sectionSRAMData += ";; Unimplemented  (CodeGeneratorVisitor.java line 348))\n"; 
			break;
		
		}
	}
	
	// Purpose: Produire le code d'une ExprCONSTANT
	// Arguments: expr
	// Effect: 	sectionText contient le code
	// 			sectionData contient les données constantes déclarées
	// Uses:
	// - Type expr.getType() ; type de la constante
	// - String expr.getName() ; nom de la constante
	// - Object expr.getValue() ; valeur de la constante
	//
	@Override
	public void visit(ExprCONSTANT expr) {
		System.err.println("*** visit(ExprCONSTANT (" + expr.getType().getTag() + ") with " + this);
		switch (expr.getType().getTag()) {
		// La constante string est réservée dans l'espace FLASH
		case STRING:
				sectionFLASHData += expr.getName() + ":\n";
				sectionFLASHData += "	.asciz	\"" + (String)expr.getValue() + "\"\n";
			break;
		default:
			break;
		
		}
	}

	// @Override
	// public void visit(ExprLIST expr) throws Exception {
	// 	System.err.println("*** visit(ExprLIST (" + expr.getType().getTag() + ") with " + this);
	// 	switch (expr.getType().getTag()) {
	// 	case LIST:
	// 			sectionFLASHData += ".comm	NOM_VAR," + expr.size() + "\n";
	// 		break;
	// 	default:
	// 		break;
		
	// 	}
	// }
	
	// Purpose: Produit la partie data (en FLASH pour les constantes et
	//		en SRAM pour les variables) de l'AVR Assembler
	// Argument: StmAFF object
	// Effect: Ajoute du code AVR Assembler aux variables sectionFLASHData et
	//		sectionSRAMData
	public void visit(StmAFF object) throws Exception {
	   System.err.println("*** visit(StmAFF) withDataGeneratorVisitor");
	   object.getRight().accept(this);
	}

	public void visit(StmIF object) throws Exception {
		System.err.println("*** visit(stmIF) withDataGeneratorVisitor");
		object.getExpr().accept(this);
	}

	public void visit(StmWHILE object) throws Exception {
		System.err.println("*** visit(stmWHILE) withDataGeneratorVisitor");
		object.getExpr().accept(this);
	}

	public void visit(StmDO object) throws Exception {
		System.err.println("*** visit(stmDO) withDataGeneratorVisitor");
		object.getExpr().accept(this);
	}

	// Purpose: Produit la partie data (en FLASH pour les constantes et
	//		en SRAM pour les variables) de l'AVR Assembler
	// Argument: ExprBinary object
	// Effect: Ajoute du code AVR Assembler aux variables sectionFLASHData et
	//		sectionSRAMData
	public void visit(ExprBinary object) throws Exception {
	   System.err.println("*** visit(ExprBinary) withDataGeneratorVisitor");
	   object.getLeft().accept(this);
	   object.getRight().accept(this);
	}

	// public void visit(ExprFUNCTION expr) throws Exception {
	// 	System.err.println("*** visit(ExprFUNCTION) withDataGeneratorVisitor");
	// 	if(expr.getDefined() == null) {
	// 		sectionFLASHData += expr.getName() + ":\n";
	// 		sectionFLASHData += "	push r28\n";
	// 		sectionFLASHData += "	push r29\n";
	// 		sectionFLASHData += "	push __tmp_reg__\n";
	// 		sectionFLASHData += "	in r28,__SP_L__\n";
	// 		sectionFLASHData += "	in r29,__SP_H__\n";
	// 		sectionFLASHData += "	pop __tmp_reg__\n";
	// 		sectionFLASHData += "	pop r29\n";
	// 		sectionFLASHData += "	pop r28\n";
	// 		sectionFLASHData += "	ret\n";
			
	// 		// expr.accept(this);
	// 	}
	//  }

	// Purpose: Produit la partie data (en FLASH pour les constantes et
	//		en SRAM pour les variables) de l'AVR Assembler
	// Argument: ExprVAR object
	// Effect: Ajoute du code AVR Assembler aux variables sectionFLASHData et
	//		sectionSRAMData
	public void visit(ExprVAR object) throws Exception {
	   System.err.println("*** visit(ExprVAR) withDataGeneratorVisitor");
	}

	// Purpose: Produit le code du fichier *.s
	public void generateCode() {
		if (!sectionFLASHData.isEmpty()) {
			System.out.println(";; -------------------------------------------------------");
			System.out.println(";; Section des variables enregistrées dans la mémoire FLASH");
			System.out.println("	.section .rodata\n");
			System.out.println(sectionFLASHData);
			System.out.println(";; -------------------------------------------------------");
		}
		if (!sectionSRAMData.isEmpty()) {
			System.out.println(";; -------------------------------------------------------");
			System.out.println(";; Section des variables enregistrées dans la mémoire SRAM");
			System.out.println("	.section .data\n");
			System.out.println(sectionSRAMData);
			System.out.println(";; -------------------------------------------------------");
		}
	}

	// Purpose: Donne le nom de ce visitor
	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getPurpose() {
		return "Produit la partie data (en FLASH pour les constantes et\n//\ten SRAM pour les variables) de l'AVR Assembler";
	}

	@Override
	public String getEffect() {
		return "Ajoute du code AVR Assembler aux variables sectionFLASHData et\n//\tsectionSRAMData";
	}


}
