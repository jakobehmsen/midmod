grammar Reo;

block: statementOrExpression*;
statementOrExpression: statement | expression;
statement: variableDeclaration;
variableDeclaration: 'var' ID '=' expression;
expression: assignment | addExpression;
addExpression: lhs=mulExpression (('+'|'-') addExpressionOp)*;
addExpressionOp: mulExpression;
mulExpression: lhs=chainedExpression (('*'|'/') mulExpressionOp)*;
mulExpressionOp: chainedExpression;
chainedExpression: atomExpression (expressionTail);
expressionTail: expressionTailPart* expressionTailEnd?;
expressionTailPart: ('.' message) | slotAccess;
message: ID '(' (expression (',' expression)*)? ')';
expressionTailEnd: '.' slotAssignment;
slotAssignment: fieldSlotAssignment | methodSlotAssignment;
fieldSlotAssignment: selector '=' expression;
methodSlotAssignment: (selectorName selectorParameters) '=>' (singleExpressionBody=expression | '{' blockBody=block '}');
slotAccess: '.' access;
atomExpression: selfSend | access | string | number | embeddedExpression | self | objectLiteral;
selfSend: message;
access: ID | qualifiedSelector;
string: STRING;
number: NUMBER;
embeddedExpression: '(' expression ')';
self: 'this';
objectLiteral: '#' '{' slotAssignment* '}';
assignment: slotAssignment;
selector: unqualifiedSelector | qualifiedSelector;
unqualifiedSelector: ID selectorParameters?;
qualifiedSelector: SELECTOR selectorParameters?;
selectorParameters: '(' (ID (',' ID)*)? ')';
selectorName: ID | SELECTOR;

fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
SELECTOR :  '\'' ~['\\]* '\'';
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
SINGLE_LINE_COMMENT: '//' ~('\r' | '\n')* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;