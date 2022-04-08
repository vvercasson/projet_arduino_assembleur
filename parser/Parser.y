%language "Java"

%define api.package {fr.ubordeaux.arduinoCode}
%define api.parser.class {Parser}
%define api.parser.public

%code imports {
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import fr.ubordeaux.arduinoCode.ast.*;
import fr.ubordeaux.arduinoCode.environment.*;
import fr.ubordeaux.arduinoCode.type.*;
import fr.ubordeaux.arduinoCode.visitor.*;
}

%code{

  private Environment<ExprVAR> varEnvironment = new MapEnvironment<>("Variables");
  private Environment<Type> typeEnvironment = new MapEnvironment<>("Types");
  private Visitor checkTypeVisitor = new CheckTypeVisitor("Type checking");
  private CodeGeneratorVisitor codeGeneratorVisitor = new CodeGeneratorVisitor("Code Generator");
  private Environment<Type> functionEnvironment = new MapEnvironment<>("Functions");
    
  /* public static String yytname(int code){ */
  /* 	  return String. SymbolKind.yytname_[code-256]; */
  /* 	} */

	public void trace(String message) {
	  // uncomment to trace
	  /* */
	  System.err.println(message);
	  /* */
	}

}

/* multi-character operators */
%token<Token>
	'<'
	LE_TOKEN "<="
	'>'
	GE_TOKEN ">="
	EQ_TOKEN "=="
	'!'
	NE_TOKEN "!="
	LL_TOKEN "<<"
	GG_TOKEN ">>"
	'&'
	AA_TOKEN "&&"
	'|'
	'+' '-' '*' '/' '%' 
	OO_TOKEN "||"
	PE_TOKEN "+="
	ME_TOKEN "-="
	TE_TOKEN "*="
	DE_TOKEN "/="
	CE_TOKEN "%="
	AE_TOKEN "&="
	OE_TOKEN "|="
	AAE_TOKEN "&&="
	OOE_TOKEN "||="
	PP_TOKEN "++"
	MM_TOKEN "--"
	'=' ':'

 /* KEYWORDS */
%token <Token>
	BEGIN_KEYWORD "begin"
	BOOLEAN_KEYWORD "boolean"
	BREAK_KEYWORD "break"
	BYTE_KEYWORD "byte"
	CASE_KEYWORD "case"
	CONTINUE_KEYWORD "continue"
	DO_KEYWORD "do"
	ELSE_KEYWORD "else"
	END_KEYWORD "end"
	ENUM_KEYWORD "enum"
	FALSE_KEYWORD "FALSE"
	FOREACH_KEYWORD "foreach"
	FROM_KEYWORD "from"
	FUNCTION_KEYWORD "function"
	IF_KEYWORD "if"
	INTEGER_KEYWORD "integer"
	IN_KEYWORD "in"
	LIST_KEYWORD "list"
	LOOP_KEYWORD "loop"
	OF_KEYWORD "of"
	PROCEDURE_KEYWORD "procedure"
	RANGE_KEYWORD "range"
	RETURN_KEYWORD "return"
	SET_KEYWORD "set"
	SMALL_KEYWORD "small"
	STRUCT_KEYWORD "struct"
	STRING_KEYWORD "string"
	SWITCH_KEYWORD "switch"
	TO_KEYWORD "to"
	TRUE_KEYWORD "TRUE"
	UNSIGNED_KEYWORD "unsigned"
	WHILE_KEYWORD "while"

 /* ARDUINO KEYWORDS */
%token <Token>
	PINMODE_KEYWORD "pinMode"
	DIGITALWRITE_KEYWORD "digitalWrite"
	ANALOGREFERENCE_KEYWORD "analogReference"
	ANALOGWRITE_KEYWORD "analogWrite"
	DIGITALREAD_KEYWORD "digitalRead"
	ANALOGREAD_KEYWORD "analogRead"
	LOW_KEYWORD "LOW"
	HIGH_KEYWORD "HIGH"
	INPUT_KEYWORD "INPUT"
	INPUTPULLUP_KEYWORD "INPUTPULLUP"
	OUTPUT_KEYWORD "OUTPUT"
	GETC_KEYWORD "getc"
	AVAILABLE_KEYWORD "available"
	PEEK_KEYWORD "peek"
	FLUSH_KEYWORD "flush"
	PUTC_KEYWORD "putc"
	PUTS_KEYWORD "puts"
	DELAY_KEYWORD "delay"
	DELAY_MS_KEYWORD "delay_ms"
	DELAY_S_KEYWORD "delay_s"
	DELAY_1S_KEYWORD "delay_1s"

