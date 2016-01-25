/*
Pal: Pattern Action Language
*/

grammar Pal;

script: scriptElement*;
scriptElement: defineNameAndParams | define | action;
defineNameAndParams: name=ID OPEN_PAR (pattern (COMMA pattern)*)? CLOSE_PAR EQUALS action;
define: (name=ID EQUALS)? pattern EQUALS_GREATER action;
pattern: pattern1 (repeatPattern=ELLIPSIS)? (name=ID)? | isAction=mesaAction;
pattern1: pattern2 (PIPE pattern1)*;
pattern2: referencePattern | typedPattern | literalPattern | notPattern | embeddedPattern;
referencePattern: name=ID;
typedPattern: type=TYPE_CODE;
literalPattern: string | number | listPattern | mapPattern | anything;
listPattern: OPEN_SQ (pattern (COMMA pattern)*)? CLOSE_SQ;
mesaAction: DOLLAR action;
notPattern: EXCLAMATION pattern1;
embeddedPattern: OPEN_PAR pattern CLOSE_PAR;
mapPattern:
    OPEN_BRA
    (
        (isMap=slotPattern (COMMA slotPattern)*)?
        | (isRuleMap=pattern (COMMA pattern)*)
    )
    CLOSE_BRA;
anything: UNDERSCORE;
slotPattern: ID EQUALS pattern;
literal: string | number | list | map;
string: STRING;
number: NUMBER;
list: OPEN_SQ (action (COMMA action)*)? CLOSE_SQ;
map:
    OPEN_BRA
    (
        (isMap=slot (COMMA slot)*)?
        | (isRuleMap=define define*)
    )
    CLOSE_BRA;
slot: ID EQUALS action;
action: expression1;
expression1: expression2 expression1Tail*;
expression1Tail: BIN_OP1 expression1;
expression2: expression3 expression2Tail*;
expression2Tail: BIN_OP2 expression1;
expression3: actionTarget isCall=QUESTION_MARK?;
actionTarget: access | literal | patternLiteral | nameAndArgs;
access: ID;
nameAndArgs: name = ID OPEN_PAR (action (COMMA action)*)? CLOSE_PAR;
patternLiteral: HASH pattern;

DOLLAR: '$';
HASH: '#';
EXCLAMATION: '!';
ELLIPSIS: '...';
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
TYPE_CODE: '%' LETTER;
UNDERSCORE: '_';
fragment DIGIT: [0-9];
fragment LETTER: [A-Z]|[a-z];
ID: LETTER (LETTER | DIGIT)*;
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
SINGLE_LINE_COMMENT: '//' ~('\r' | '\n')* -> skip;
MULTI_LINE_COMMENT: '/*' .*? '*/' -> skip;