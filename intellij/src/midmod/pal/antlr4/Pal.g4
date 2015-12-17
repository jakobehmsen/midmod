/*
Pal: Pattern Action Language
*/

grammar Pal;

script: scriptElement*;
scriptElement: define | action;
define: pattern EQUALS_GREATER action;
pattern: pattern1 (COLON name=identifier)?;
pattern1: pattern2 (PIPE pattern1)*;
pattern2: typedPattern | literalPattern;
typedPattern: type=identifier;
literalPattern: string | number | listPattern;
listPattern: OPEN_SQ (pattern (COMMA pattern)*)? CLOSE_SQ;
literal: string | number | list;
string: STRING;
number: NUMBER;
list: OPEN_SQ (action (COMMA action)*)? CLOSE_SQ;
action: expression1;
expression1: expression2 (BIN_OP1 expression1)*;
expression2: actionTarget;
actionTarget: (identifier | literal | alwaysAction) isCall=QUESTION_MARK?;
identifier: ID;
alwaysAction: OPEN_PAR (action (COMMA action)*) CLOSE_PAR;

EQUALS_GREATER: '=>';
OPEN_SQ: '[';
CLOSE_SQ: ']';
OPEN_PAR: '(';
CLOSE_PAR: ')';
QUESTION_MARK: '?';
COMMA: ',';
COLON: ':';
PIPE: '|';
BIN_OP1: '+' | '-';
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