/* TOKEN WITH CONTENT */
%token <String> IDENTIFIER
%token <Integer> INTEGER PIN
%token <Double> FLOAT
%token <String> STRING

%nonassoc LOWER_THAN_ELSE
%nonassoc "else"

%right '='
%left "||"
%left "&&"
%nonassoc '<' "<=" '>' ">=" "==" "!="
%nonassoc HIGHER_THAN_NOT
%left '-' '+'
%left '*' '/' '%'
%nonassoc HIGHER_THAN_MINUS
%right "++" "--"
%right "<<" ">>"
%left '|'
%left '&'
%nonassoc IDENTIFIER
 //%nonassoc ':'

%type <Stm> stm
%type <StmCASE> case_stm
%type < List<Stm> > list_of_stms
%type < List<StmCASE> > list_of_case_stms
%type <Expr> expression
%type <Expr> left_part
%type <Expr> ATmega328p_procedure
%type <Expr> ATmega328p_expression
%type < List<Expr> > list_of_expressions
%type <ExprCONSTANT> constant_expression
%type <Type> int type
%type < List<String> > list_of_identifiers
%type < List<TypeFIELD> > list_of_parameters
%type <TypeFIELD> parameter
%type < List<TypeFIELD> > list_of_fields
%type <TypeFIELD> field

%start program

%%
program:
	list_of_definitions "begin" list_of_stms "end" {
            trace("*** REDUCE: program -> list_of_definitions \"begin\" list_of_stms \"end\"");
	    try{
	      for (Stm stm : $3){
			stm.accept(codeGeneratorVisitor);
	    	}
	    } catch (Exception e) {
	      System.err.println(e.getMessage());
	      return YYERROR;
	    }
	    codeGeneratorVisitor.generateCode();
	    return YYABORT;
 	}
	;

list_of_definitions:
	/* empty */ { trace("*** REDUCE: list_of_definitions -> "); }
	| list_of_definitions definition {
	  trace("*** REDUCE: list_of_definitions -> definition list_of_definitions");
 	}
	;

list_of_stms:
	stm { trace("*** REDUCE: list_of_stms -> stm"); 
	  try{
	    $1.accept(checkTypeVisitor);
	  } catch (Exception e) {
	    System.err.println(e.getMessage());
	    return YYERROR;
	  }
	  List<Stm> list = new ArrayList<>();
	  list.add($1);
	  $$ = list;
	}
	| list_of_stms stm {
	  trace("*** REDUCE: list_of_stms -> list_of_stms stm"); 
	  List<Stm> list = $1;
	  try{
	    $2.accept(checkTypeVisitor);
	  } catch (Exception e) {
	    System.err.println(e.getMessage());
	    return YYERROR;
	  }
	  list.add($2);
	  $$ = list;
	}
	;

definition:
	var_definition { trace("*** REDUCE: definition -> var_definition"); }
	| type_definition { trace("*** REDUCE: definition -> type_definition"); }
	| function_decl { trace("*** REDUCE: definition -> function_decl"); }
	| procedure_decl { trace("*** REDUCE: definition -> procedure_decl"); }
	| function_definition { trace("*** REDUCE: definition -> function_definition"); }
	| procedure_definition { trace("*** REDUCE: definition -> procedure_definition"); }
	;

