package fr.ubordeaux.arduinoCode.visitor;

import fr.ubordeaux.arduinoCode.ast.*;
import fr.ubordeaux.arduinoCode.environment.Environment;
import fr.ubordeaux.arduinoCode.type.Type;
import fr.ubordeaux.arduinoCode.type.TypeTree;

public class CodeGeneratorVisitor extends ConcreteVisitor {

	private String name;
	private String sectionText = "";
	private int currentRegisterCt = 24;

	public CodeGeneratorVisitor(String name) {
		this.name = name;
	}

	// Purpose: Afficher Le registre courant + offset
	// Arguments: offset
	// Result: String = "rx"
	private String currentRegister(int offset) {
		return "r" + (currentRegisterCt + offset);
	}

	// Purpose: Afficher Le registre courant
	// Arguments: none
	// Result: String = "rx"
	private String currentRegister() {
		return currentRegister(0);
	}

	// Purpose: Afficher un registre
	// Arguments: x
	// Result: String = "rx"
	private String register(int register) {
		return "r" + register;
	}

	// Purpose: Produire le code d'une ExprFUNCTION
	// Arguments: expr
	// Effects:
	// sectionText contient le code
	// expr.register contient le résultat
	public void visit(ExprFUNCTION expr) throws Exception {
		System.err.println("*** visit(ExprFUNCTION) with " + this);

		// int8_t : r24:25 (r24= data)
		// int16_t : r24:25
		// int32_t : r22:25
		// int64_t : r18:25
		//
		// On sauvegarde le registre courant
		int savedRegisterCt = currentRegisterCt;
		// Le registre courant les 26 car les données non empilées
		// seront enregistrées sur r18:r25
		currentRegisterCt = 26;
		if (expr.getArguments() != null) {
			for (Expr arg : expr.getArguments()) {
				// On pointe sur un registre pair
				// - 1 ou 2 octets (uin8_t ou uint16_t): r24:r25
				// - 4 octets (uint32_t): r22:r25
				// - 8 octets (uint64_t) : r18:r25
				if (arg.size() % 2 == 1) {
					currentRegisterCt -= arg.size() + 1;
				} else {
					currentRegisterCt -= arg.size();
				}
				arg.accept(this);
			}
		}
		currentRegisterCt = savedRegisterCt;
		if (expr.getDefined() != null) {
			switch (expr.getDefined()) {
			case pinMode:
				sectionText += "	call pinMode\n";
				break;
			case digitalWrite:
				sectionText += "	call digitalWrite\n";
				break;
			case digitalRead:
				sectionText += "	call digitalRead\n";
				break;
			case analogReference:
				sectionText += "	call analogReference\n";
				break;
			case analogWrite:
				sectionText += "	call analogWrite\n";
				break;
			case analogRead:
				sectionText += "	call analogRead\n";
				break;
			case delay_ms:
				sectionText += "	call delay_ms\n";
				break;
			case delay_s:
				sectionText += "	call delay_s\n";
				break;
			case peek:
				sectionText += "	call uart0_peek\n";
				break;
			case flush:
				sectionText += "	call uart0_flush\n";
				break;
			case getc:
				sectionText += "	call uart0_getc\n";
				break;
			case putc:
				sectionText += "	call uart0_putc\n";
				break;
			case puts:
				sectionText += "	call uart0_puts\n";
				break;
			case available:
				sectionText += "	call uart0_available\n";
				break;
			default:
				sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 115)\n";
				break;
			}
		}
		else {
			sectionText += "	call " + expr.getName(); 
		}
		Type output = expr.getType().getRight();
		if (output != null) {
			switch (output.getTag()) {
			case INT8_T:
			case UINT8_T:
			case PIN:
				expr.setRegister(currentRegisterCt);
				break;
			case INT16_T:
			case UINT16_T:
				expr.setRegister(currentRegisterCt - 2);
				break;
			case INT32_T:
			case UINT32_T:
				expr.setRegister(currentRegisterCt - 4);
				break;
			default:
				sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 136)\n";
				break;
			}
		}
	}

	// Purpose: Produire le code d'une ExprUnary
	// Arguments: expr
	// Effects:
	// sectionText contient le code
	// expr.register contient le résultat
	@Override
	public void visit(ExprUnary expr) throws Exception {
		System.err.println("*** visit(ExprUnary) with " + this);
		switch (expr.getOp()) {
		// L'expression est un argument de fonction
		// On passe à rxx:ryy les arguments qui se trouvent dans les registres
		// qui contiennent les données de cette expression
		case ARG: {
			Expr subExpr = expr.getExpr();
			switch (subExpr.getType().getTag()) {
			case INT8_T:
			case UINT8_T:
			case PIN:
				sectionText += "	;; Passage d'un argument de 8 bits à une fonction\n";
				break;
			case INT16_T:
			case UINT16_T:
				sectionText += "	;; Passage d'un argument de 16 bits à une fonction\n";
				break;
			default:
				sectionText += ";; Unimplemented  (CodeGeneratorVisitor.java line 167)\n";
				break;
			}
			subExpr.accept(this);
			switch (subExpr.getType().getTag()) {
			case INT8_T:
			case UINT8_T:
			case PIN:
				if (currentRegisterCt != subExpr.getRegister())
					sectionText += "	mov " + currentRegister() + ", " + register(subExpr.getRegister()) + "; Copy "
							+ register(subExpr.getRegister()) + " to " + currentRegister(-1) + "\n";
				break;
			case INT16_T:
			case UINT16_T:
				if (currentRegisterCt != subExpr.getRegister())
					sectionText += "	movw " + currentRegister() + ", " + register(subExpr.getRegister()) + ";\n";
				break;
			case STRING:
				if (currentRegisterCt != subExpr.getRegister())
					sectionText += "	movw " + currentRegister() + ", " + register(subExpr.getRegister()) + ";\n";
				break;
			default:
				sectionText += ";; Unimplemented  (CodeGeneratorVisitor.java line 189)\n";
				break;
			}
		}
			break;
		// L'expression est un opérateur ++
		case PP: {
			Expr subExpr = expr.getExpr();
			// Dépose dans le(s) registre(s) courants la valeur
			// Attention: ne fonctionne que pour les variables, mais pas pour les tableaux
			subExpr.accept(this);
			switch (subExpr.getType().getTag()) {
			case INT8_T:
			case UINT8_T:
			case PIN:
				sectionText += "	subi " + currentRegister() + ",0xFF" + "; Subtract 255 from " + currentRegister()
						+ "\n";
				sectionText += "	;; Copier 1 octets à partir de " + currentRegister() + " vers cible\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + ", " + currentRegister() + "\n";
				expr.setRegister(currentRegisterCt);
				break;
			case INT16_T:
			case UINT16_T:
				sectionText += "	adiw " + currentRegister() + ",1" + "; Add 1 to " + currentRegister() + ":"
						+ currentRegister(1) + "\n";
				sectionText += "	;; Copier 2 octets à partir de " + currentRegister() + " vers cible\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + ", " + currentRegister() + "\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + "+1, " + currentRegister(1) + "\n";
				expr.setRegister(currentRegisterCt);
				break;
			case INT32_T:
			case UINT32_T:
				sectionText += "	adiw " + currentRegister() + ",1" + "; Add 1 to " + currentRegister() + ":"
						+ currentRegister(1) + "\n";
				sectionText += "	adc " + currentRegister(2) + ",__zero_reg__" + "; Add 0 with carry high byte\n";
				sectionText += "	adc " + currentRegister(3) + ",__zero_reg__" + "; Add 0 with carry high byte\n";
				sectionText += "	;; Copier 4 octets à partir de " + currentRegister() + " vers cible\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + ", " + currentRegister() + "\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + "+1, " + currentRegister(1) + "\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + "+2, " + currentRegister(2) + "\n";
				sectionText += "	sts " + ((ExprVAR) subExpr).getName() + "+3, " + currentRegister(3) + "\n";
				expr.setRegister(currentRegisterCt);
				break;
			default:
				sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 214)\n";
				break;
			}
		}
			break;
		default:
			sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 239))" + expr.getOp() + "\n";
			break;

		}
	}

	// Purpose: Produire le code pour une instruction loop
	// Arguments: stmLOOP
	// Effect: sectionText contient le code
	// Uses:
	// - int stmLOOP.getId() ; identifiant unique de stm
	// - Stm stmLOOP.getStm() ; instruction enchâssée dans la boucle
	@Override
	public void visit(StmLOOP stm) throws Exception {
		System.err.println("*** visit(StmLOOP) with " + this);
		sectionText += ".LOOP_" + stm.getId() + ":\n";
		stm.getStm().accept(this);
		sectionText += "	jmp " + ".LOOP_" + stm.getId() + "\t\t; eternal loop\n";
		sectionText += ".END_LOOP_" + stm.getId() + ":\t\t; break jump\n";
	}

	//
	// Purpose: Produire le code d'une ExprBinary
	// Arguments: expr
	// Effect: sectionText contient le code
	@Override
	public void visit(ExprBinary expr) throws Exception {
		System.err.println("*** visit(ExprBinary) with " + this);
		int savedRegisterCt = currentRegisterCt;
		expr.getLeft().accept(this);
		currentRegisterCt += expr.getLeft().getType().size();
		expr.getRight().accept(this);
		switch (expr.getOp()) {
		case EQ:
			sectionText += "	cp " + register(expr.getLeft().getRegister()) + ","
					+ register(expr.getRight().getRegister()) + "\n";
			break;
		case PLUS:
			sectionText += "	add " + register(expr.getLeft().getRegister()) + "," + register(expr.getRight().getRegister()) + "\n";
			expr.setRegister(expr.getLeft().getRegister());
			break;
		default:
			sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 300))" + "\n";
			break;
		}
		currentRegisterCt = savedRegisterCt;
	}

	//
	// Purpose: Produire le code d'une ExprVAR
	// Arguments: expr
	// Effect: sectionText contient le code
	@Override
	public void visit(ExprVAR expr) throws Exception {
		System.err.println("*** visit(ExprVAR) with " + this);
		switch (expr.getType().getTag()) {
		// on copie la variable dans le registre courant
		case UINT8_T:
		case INT8_T:
		case PIN:
			sectionText += "	;; Copier la variable dans " + currentRegister() + "\n";
			sectionText += "	lds " + currentRegister() + ", " + expr.getName() + "\n";
			expr.setRegister(currentRegisterCt);
			break;
		case UINT16_T:
		case INT16_T:
			sectionText += "	;; Copier la variable dans " + currentRegister() + ":" + currentRegister(1) + "\n";
			sectionText += "	lds " + currentRegister() + ", " + expr.getName() + "\n";
			sectionText += "	lds " + currentRegister(1) + ", " + expr.getName() + "+1\n";
			expr.setRegister(currentRegisterCt);
			break;
		case UINT32_T:
		case INT32_T:
			sectionText += "	;; Copier la variable dans " + currentRegister() + ":" + currentRegister(1) + "\n";
			sectionText += "	lds " + currentRegister() + ", " + expr.getName() + "\n";
			sectionText += "	lds " + currentRegister(1) + ", " + expr.getName() + "+1\n";
			sectionText += "	lds " + currentRegister(2) + ", " + expr.getName() + "+2\n";
			sectionText += "	lds " + currentRegister(3) + ", " + expr.getName() + "+4\n";
			expr.setRegister(currentRegisterCt);
			break;
		default:
			sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 278)\n";
			break;
		}
	}

	// Purpose: Produire le code d'une ExprCONSTANT
	// Arguments: expr
	// Effect: sectionText contient le code
	// sectionData contient les données constantes déclarées
	// Uses:
	// - Type expr.getType() ; type de la constante
	// - String expr.getName() ; nom de la constante
	// - Object expr.getValue() ; valeur de la constante
	//
	@Override
	public void visit(ExprCONSTANT expr) {
		System.err.println("*** visit(ExprCONSTANT (" + expr.getType().getTag() + ") with " + this);
		switch (expr.getType().getTag()) {
		// La valeur de la constante est enregistrée dans le registre courant
		case PIN:
		case INT8_T:
		case UINT8_T: {
			sectionText += "	;; Enregistrer la constante " + expr.getValue() + " dans le registre "
					+ currentRegister() + "\n";
			sectionText += "	ldi " + currentRegister() + ", lo8(" + expr.getValue().toString() + ")\n";
			expr.setRegister(currentRegisterCt);
		}
			break;
		// La valeur de la constante est enregistrée dans le registre courant
		case INT16_T:
		case UINT16_T: {
			long value = (long) expr.getValue();
			short high = (short) ((value >> 8) & 0xFF);
			short low = (short) (value & 0xFF);
			sectionText += "	;; Enregistrer la constante " + expr.getValue() + " dans 2 registres "
					+ currentRegister() + ":" + currentRegister(1) + "\n";
			sectionText += "	ldi " + currentRegister() + ", " + low + "\n";
			sectionText += "	ldi " + currentRegister(1) + ", " + high + "\n";
			expr.setRegister(currentRegisterCt);
		}
		case UINT32_T:
		case F32_T: {
			long value = 0;
			if (expr.getValue() instanceof Float) {
				Float f = (Float)expr.getValue();
				value = Float.floatToIntBits(f);
			} else {
				value = (Long) expr.getValue();
			}
			int high = (short) (value >> 16);
			int low = (short) (value & 0xFFFF);
			sectionText += "	;; Enregistrer la constante " + expr.getValue() + " dans 4 registres "
					+ currentRegister() + ":" + currentRegister(3) + "\n";
			sectionText += "	ldi " + currentRegister() + ", " + (low & 0xFF) + "\n";
			sectionText += "	ldi " + currentRegister(1) + ", " + ((low >> 8) & 0xFF) + "\n";
			sectionText += "	ldi " + currentRegister(2) + ", " + (high & 0xFF) + "\n";
			sectionText += "	ldi " + currentRegister(3) + ", " + ((high >> 8) & 0xFF) + "\n";
			expr.setRegister(currentRegisterCt);
		}
			break;
		// La valeur de la constante est enregistrée dans le registre courant
		case STRING:
			sectionText += "	;; Enregistrer la constante " + expr.getValue() + " dans 2 registres "
					+ currentRegister() + ":" + currentRegister(1) + "\n";
			sectionText += "	ldi " + currentRegister() + ", lo8(" + String.valueOf(expr.getName()) + ")\n";
			sectionText += "	ldi " + currentRegister(1) + ", hi8(" + String.valueOf(expr.getName()) + ")\n";
			expr.setRegister(currentRegisterCt);
			break;
		default:
			sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 393)\n";
			break;

		}
	}

	// Purpose: Produire le code pour une instruction d'affectation
	// Arguments: stm
	// Effect: sectionText contient le code
	// Uses:
	// - Expr stm.getLeft() ; partie gauche de l'affectation
	// - Expr stm.getRight() ; partie droite de l'affectation
	// - Op stm.getOp() ; opérateur ajoutée à l'affectation (par exemple PLUS pour
	// +=)
	@Override
	public void visit(StmAFF stm) throws Exception {
		System.err.println("*** visit(StmAFF) with " + this);
		currentRegisterCt = 24;
		stm.getRight().accept(this);
		switch (stm.getLeft().getType().getTag()) {
		case INT8_T:
		case UINT8_T:
		case PIN:
			sectionText += "	;; Copier 1 octets à partir de " + currentRegister() + " vers cible\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + ", " + currentRegister() + "\n";
			break;
		case INT16_T:
		case UINT16_T:
			sectionText += "	;; Copier 2 octets à partir de " + currentRegister() + " vers cible\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + ", " + currentRegister() + "\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + "+1, " + currentRegister(1) + "\n";
			break;
		case INT32_T:
		case UINT32_T:
		case F32_T:
			sectionText += "	;; Copier 4 octets à partir de " + currentRegister() + " vers cible\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + ", " + currentRegister() + "\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + "+1, " + currentRegister(1) + "\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + "+2, " + currentRegister(2) + "\n";
			sectionText += "	sts " + ((ExprVAR) stm.getLeft()).getName() + "+3, " + currentRegister(3) + "\n";
			break;
		default:
			sectionText += ";; Unimplemented (CodeGeneratorVisitor.java line 435)\n";
			break;
		}
		currentRegisterCt += stm.getRight().size();
		
	}

	@Override
	public void visit(StmIF stm) throws Exception {
		System.err.println("*** visit(StmIF) with " + this);
		currentRegisterCt = 24;
		stm.getExpr().accept(this);
		sectionText += "	;; <Code de la partie <TEST>\n";
		sectionText += "	;; dont le résultat (zero si faux)\n";
		sectionText += "	;; est dans le registre " + currentRegister() + "\n";
		switch (stm.getToken().getCode() ){
			case 293: //IF_TOKEN
				sectionText += "	tst " + currentRegister() +"\n";
				sectionText += "	breq .L" + stm.getStm1().getId() + " \n";
				stm.getStm1().accept(this);
				// sectionText += "	rjmp .L" + stm.getStm1().getId()+1 + " \n";
				sectionText += ".L" + stm.getStm1().getId() + ": \n";
				break;
			case 286: //ELSE_TOKEN
				sectionText += "	tst " + currentRegister() +"\n";
				sectionText += "	breq .L" + stm.getStm1().getId() + " \n";
				stm.getStm1().accept(this);
				sectionText += "	rjmp .L" + stm.getStm2().getId() + " \n";
				sectionText += ".L" + stm.getStm1().getId() + ":\n" ;
				stm.getStm2().accept(this);
				sectionText += ".L" + stm.getStm2().getId() + ": \n";
				break;
			default:
				break;
		}
	}

	// Code Geneator des listes
	// Problème  rencontré : Récupérer le nom de la liste,
	// c'est pourquoi dans les lignes de génération des "sts"
	// Nous avons écris NAME_LIST au lieu du vrai nom
	@Override
	public void visit(ExprLIST expr) throws Exception {
		System.err.println("*** visit(ExprLIST (" + expr.getType().getTag() + ") with " + this);
		int taille = expr.getList().size();
		currentRegisterCt = 24;
		// Pour chaque élement à ajouter on vérifie son type et on l'ajoute
		for (int i = 0; i < taille; i++) {
			switch(expr.getList().get(i).getType().getTag()) {
				case INT8_T:
				case UINT8_T:
				case PIN:
					sectionText += "	;; Copie variable dans tableau sur 8 bits\n";
					sectionText += "	ldi " + currentRegister() + "," + expr.getList().get(i).getValue().toString() + "\n";// TROUVER COMMENT FAIRE + expr.getList().get(i);
					sectionText += "	sts " + "NAME_LIST" + "+32," + currentRegister();
					break;
				case INT16_T:
				case UINT16_T: {
					sectionText += "	;; Copie variable dans tableau sur 16 bits\n";
					long value = (long) expr.getList().get(i).getValue();
					short high = (short) ((value >> 8) & 0xFF);
					short low = (short) (value & 0xFF);
					sectionText += "	ldi " + currentRegister() + ", " + low + "\n";
					sectionText += "	ldi " + currentRegister(1) + ", " + high + "\n";
					sectionText += "	sts " + "NAME_LIST" + "+32," + currentRegister();
					sectionText += "	sts " + "NAME_LIST" + "+32+1," + currentRegister(1);
					expr.setRegister(currentRegisterCt);
					}	
					break;
				case INT32_T:
				case UINT32_T:
				case F32_T: {
					sectionText += "	;; Copie variable dans tableau sur 32 bits\n";
					long value = 0;
					if (expr.getList().get(i).getValue() instanceof Float) {
						Float f = (Float) expr.getList().get(i).getValue();
						value = Float.floatToIntBits(f);
					} else {
						value = (Long) expr.getList().get(i).getValue();
					}
					int high = (short) (value >> 16);
					int low = (short) (value & 0xFFFF);
					sectionText += "	ldi " + currentRegister() + ", " + (low & 0xFF) + "\n";
					sectionText += "	ldi " + currentRegister(1) + ", " + ((low >> 8) & 0xFF) + "\n";
					sectionText += "	ldi " + currentRegister(2) + ", " + (high & 0xFF) + "\n";
					sectionText += "	ldi " + currentRegister(3) + ", " + ((high >> 8) & 0xFF) + "\n";
					sectionText += "	sts " + "NAME_LIST" + "+32," + currentRegister();
					sectionText += "	sts " + "NAME_LIST" + "+32+1," + currentRegister(1);
					sectionText += "	sts " + "NAME_LIST" + "+32+2," + currentRegister(2);
					sectionText += "	sts " + "NAME_LIST" + "+32+3," + currentRegister(3);
					expr.setRegister(currentRegisterCt);
					}
					break;
				default:
					sectionText += ";; Default\n";
					break;
			}
		}
	}
		
	public void visit(StmWHILE stm) throws Exception {
		System.err.println("*** visit(StmWHILE) with " + this);
		currentRegisterCt = 24;
		stm.getExpr().accept(this);
		sectionText += "rjmp .L" + stm.getStm().getId() + " :\n";
		sectionText += ".L"+ (stm.getStm().getId()+1) +"\n";
		sectionText += "    ;; Code de la partie <STM> \n" ; //
		stm.getStm().accept(this);
		sectionText += ".L" + stm.getStm().getId() + " :\n";
		sectionText += "	;; <Code de la partie <TEST>\n";
		sectionText += "	;; dont le résultat (zero si faux)\n";
		sectionText += "	;; est dans le registre " + currentRegister() + "\n";
		sectionText += "	tst " + currentRegister() +"\n";
		sectionText += "	brne .L"+ (stm.getStm().getId()+1) + "\n";
	}

	@Override
	public void visit(StmDO stm) throws Exception {
		System.err.println("*** visit(StmDO) with " + this);
		currentRegisterCt = 24;
		stm.getExpr().accept(this);
		//sectionText += "rjmp .L" + stm.getStm().getId() + " :\n";
		sectionText += ".L"+ (stm.getStm().getId()+1) +"\n";
		sectionText += "    ;; Code de la partie <STM> \n" ; //
		stm.getStm().accept(this);
		sectionText += ".L" + stm.getStm().getId() + " :\n";
		sectionText += "	;; <Code de la partie <TEST>\n";
		sectionText += "	;; dont le résultat (zero si faux)\n";
		sectionText += "	;; est dans le registre " + currentRegister() + "\n";
		sectionText += "	tst " + currentRegister() +"\n";
		sectionText += "	brne .L"+ (stm.getStm().getId()+1) + "\n";

	}

	@Override
	public void visit(StmFOREACH stm) throws Exception {
		System.err.println("*** visit(StmFOREACH) with " + this);
 
		currentRegisterCt = 24;

		stm.getExpr().accept(this);
		sectionText += "	ldi " + currentRegister() + "," + stm.getVar() + "+1" + "\n";
		sectionText += "	std " + stm.getVar() + "+1" + "," + currentRegister() + "\n";
		sectionText += "	rjump .L" + stm.getId() + ":\n";
		sectionText += ".L"+ stm.getStm().getId() +":\n";
		stm.getStm().accept(this);
		sectionText += "	; ; On incrémente x (en retranchant 255)\n";
		sectionText += "	ldd " + currentRegister() + "," + stm.getVar() + "+1" + "\n";
		sectionText += "	subi " + currentRegister() + "," + "OxFF" + "\n";
		sectionText += "	std " + stm.getVar() + "+1" + "," + currentRegister() + "\n\n";
		sectionText += ".L" + stm.getId() + "\n";
		sectionText += "	;; On teste si égale à <MAX>\n";
		sectionText += "	;; on boucle sur .L"+ (stm.getStm().getId()+1)+"\n";
		sectionText += "	ldd " + currentRegister() + "," + stm.getVar() + "+1" + "\n";
		sectionText += "	cpi " + currentRegister() + "," + "<MAX>" + "\n";
		sectionText += "	brne .L"+ stm.getStm().getId() + "\n\n";
	}

	// Purpose: Donne le nom de ce visitor
	@Override
	public String toString() {
		return name;
	}

	// Purpose: Produit le code en tête du code main_program
	public void generateHead() {
		System.out.println("#include <avr/io.h>");
		System.out.println(".include \"data/m328Pdef.s\"");
		System.out.println("__SP_H__ = 0x3e");
		System.out.println("__SP_L__ = 0x3d");
		System.out.println("__SREG__ = 0x3f");
		System.out.println("__tmp_reg__ = 0");
		System.out.println("__zero_reg__ = 1");
	}

	// Purpose: Produit le code en tête du code main_program
	public void generateFoot() {
		System.out.println(".end");
		System.out.println(";; -------------------------------------------------------");
	}

	// Purpose: Produit le code en tête du code main_program
	private String begin() {
		String beginSection = "";
		beginSection += ";; fonction appelée par le programme principal C\n";
		beginSection += ".global main_program\n";
		beginSection += "main_program:\n";
		beginSection += "	push r28\n";
		beginSection += "	push r29\n";
		beginSection += "	push __tmp_reg__\n";
		beginSection += "	in r28,__SP_L__\n";
		beginSection += "	in r29,__SP_H__\n";
		return beginSection;
	}

	// Purpose: Produit le code en pied du code main_program
	private String end() {
		String endSection = "";
		endSection += "	pop __tmp_reg__\n";
		endSection += "	pop r29\n";
		endSection += "	pop r28\n";
		endSection += "	ret\n";
		endSection += "	.size	main_program, .-main_program\n";
		return endSection;
	}

	// Purpose: Produit le code du fichier *.s
	public void generateCode() {
		System.out.println(";; -------------------------------------------------------");
		System.out.println(";; Section du code enregistrées dans la mémoire FLASH");
		System.out.println("	.section .text\n");
		System.out.println(";; ajoute quelques fonctions de délai");
		System.out.println(".include \"data/delay.s\"\n");
		System.out.println(begin());
		System.out.print(sectionText);
		System.out.print(end());
	}

	@Override
	public String getPurpose() {
		return "Produit la partie code (.text) de l'AVR Assembler";
	}

	@Override
	public String getEffect() {
		return "Ajoute du code AVR Assembler à la variable\n//\tsectionText";
	}

}
