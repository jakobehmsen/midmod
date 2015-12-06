// Derived from https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4

grammar JSON;

script: json*;

json:   pair
    |   value
    ;

identifier: ID;
string: STRING;
number: NUMBER;
bool: 'true' | 'false';
nil: 'null';

object
    :   '{' pair (',' pair)* '}'
    |   '{' '}' // empty object
    ;

pair:   ID ':' value ;

array
    :   '[' value (',' value)* ']'
    |   '[' ']' // empty array
    ;

value
    :   string
    |   number
    |   object  // recursion
    |   array   // recursion
    |   identifier
    |   bool
    |   nil
    ;

fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
ID: (LETTER | '_') (LETTER | '_' | DIGIT)*;
STRING :  '"' (ESC | ~["\\])* '"' ;
fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;
NUMBER
    :   '-'? INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP             // 1e10 -3e4
    |   '-'? INT                 // -3, 45
    ;
fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ; // \- since - means "range" inside [...]
WS  :   [ \t\n\r]+ -> skip ;