var_definition:
	list_of_identifiers ':' type ';' {
	  trace("*** REDUCE: var_definition -> list_of_identifiers \':\' type \';\'");
	  for (String id : $1) {
	    ExprVAR var = new ExprVAR(id, $3);
	    varEnvironment.put(id, var);
	    try{
	      var.accept(codeGeneratorVisitor);
	    } catch (Exception e) {
	      System.err.println(e.getMessage());
	      return YYERROR;
	    }
	    
	  }
	}
	;

list_of_identifiers:
	IDENTIFIER { trace("*** REDUCE: list_of_identifiers -> IDENTIFIER");
	  List<String> list = new ArrayList<>();
	  list.add($1);
	  $$ = list;
	}
	| list_of_identifiers ',' IDENTIFIER {
	  trace("*** REDUCE: list_of_identifiers -> list_of_identifiers \',\' IDENTIFIER");
	  List<String> list = $1;
	  list.add($3);
	  $$ = list;
	  }
	;

type_definition:
	IDENTIFIER '=' type ';' {
	  trace("*** REDUCE: type_definition -> IDENTIFIER \'=\' type \';\'");
	  typeEnvironment.put($1, $3);
	}
 	;

function_decl:
	"function" IDENTIFIER '(' list_of_parameters ')' ':' type ';' {
	  trace("*** REDUCE: function_decl -> \"function\" IDENTIFIER \'(\' list_of_parameters \')\' \':\' type \';\'");
	  functionEnvironment.put($2, new TypeFUNCTION($4, $7));
	}
	;

procedure_decl:
	"procedure" IDENTIFIER '(' list_of_parameters ')' ';' {
	  trace("*** REDUCE: procedure_decl -> \"procedure\" IDENTIFIER \'(\' list_of_parameters \')\' \';\'");
	  functionEnvironment.put($2, new TypePROCEDURE($4));
	}
	;

function_definition:
	"function" IDENTIFIER '(' list_of_parameters ')' ':' type '{' {
		functionEnvironment.put($2, new TypeFUNCTION($4, $7));
	} 
	block '}' {
	  trace("*** REDUCE: function_definition -> \"function\" IDENTIFIER \'(\' list_of_parameters \')\' \':\' type \'{\' block \'}\'");
	  }
	;

procedure_definition:
	"procedure" IDENTIFIER '(' list_of_parameters ')' '{' {	  
		functionEnvironment.put($2, new TypePROCEDURE($4));
	} 
	block '}' {
	  trace("*** REDUCE: procedure_definition -> \"procedure\" IDENTIFIER \'(\' list_of_parameters \')\' \' {\' block \'}\'");
	}
	;

list_of_parameters:
	parameter {
	  trace("*** REDUCE: list_of_parameters -> parameter");
	  List<TypeFIELD> list = new ArrayList<>();
	  list.add($1);
	  $$ = list;
	  }
	| list_of_parameters ',' parameter {
	  trace("*** REDUCE: list_of_parameters -> list_of_parameters \',\' parameter");
	  List<TypeFIELD> list = $1;
	  list.add($3);
	  $$ = list;
	}
	;

parameter:
	IDENTIFIER ':' type { trace("*** REDUCE: parameter -> IDENTIFIER \':\' type "); $$ = new TypeTree(Type.Tag.FIELD, $1, $3); }
	;

block:
	list_of_stms { trace("*** REDUCE: block -> list_of_stms"); }
	;

type:
	"boolean" { trace("*** REDUCE: type -> \"boolean\""); $$ = new TypeTree(Type.Tag.BOOLEAN); }
	| int { trace("*** REDUCE: type -> int"); $$ = $1; }
	| "string" '[' INTEGER ']' { trace("*** REDUCE: type -> \"string\" \'[\' INTEGER \']\'"); $$ = new TypeTree(Type.Tag.STRING, $3); }
	| "range" "of" type { trace("*** REDUCE: type -> \"range\" \"of\" type"); $$ = new TypeTree(Type.Tag.RANGE, $3); }
	| "set" "of" type { trace("*** REDUCE: type -> \"set\" \"of\" type"); $$ = new TypeTree(Type.Tag.SET, $3); }
	| "list" "of" type { trace("*** REDUCE: type -> \"list\" \"of\" type"); $$ = new TypeTree(Type.Tag.LIST, $3); }
	| "enum" '(' list_of_identifiers ')' { trace("*** REDUCE: type -> \"enum\" \'(\' list_of_identifiers \')\'"); $$ = new TypeTree(Type.Tag.ENUM, $3); }
	| "struct" '{' list_of_fields '}' { trace("*** REDUCE: type -> \"struct\" \'{\' list_of_fields \'}\'"); $$ = new TypeTree(Type.Tag.STRUCT, $3); }
	;

