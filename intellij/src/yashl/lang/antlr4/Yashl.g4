grammar Yashl;

forms: form*;
form: list|atom;
list: '(' form* ')';
atom: symbol|number|string;
symbol: ID;
number: NUMBER;
string: STRING;

fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
fragment SPECIAL_CHAR: '+'|'-'|'*'|'/'|'%'|'^'|'@'|'$'|':'|'.';
ID: (LETTER | SPECIAL_CHAR) (LETTER | SPECIAL_CHAR | DIGIT)*;
STRING :  '"' (ESC | ~["\\])* '"' ;
SELECTOR :  '\'' ~['\\]* '\'';
fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;
NUMBER
    :   '-'? INT '.' [0-9]+ EXP?
    |   '-'? INT EXP
    |   '-'? INT
    ;
fragment INT :   '0' | [1-9] [0-9]* ;
fragment EXP :   [Ee] [+\-]? INT ;
WS  :   [ \t\n\r]+ -> skip ;
SINGLE_LINE_COMMENT: ';' ~('\r' | '\n')* -> skip;