/* *** This file is given as part of the programming assignment. *** */

import java.util.ArrayList;

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token

    //create symbol table
    ArrayList<ArrayList<String> > symtbl;

    private void scan() {
	tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	program();
	//System.out.println("last toke " + tok.kind);
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
	/*while(tok.kind!=TK.EOF){
		scan();
		System.out.println(tok.kind + " ");	
	}*/
    }

    private void program() {
	symtbl = new ArrayList<ArrayList<String > >();
	block();
    }

    private void block(){
	ArrayList<String> scope = new ArrayList<String>();
	symtbl.add(scope);
	declaration_list();
	statement_list();
	symtbl.remove(symtbl.size() - 1);
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	while( is(TK.DECLARE) ) {
	    declaration();
	}
    }

    private void declaration() {
	String buffer;

	mustbe(TK.DECLARE);
	symtbl.get(symtbl.size()-1).add(tok.string);
	mustbe(TK.ID);

	while( is(TK.COMMA) ) {
	    scan();
	    symtbl.get(symtbl.size()-1).add(tok.string);
	    mustbe(TK.ID);
	}

	
    }

    private void statement_list() {
	while(is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF) || is(TK.SCOPE)){
		if(is(TK.ID) || is(TK.SCOPE)){
			assignment(); 		
		}//assignment
		else if(is(TK.PRINT)){
			print(); 
		}//print
		else if(is(TK.DO)){
			DO(); 	
		}//do
		else if(is(TK.IF)){
			if_statement(); 

		}//if

	}

    }

    private  void print(){
	mustbe(TK.PRINT);
	expr();

   }
   private void DO(){
	mustbe(TK.DO);
	guarded_command(); 
	mustbe(TK.DO2);
  }

    private void assignment(){
	ref_id();
	mustbe(TK.ASSIGN);
	expr(); 	

    }

    private void expr(){
	//System.out.print(tok.kind);
	term(); 
	while(is(TK.PLUS) || is(TK.MINUS)){
		//System.out.print(tok.kind);
		addop();
		term();		
	}

   }
  
   private void term(){

	//scan();
	factor();
	while(is(TK.TIMES) || is(TK.DIVIDE)){
		scan();
		factor(); 
	}

   }
	
   private void factor(){
	if(is(TK.LPAREN)){
		mustbe(TK.LPAREN);
		expr(); 
		mustbe(TK.RPAREN);
	}
	else if(is(TK.SCOPE) || is(TK.ID) ){
		ref_id();
	}
	else if(is(TK.NUM)){
		
		number();
	}
	
   } 

   private void ref_id(){
	String buffer = "0";
	String id ="";
	int lineNumber = tok.lineNumber; 
 	if(is(TK.SCOPE)){
	     mustbe(TK.SCOPE);
		if(is(TK.NUM)){
			System.out.println("bout to be the buffer:\t" + tok.string);
			buffer = tok.string; 
			number();
		}	
	}

	id = tok.string;
	mustbe(TK.ID);
	int level = Integer.parseInt(buffer); 
	//we know the scope exsists
	if((symtbl.size()-1) >= level && !(symtbl.get(level).isEmpty())){
		for(String ele : symtbl.get(level)){
		//we know the variable(ref id) exsists
			if(!ele.contains(id)){
				System.err.println(id + " is an undeclared variable at line " + lineNumber);
				System.exit(1);
			}//symbol is not in table

		}
	}//scope level exists
	else{

		System.err.println(id + " is an undeclared variable at line " + lineNumber);
		System.exit(1);	
	}
	 
	
   }
  private void if_statement(){
	mustbe(TK.IF);
	guarded_command();
	while(is(TK.ELSEIF)){
		scan();
		guarded_command(); 
	}
	if(is(TK.ELSE)){
		scan();
		block();
	}
	mustbe(TK.ENDIF);

  } 

  private void guarded_command(){
	expr(); 
	mustbe(TK.THEN);
	block();

   }
	
  private void addop(){

	if(is(TK.PLUS)){

		mustbe(TK.PLUS);
	}
	else if(is(TK.MINUS)){
		mustbe(TK.MINUS);
	}

   }

 private void multop(){

	if(is(TK.TIMES)){

		mustbe(TK.TIMES);
	}

	else if(is(TK.DIVIDE)){
		mustbe(TK.DIVIDE);
	}

}

 private void number(){
	//scan(); 
	//while(is(TK.NUM)){
		mustbe(TK.NUM);
	//}
 }

private void id(){
		//scan();
	//while(is(TK.ID)){
		mustbe(TK.ID);
		//scan();
	//}
}


    

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