list_of_fields:
	field {
	  trace("*** REDUCE: list_of_fields -> field");
	  List<TypeFIELD> list = new ArrayList<>();
	  list.add($1);
	  $$ = list;
	}
	| list_of_fields field {
	  trace("*** REDUCE: list_of_fields -> field list_of_fields");
	  List<TypeFIELD> list = $1;
	  list.add($2);
	  $$ = list;
	}
	;

field:
	IDENTIFIER ':' type ';' { 
		trace("*** REDUCE: field -> IDENTIFIER \':\' type \';\'"); 
		$$ = new TypeFIELD($1, $3); 
	}
	;

int:
	"byte" { trace("*** REDUCE: int -> \"byte\""); $$ = new TypeTree(Type.Tag.INT8_T); }
	| "unsigned" "byte" { trace("*** REDUCE: int -> \"unsigned\" \"byte\""); $$ = new TypeTree(Type.Tag.UINT8_T); }
	| "small" "integer" { trace("*** REDUCE: int -> \"small\" \"integer\""); $$ = new TypeTree(Type.Tag.INT16_T); }
	| "unsigned" "small" "integer" { trace("*** REDUCE: int -> \"unsigned\" \"small\" \"integer\""); $$ = new TypeTree(Type.Tag.UINT16_T); }
	| "integer" { trace("*** REDUCE: int -> \"integer\""); $$ = new TypeTree(Type.Tag.INT32_T); }
	| "unsigned" "integer" { trace("*** REDUCE: int -> \"unsigned\" \"integer\""); $$ = new TypeTree(Type.Tag.UINT32_T); }
	;

