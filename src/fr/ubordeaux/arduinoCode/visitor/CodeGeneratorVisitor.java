package fr.ubordeaux.arduinoCode.visitor;


import fr.ubordeaux.arduinoCode.ast.*;

public class CodeGeneratorVisitor extends ConcreteVisitor {

	private String name;
	private String headSection = "";
	private String footSection = "";
	private String sectionData = "";
	private String sectionText = "";
	private Integer currentRegister = 25;
	
	public CodeGeneratorVisitor(String name) {
		super();
		this.name = name;
	}

	// Le registre courant qui nous sert pour les appels 
	// de fonctions et les calculs
	private String register() {
		return "r" + currentRegister;
	}

	// 
	// Produit le code pour une affectation
	// fr.ubordeaux.arduinoCode.ast.ExprFUNCTION
	// méthodes exploitables:
	//	- Defined expr.getDefined() ; function prédéfinie
	//  - List<Expr> expr.getArguments() ; arguments de la fonction
	//  - String expr.getName() ; nom de la fonction
	//  - int expr.getPin() ; pin Arduino argument de la fonction prédéfinie
	//
	public void visit(ExprFUNCTION expr) throws Exception {
		System.err.println("*** visit(ExprFUNCTION) with " + this);

		//for (int i = 0 ; i<=25 ; ++i)
		//	sectionText += "\tpush r" + i + "\n";
		
		currentRegister = 25;
		if (expr.getArguments() != null) {
			for (Expr arg : expr.getArguments()) {
				arg.accept(this);
				currentRegister -= arg.getSize();
				}
			}
		if (expr.getDefined() != null) {
			switch (expr.getDefined()) {
			case pinMode:
				sectionText += "	call pinMode\n";
				break;
			case digitalWrite:
				sectionText += "	call digitalWrite\n";
				break;
			case delay:
				sectionText += "	call delay\n";
				break;
			case delay_ms:
				sectionText += "	call delay_ms\n";
				break;
			case delay_s:
				sectionText += "	call delay_s\n";
				break;
			case delay_1s:
				sectionText += "	call delay_1s\n";
				break;
			case putc:
				sectionText += "	call uart_putc\n";
				break;
			case puts:
				sectionText += "	call uart_puts\n";
				break;
			default:
				break;
			}
		}
		//for (int i = 25 ; i>=0 ; --i)
		//	sectionText += "\tpop r" + i + "\n";
		

	}
	
	// 
	// Produit le code pour une expression constante
	// fr.ubordeaux.arduinoCode.ast.ExprCONSTANT
	// méthodes exploitables:
	// - Type expr.getType() ; type de la constante
	// - String expr.getName() ; nom de la constante
	// - Object expr.getValue() ; valeur de la constante
	@Override
	public void visit(ExprCONSTANT expr) {
		System.err.println("*** visit(ExprCONSTANT (" + expr.getType().getTag() + ") ) with " + this);
		switch (expr.getType().getTag()) {
		case STRING:
			sectionData += expr.getName() + ":\n";
			sectionData += "	.asciz	\"" + (String)expr.getValue() + "\"\n";
			sectionText += "	ldi r" + (currentRegister-1) + ", lo8(" + String.valueOf(expr.getName()) + ")\n"; 
			sectionText += "	ldi r" + (currentRegister) + ", hi8(" + String.valueOf(expr.getName()) + ")\n"; 
			break;
		case BOOLEAN:
		case ENUM:
		case FIELD:
		case FUNCTION:
		case LIST:
		case NC:
		case PROCEDURE:
		case RANGE:
		case SET:
		case STRUCT:
			break;
		case INT8_T:
		case UINT8_T:
			sectionText += "	ldi r" + (currentRegister) + ", 0\n";
			sectionText += "	ldi r" + (currentRegister-1) + ", " + expr.getValue().toString() + "\n";
			break;
		case INT16_T:
		case UINT16_T:{
			int value = (int)expr.getValue();
			System.err.println("###########" + expr.getValue());
			short high = (short) ((value >> 8) & 0xFF);
			short low = (short) (value & 0xFF);
			System.err.println("###########" + high);
			System.err.println("###########" + low);
			sectionText += "	ldi r" + (currentRegister) + ", " + high + "\n"; 
			sectionText += "	ldi r" + (currentRegister-1) + ", " + low + "\n"; 
			} break;
		case INT32_T:
		case UINT32_T:
			{int value = (int)expr.getValue();
			int high = (short) (value >> 16);
			int low = (short) (value & 0xFFFF);
			sectionText += "	ldi r" + (currentRegister) + ", " + (low & 0xFF) + "\n"; 
			sectionText += "	ldi r" + (currentRegister-1) + ", " + ((low >> 8) & 0xFF) + "\n"; 
			sectionText += "	ldi r" + (currentRegister-2) + ", " + (high & 0xFF) + "\n"; 
			sectionText += "	ldi r" + (currentRegister-3) + ", " + ((high >> 8) & 0xFF) + "\n"; 
			} break;
		default:
			break;
		
		}
	}
	
