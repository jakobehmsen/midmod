grammar Reo;

block: blockElement*;
blockElement: statement | expression;
statement: declaration | returnStatement;
declaration: isDecl='var' ID ('=' expression)?;
returnStatement: 'return' expression;
expression: assignment | expression1;
assignment: ID '=' expression;
expression1: lhs=expression2 ((op='+'|op='-') expression1)*;
expression2: lhs=expression3 ((op='*'|op='/') expression2)*;
expression3: atom expressionTail;
atom:
    number | string | access | self | thisFrame |
    objectLiteral | arrayLiteral | function | primitive | embeddedExpression;
number: NUMBER;
string: STRING;
embeddedExpression: '(' expression ')';
expressionTail: expressionTailPart* expressionTailEnd?;
expressionTailPart: call | slotAccess | indexAccess;
call: '.' ID '(' (expression (',' expression)*)? ')';
slotAccess: '.' ID;
access: ID;
indexAccess: '[' expression ']';
expressionTailEnd: ('.' (slotAssignment | keyword)) | indexAssign;
keyword: ID_LOWER expression (ID_UPPER expression)*;
slotAssignment: fieldSlotAssignment | methodSlotAssignment;
fieldSlotAssignment: selector '=' expression;
methodSlotAssignment: selector '=>' (singleExpressionBody=expression | '{' blockBody=block '}');
indexAssign: '[' expression ']' '=' expression;
self: 'this';
thisFrame: 'thisFrame';
objectLiteral: '#' '{' slotAssignment* '}';
arrayLiteral: '#' '[' (expression (',' expression)*)? ']';
function:
    ( functionParameters '->' (singleExpressionBody=expression | '{' blockBody=block '}') )
    | '{' blockBody=block '}';
primitive: '$' ID '(' (expression (',' expression)*)? ')';
functionParameters: ID | '(' (ID (',' ID)*)? ')';
selector: selectorName (isMethod='(' (ID (',' ID)*)? ')')?;
selectorName: ID | SELECTOR;//'+' | '-' | '*' | '/' | '[]' | '[]=';

fragment DIGIT: [0-9];
fragment LETTER_UPPER: [A-Z];
fragment LETTER_LOWER: [a-z];
fragment LETTER: LETTER_UPPER|LETTER_LOWER;
ID: (LETTER | '_') (LETTER | '_' | DIGIT)*;
ID_LOWER: LETTER_LOWER (LETTER | '_' | DIGIT)* ':';
ID_UPPER: LETTER_UPPER (LETTER | '_' | DIGIT)* ':';
STRING :  '"' (ESC | ~["\\])* '"' ;
SELECTOR :  '\'' ~['\\]* '\'';
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
SINGLE_LINE_COMMENT: '//' ~('\r' | '\n')* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;