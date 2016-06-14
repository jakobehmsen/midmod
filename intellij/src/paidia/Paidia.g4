grammar Paidia;

block: selector | (blockPart*);
blockPart: expression;
expression: addExpression;
addExpression: lhs=mulExpression addExpressionOp*;
addExpressionOp: ADD_OP mulExpression;
mulExpression: lhs=raiseExpression mulExpressionOp*;
mulExpressionOp: MUL_OP raiseExpression;
raiseExpression: lhs=chainedExpression raiseExpressionOp*;
raiseExpressionOp: RAISE_OP chainedExpression;
chainedExpression: atomExpression;
atomExpression: string | number | identifier | parameter | embeddedExpression;
string: STRING;
number: NUMBER;
identifier: ID;
parameter: QUESTION_MARK;
embeddedExpression: '(' embeddedExpressionContent? ')';
embeddedExpressionContent: selector | expression;
selector: ID | binaryOperator;
binaryOperator: ADD_OP | MUL_OP | RAISE_OP;

fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
ADD_OP: '+'|'-';
MUL_OP: '*'|'/';
RAISE_OP: '^';
QUESTION_MARK: '?';
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