	// 
	// Produit le code pour une affectation
	// fr.ubordeaux.arduinoCode.ast.StmAFF
	// méthodes exploitables:
	// - Expr stm.getLeft() ; partie gauche de l'affectation
	// - Expr stm.getRight() ; partie droite de l'affectation
	// - Op stm.getOp() ; opérateur ajoutée à l'affectation (par exemple PLUS pour +=)
	// 
	@Override
	public void visit(StmAFF stm) throws Exception {
		System.err.println("*** visit(StmAFF) with " + this);
		stm.getRight().accept(this);
		sectionText += "	sts " + ((ExprVAR)stm.getLeft()).getName() + ", " + register() + "\n";
	}

	// 
	// Produit le code pour une affectation
	// fr.ubordeaux.arduinoCode.ast.StmIF
	// méthodes exploitables:
	// - public Expr stmIF.getExpr() ; expression booléenne du test
	// - Stm stmIF.getStm1() ; code si le test réussit
	// - Stm stmIF.getStm2() ; code si le test échoue
	// 
	//@Override
	//public void visit(StmIF stmIF) throws TypeException {
	//	System.err.println("*** visit(StmIF) with " + this);
	//}

	// 
	// Produit le code pour une affectation
	// fr.ubordeaux.arduinoCode.ast.StmLOOP
	// méthodes exploitables:
	// - public Stm stmLOOP.getStm() ; code répété
	// - int stmLOOP.getId() ; identifiant de la boucle
	// 
	@Override
	public void visit(StmLOOP stmLOOP) throws Exception {
		System.err.println("*** visit(StmLOOP) with " + this);
		sectionText += ".LOOP_" + stmLOOP.getId() + ":\n";
		stmLOOP.getStm().accept(this);
		sectionText += "	rjmp " + ".LOOP_" + stmLOOP.getId() + "\t\t; eternal loop\n";
		sectionText += ".END_LOOP_" + stmLOOP.getId() + ":\t\t; break jump\n";
	}

	@Override
	public String toString() {
		return name;
	}

	private void generateHead() {
		//headSection += "	LDI r16, lo8(RAMEND)\n";
		//headSection += "	OUT SPL, r16\n";
		//headSection += "	LDI r16, hi8(RAMEND)\n";
		//headSection += "	OUT SPH, r16\n";
		headSection += "	push r28\n";
		headSection += "	push r29\n";
		headSection += "	in r28,SPL\n";
		headSection += "	in r29,SPH\n";

	}
	
	private void generateFoot() {
		footSection += "	pop r29\n";
		footSection += "	pop r28\n";
		footSection += "	ret\n";
		footSection += ".global __do_copy_data\n";
	}
	
	public void generateCode() {
		generateHead();
		generateFoot();
		System.out.println("#include <avr/io.h>");
		System.out.println(".include \"data/m328Pdef.s\"");
		
		System.out.println(";;; SRAM DATA");
		System.out.println(".section .data");
		System.out.println("");
		System.out.print(sectionData);
		System.out.println("");
		System.out.println(";;; FLASH CODE");
		System.out.println(".section .text");
		System.out.println(".org 0x00");
		System.out.println(".include \"data/delay-2.s\"");
		System.out.println("");
		System.out.println(".global main_program");
		System.out.println("main_program:");
		System.out.println("");
		System.out.print(headSection);
		System.out.print(sectionText);
		System.out.print(footSection);
		System.out.println("");
		System.out.println("");
		System.out.println(".end");
	}

}
