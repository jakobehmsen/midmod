/*
Pal: Pattern Action Language
*/

grammar Pal;

script: scriptElement*;
scriptElement: define | action;
define: pattern EQUALS_GREATER action;
pattern: pattern1 (COLON name=ID)?;
pattern1: pattern2 (PIPE pattern1)*;
pattern2: typedPattern | literalPattern;
typedPattern: type=ID;
literalPattern: string | number | listPattern | mapPattern;
listPattern: OPEN_SQ (pattern (COMMA pattern)*)? CLOSE_SQ;
mapPattern: OPEN_BRA (slotPattern (COMMA slotPattern)*)? CLOSE_BRA;
slotPattern: ID EQUALS pattern;
literal: string | number | list | map;
string: STRING;
number: NUMBER;
list: OPEN_SQ (action (COMMA action)*)? CLOSE_SQ;
map: OPEN_BRA (slot (COMMA slot)*)? CLOSE_BRA;
slot: ID EQUALS action;
action: expression1;
expression1: expression2 expression1Tail*;
expression1Tail: BIN_OP1 expression1;
expression2: expression3 expression2Tail*;
expression2Tail: BIN_OP2 expression1;
expression3: actionTarget isCall=QUESTION_MARK?;
actionTarget: access | literal | alwaysAction;
access: ID;
alwaysAction: OPEN_PAR (action (COMMA action)*) CLOSE_PAR;

EQUALS: '=';
EQUALS_GREATER: '=>';
OPEN_BRA: '{';
CLOSE_BRA: '}';
OPEN_SQ: '[';
CLOSE_SQ: ']';
OPEN_PAR: '(';
CLOSE_PAR: ')';
QUESTION_MARK: '?';
COMMA: ',';
COLON: ':';
PIPE: '|';
BIN_OP1: '+' | '-';
BIN_OP2: '*' | '/';
fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
ID: (LETTER | '_') (LETTER | '_' | DIGIT)*;
STRING :  '"' (ESC | ~["\\])* '"' ;
fragment ESC:   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
NUMBER
    :   '-'? INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP             // 1e10 -3e4
    |   '-'? INT                 // -3, 45
    ;
fragment INT :   '0' | [1-9] [0-9]* ; // no leading zeros
fragment EXP :   [Ee] [+\-]? INT ; // \- since - means "range" inside [...]
WS  :   [ \t\n\r]+ -> skip ;