stm:
	var_definition ';' { trace("*** REDUCE: stm -> var_definition \';\'"); } 
	| left_part '=' expression ';' { trace("*** REDUCE: stm -> left_part \'=\' expression \';\'"); $$ = new StmAFF($1, $3); }
	| left_part "+=" expression ';' { trace("*** REDUCE: stm -> left_part \"+=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.PLUS, $1, $3); }
	| left_part "-=" expression ';' { trace("*** REDUCE: stm -> left_part \"-=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.MINUS, $1, $3);}
	| left_part "*=" expression ';' { trace("*** REDUCE: stm -> left_part \"*=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.MUL, $1, $3);}
	| left_part "/=" expression ';' { trace("*** REDUCE: stm -> left_part \"/=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.DIV, $1, $3);}
	| left_part "%=" expression ';' { trace("*** REDUCE: stm -> left_part \"%=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.PER, $1, $3);}
	| left_part "&=" expression ';' { trace("*** REDUCE: stm -> left_part \"&=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.LAND, $1, $3);}
	| left_part "|=" expression ';' { trace("*** REDUCE: stm -> left_part \"|=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.LOR, $1, $3);}
	| left_part "&&=" expression ';' { trace("*** REDUCE: stm -> left_part \"&&=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.AND, $1, $3);}
	| left_part "||=" expression ';' { trace("*** REDUCE: stm -> left_part \"||=\" expression \';\'"); $$ = new StmAFF(StmAFF.Op.OR, $1, $3);}
	| "switch" '(' expression ')' '{' list_of_case_stms '}' { trace("*** REDUCE: stm -> \"switch\" \'(\' expression \')\' \'{\' list_of_case_stms \'}\'"); $$ = new StmSWITCH($3, $6);}
	| "while" '(' expression ')' stm { trace("*** REDUCE: stm -> \"while\" \'(\' expression \')\' stm"); $$ = new StmWHILE($3, $5);}
	| "do" stm "while" '(' expression ')' ';' { trace("*** REDUCE: stm -> \"do\" stm \"while\" \'(\' expression \')\' \';\'"); $$ = new StmDO($2, $5);}
	| "loop" stm { trace("*** REDUCE: stm -> \"loop\" stm"); $$ = new StmLOOP($2);}
 	| "break" ';' { trace("*** REDUCE: stm -> \"break\" \';\'"); $$ = new StmBREAK();}
 	| "continue" ';' { trace("*** REDUCE: stm -> \"continue\" \';\'"); $$ = new StmBREAK();}
 	| "return" expression ';' { trace("*** REDUCE: stm -> \"return\" expression \';\'"); $$ = new StmRETURN($2);}
	| ATmega328p_procedure { trace("*** REDUCE: stm -> ATmega328p_procedure"); $$ = new StmExpr($1); }
	| '{' list_of_stms '}' { trace("*** REDUCE: stm -> '{' list_of_stms '}'"); $$ = new StmSeq($2); }
	;

list_of_case_stms:
	case_stm {
	  trace("*** REDUCE: list_of_case_stms -> case_stm");
	  List<StmCASE> list = new ArrayList<>();
	  list.add($1);
	  $$ = list;
	}
	| list_of_case_stms case_stm {
	  trace("*** REDUCE: list_of_case_stms -> list_of_case_stms case_stm");
	  List<StmCASE> list = $1;
	  list.add($2);
	  $$ = list;
	}
	;

case_stm:
	"case" expression ':' stm {
	  trace("*** REDUCE: case_stm -> \"case\" expression \':\' stm");
	  $$ = new StmCASE($2, $4);
	}
	;

left_part:
	IDENTIFIER {
	  trace("*** REDUCE: left_part -> IDENTIFIER");
	  ExprVAR var = null;
	  try {
	    var = varEnvironment.get($1);
	  } catch (EnvironmentException e) {
	    yyerror(e.getMessage());
	    return YYABORT;
	  }
	  $$ = var;
	}
 	| IDENTIFIER '[' expression ']' {
	  trace("*** REDUCE: left_part -> IDENTIFIER \'[\' expression \']\'");
	  ExprVAR var = null;
	  try {
	    var = varEnvironment.get($1);
	  } catch (EnvironmentException e) {
	    yyerror(e.getMessage());
	    return YYABORT;
	  }
	  $$ = new ExprARRAY($3, var);
	}
	;

expression:
	constant_expression { trace("*** REDUCE: expression -> constant_expression"); $$ = $1; }
	| left_part { trace("*** REDUCE: expression -> left_part"); $$ = $1; }
	| IDENTIFIER '(' list_of_expressions ')' { trace("*** REDUCE: expression -> IDENTIFIER \'(\' list_of_expressions \')\'"); $$ = new ExprFUNCTION($1, $3); }
	| expression '+' expression { trace("*** REDUCE: expression -> expression \'+\' expression"); $$ = new ExprBinary(ExprBinary.Op.PLUS, $1, $3); }
	| expression '*' expression { trace("*** REDUCE: expression -> expression \'*\' expression"); $$ = new ExprBinary(ExprBinary.Op.PLUS, $1, $3); }
	| expression '-' expression { trace("*** REDUCE: expression -> expression \'-\' expression"); $$ = new ExprBinary(ExprBinary.Op.MUL, $1, $3); }
	| expression '/' expression { trace("*** REDUCE: expression -> expression \'/\' expression"); $$ = new ExprBinary(ExprBinary.Op.MINUS, $1, $3); }
	| expression '%' expression { trace("*** REDUCE: expression -> expression \'%\' expression"); $$ = new ExprBinary(ExprBinary.Op.PERC, $1, $3); }
	| '-' expression %prec HIGHER_THAN_MINUS { trace("*** REDUCE: expression -> \'-\' expression"); $$ = new ExprUnary(ExprUnary.Op.MINUS, $2); }
	| expression "++" { trace("*** REDUCE: expression -> expression \"++\""); $$ = new ExprUnary(ExprUnary.Op.PLUSPLUS, $1); }
	| expression "--" { trace("*** REDUCE: expression -> expression \"--\""); $$ = new ExprUnary(ExprUnary.Op.MINUSMINUS, $1); }
	| "++" expression { trace("*** REDUCE: expression -> \"++\" expression"); $$ = new ExprUnary(ExprUnary.Op.PLUSPLUSP, $2); }
	| "--" expression { trace("*** REDUCE: expression -> \"--\" expression"); $$ = new ExprUnary(ExprUnary.Op.MINUSMINUSP, $2); }
	| expression '<' expression { trace("*** REDUCE: expression -> expression \'<\' expression"); $$ = new ExprBinary(ExprBinary.Op.GT, $1, $3); }
	| expression "<=" expression { trace("*** REDUCE: expression -> expression \"<=\" expression"); $$ = new ExprBinary(ExprBinary.Op.GE, $1, $3); }
	| expression '>' expression { trace("*** REDUCE: expression -> expression \'>\' expression"); $$ = new ExprBinary(ExprBinary.Op.LT, $1, $3); }
	| expression ">=" expression { trace("*** REDUCE: expression -> expression \">=\" expression"); $$ = new ExprBinary(ExprBinary.Op.LE, $1, $3); }
	| expression "==" expression { trace("*** REDUCE: expression -> expression \"==\" expression"); $$ = new ExprBinary(ExprBinary.Op.EQ, $1, $3); }
	| expression "!=" expression { trace("*** REDUCE: expression -> expression \"!=\" expression"); $$ = new ExprBinary(ExprBinary.Op.NE, $1, $3); }
	| expression "<<" expression { trace("*** REDUCE: expression -> expression \"<<\" expression"); $$ = new ExprBinary(ExprBinary.Op.BSL, $1, $3); }
	| expression ">>" expression { trace("*** REDUCE: expression -> expression \">>\" expression"); $$ = new ExprBinary(ExprBinary.Op.BSR, $1, $3); }
	| expression '&' expression { trace("*** REDUCE: expression -> expression \'&\' expression"); $$ = new ExprBinary(ExprBinary.Op.BAND, $1, $3); }
	| expression '|' expression { trace("*** REDUCE: expression -> expression \'|\' expression"); $$ = new ExprBinary(ExprBinary.Op.BOR, $1, $3); }
	| expression "&&" expression { trace("*** REDUCE: expression -> expression \"&&\" expression"); $$ = new ExprBinary(ExprBinary.Op.AND, $1, $3); }
	| expression "||" expression { trace("*** REDUCE: expression -> expression \"||\" expression"); $$ = new ExprBinary(ExprBinary.Op.OR, $1, $3); }
    	| '!' expression %prec HIGHER_THAN_NOT { trace("*** REDUCE: expression -> \'!\' expression"); $$ = new ExprUnary(ExprUnary.Op.NOT, $2); }
	| '(' expression ')' { trace("*** REDUCE: expression -> \'(\' expression \')\'"); $$ = $2; }
 	| ATmega328p_expression { trace("*** REDUCE: expression -> ATmega328p_expression"); $$ = $1; }
	;

list_of_expressions:
	expression {
	  trace("*** REDUCE: list_of_expressions -> expression");
	  List<Expr> list = new ArrayList<>();
	  list.add($1);
	  $$ = list;
	}
	| list_of_expressions ',' expression {
	  trace("*** REDUCE: list_of_expressions -> expression \',\' list_of_expressions");
	  List<Expr> list = $1;
	  list.add($3);
	  }
	;

constant_expression:
	"TRUE" { 
		trace("*** REDUCE: constant_expression -> \"true\""); 
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.BOOLEAN), true); 
	}
	| "FALSE" { 
		trace("*** REDUCE: constant_expression -> \"false\""); 
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.BOOLEAN), false); 
	}
	| INTEGER { 
		trace("*** REDUCE: constant_expression -> INTEGER");
	  	$$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT32_T), $1);
	}
	| FLOAT {
	  trace("*** REDUCE: constant_expression -> FLOAT");
	  $$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT32_T), $1);
	}
	| STRING {
	  trace("*** REDUCE: constant_expression -> STRING");
	  $$ = new ExprCONSTANT(new TypeTree(Type.Tag.STRING), $1);
	}
	;

/***
* void pinMode(uint8_t pin, uint8_t mode);
* void digitalWrite(uint8_t pin, uint8_t val);
* void analogReference(uint8_t mode);
* void analogWrite(uint8_t pin, int val);
***/
ATmega328p_procedure:

	"pinMode" '(' PIN ',' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"pinMode\" \'(\' expression \',\' expression \')\' \';\' ");  
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $5)); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.pinMode, list); 
	}
	
	| "digitalWrite" '(' PIN ',' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"digitalWrite\" \'(\' PIN \',\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		$5.cast(Type.Tag.UINT8_T);
		list.add($5); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.digitalWrite, list); 
	}
	
	| "analogReference" '(' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"analogReference\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.analogReference, list); 
	}
	
	| "analogWrite" '(' PIN ',' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"analogWrite\" \'(\' PIN \',\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT16_T), $5)); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.analogWrite, list); 
	}

	| "peek" '(' ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"peek\" \'(\' \')\' \';\'"); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.peek); 
	}

	| "flush" '(' ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"flush\" \'(\' \')\' \';\'"); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.flush); 
	}

	| "putc" '(' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"putc\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.putc, list); 
	}

	| "puts" '(' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"puts\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		list.add($3); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.puts, list); 
	}

	| "delay_1s" '(' ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"puts\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.delay_1s); 
	}

	| "delay" '(' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"puts\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		$3.cast(Type.Tag.UINT16_T);
		list.add($3); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.delay, list); 
	}

	| "delay_s" '(' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"puts\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		$3.cast(Type.Tag.UINT16_T);
		list.add($3); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.delay_s, list); 
	}

	| "delay_ms" '(' expression ')' ';' { 
		trace("*** REDUCE: ATmega328p_procedure -> \"puts\" \'(\' expression \')\' \';\'"); 
		List<Expr> list = new ArrayList<>();
		$3.cast(Type.Tag.UINT16_T);
		list.add($3); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.delay_ms, list); 
	}
	;

