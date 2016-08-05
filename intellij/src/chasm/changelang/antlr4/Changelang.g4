// Derived from https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4

grammar Changelang;

program: statement*;
statement: thisSlotAssign | expression;
thisSlotAssign: identifier '=' expression;
identifier: (isCapture='@' (isMulti='*')?)? ID;
string: STRING;
number: NUMBER;
bool: 'true' | 'false';
nil: 'null';
self: 'this';
objectLiteral: '{' (objectLiteralSlot (',' objectLiteralSlot)*)? '}';
objectLiteralSlot: identifier ':' expression;
array: '[' (expression (',' expression)*)? ']';
templateArray: '#' '[' expression ']';
capture: '@' (isMulti='*')? ID;
expression:
    (string | number | identifier invoke? | bool | nil | self | objectLiteral | array | capture | templateArray)
    expressionSlotAccess*
    (isClosedCapture=capture)?
    expressionSlotAssign?;
expressionSlotAccess: '.' identifier invoke?;
invoke: '(' (expression (',' expression)*)? ')';
expressionSlotAssign: '.' identifier '=' expression;

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