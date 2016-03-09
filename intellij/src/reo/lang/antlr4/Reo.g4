grammar Reo;

block: blockElement*;
blockElement: statement | expression;
statement: assignment | returnStatement;
assignment: isDecl='var' ID '=' expression;
returnStatement: 'return' expression;
expression: expression1;
expression1: lhs=expression2 ((op='+'|op='-') expression1)*;
expression2: lhs=expression3 ((op='*'|op='/') expression2)*;
expression3: atom expressionTail;
atom: number | string | access | self | objectLiteral | arrayLiteral | function | embeddedExpression;
number: NUMBER;
string: STRING;
embeddedExpression: '(' expression ')';
expressionTail: expressionTailPart* expressionTailEnd?;
expressionTailPart: call | slotAccess | indexAccess;
call: '.' ID '(' (expression (',' expression)*)? ')';
slotAccess: '.' ID;
access: ID;
indexAccess: '[' expression ']';
expressionTailEnd: slotAssignment | indexAssign;
slotAssignment: '.' ID '=' expression;
indexAssign: '[' expression ']' '=' expression;
self: 'this';
objectLiteral: '{' (objectLiteralSlot (',' objectLiteralSlot)*)? '}';
objectLiteralSlot: ID ':' expression;
arrayLiteral: '[' (expression (',' expression)*)? ']';
function: functionParameters '=>' (expression | '{' block '}');
functionParameters: ID | '(' (ID (',' ID)*)? ')';

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