/***
*
***/
ATmega328p_expression:

	"LOW" { 
		trace("*** REDUCE: ATmega328p_expression -> \"LOW\"");  
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), (Integer)0); 
	}

	| "HIGH" { 
		trace("*** REDUCE: ATmega328p_expression -> \"HIGH\""); 
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), (Integer)255); 
	}

	| "INPUT" { 
		trace("*** REDUCE: ATmega328p_expression -> \"INPUT\""); 
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), (Integer)0x0); 
	}

	| "INPUTPULLUP" { 
		trace("*** REDUCE: ATmega328p_expression -> \"INPUTPULLUP\""); 
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), (Integer)0x2); 
	}

	| "OUTPUT" { 
		trace("*** REDUCE: ATmega328p_expression -> \"OUTPUT\""); 
		$$ = new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), (Integer)0x1); 
	}

   	| "digitalRead" '(' PIN ')' { 
   		trace("*** REDUCE: ATmega328p_expression -> \"digitalRead\" \'(\' PIN \')\'"); 
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.digitalRead, list); 
   	}

   	| "analogRead" '(' PIN ')' { 
   		trace("*** REDUCE: ATmega328p_expression -> \"analogRead\" \'(\' PIN \')\'"); 
		List<Expr> list = new ArrayList<>();
		list.add(new ExprCONSTANT(new TypeTree(Type.Tag.UINT8_T), $3)); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.analogRead, list); 
   	}

	| "getc" '(' ')' { 
		trace("*** REDUCE: ATmega328p_expression -> \"getc\" \'(\' \')\'"); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.getc); 
	}

   	| "available" '(' ')' { 
   		trace("*** REDUCE: ATmega328p_expression -> \"available\" \'(\' \')\'"); 
		$$ = new ExprFUNCTION(ExprFUNCTION.Defined.available); 
   	}
	;

%%
