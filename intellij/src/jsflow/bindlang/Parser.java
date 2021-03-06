package jsflow.bindlang;

import jsflow.bindlang.antlr4.BindBaseVisitor;
import jsflow.bindlang.antlr4.BindLexer;
import jsflow.bindlang.antlr4.BindParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parser {
    public static String replace(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        BindLexer lexer = new BindLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        BindParser parser = new BindParser(tokenStream);

        // Traverse through block; replace only bindable expressions; forward others as they are

        //return parse(parser.block(), new ArrayList<>(), new ArrayList<>());
        int[] indexHolder = new int[]{0};
        String str = parse(charStream, parser.block(), indexHolder);
        if(indexHolder[0] < text.length())
            str += text.substring(indexHolder[0]);
        return str;
    }


    private static String parse(CharStream charStream, ParserRuleContext ctx, int[] indexHolder) {
        if(ctx.children == null || ctx instanceof TerminalNode) {
            return "";
        } else {
            return ctx.children.stream().map(x -> {
                if (((ParserRuleContext) x).getRuleIndex() == BindParser.RULE_bindable) {
                    return x.accept(new BindBaseVisitor<String>() {
                        @Override
                        public String visitBindable(BindParser.BindableContext bindableContext) {
                            String preText = charStream.getText(new Interval(indexHolder[0], bindableContext.getStart().getStartIndex() - 1));
                            indexHolder[0] = bindableContext.getStop().getStopIndex() + 1;
                            return preText + parseSource(bindableContext.atom(), new ArrayList<>(), new ArrayList<>());
                        }
                    });
                }

                return parse(charStream, (ParserRuleContext) x, indexHolder);
            }).collect(Collectors.joining());
        }
    }

    private static String parse(ParserRuleContext parseCtx, List<String> dependencies, List<String> parameters) {
        return parseCtx.accept(new BindBaseVisitor<String>() {
            /*@Override
            public String visitScript(BindParser.ScriptContext ctx) {
                return ctx.binding().stream().map(x ->
                    parse(x, dependencies, parameters)).collect(Collectors.joining("\n"));
            }*/

            @Override
            public String visitAtom(BindParser.AtomContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public String visitBlock(BindParser.BlockContext ctx) {
                return ctx.blockElement().stream().map(x ->
                    parse(x, dependencies, parameters)).collect(Collectors.joining("\n"));
            }

            @Override
            public String visitExpression(BindParser.ExpressionContext ctx) {
                return parseSource(ctx, dependencies, parameters);
            }

            /*@Override
            public String visitBinding(BindParser.BindingContext ctx) {
                return ctx.getChild(0).accept(new BindBaseVisitor<String>() {
                    @Override
                    public String visitStatement(BindParser.StatementContext ctx) {
                        String targetExpressionStr =
                            "core.memberTarget(" + ctx.targetExpression.getText() + ", \"" + ctx.ID().getText() + "\")";
                        String sourceExpressionStr = parseSource(ctx.sourceExpression, dependencies, parameters);

                        return "core.bind(" + sourceExpressionStr + ", " + targetExpressionStr + ")";
                    }

                    @Override
                    public String visitExpression(BindParser.ExpressionContext ctx) {
                        return parseSource(ctx, dependencies, parameters);
                    }
                });
            }*/
        });
    }

    private static String parseSource(ParserRuleContext sourceCtx, List<String> dependencies, List<String> parameters) {
        return sourceCtx.accept(new BindBaseVisitor<String>() {
            @Override
            public String visitExpression(BindParser.ExpressionContext ctx) {
                return parseSource(ctx.expression1(), dependencies, parameters);
            }

            @Override
            public String visitExpression1(BindParser.Expression1Context ctx) {
                String str = parseSource(ctx.expression2(), dependencies, parameters);

                for (BindParser.Expression1Context rhsCtx : ctx.expression1()) {
                    String lhsStr = str;
                    String rhsStr = parseSource(rhsCtx, dependencies, parameters);
                    str = "core.reducer([" + lhsStr + ", " + rhsStr + "], function(x, y) {return x " + ctx.op.getText() + " y;})";
                }

                return str;
            }

            @Override
            public String visitExpression2(BindParser.Expression2Context ctx) {
                String str = parseSource(ctx.expression3(), dependencies, parameters);

                for (BindParser.Expression2Context rhsCtx : ctx.expression2()) {
                    String lhsStr = str;
                    String rhsStr = parseSource(rhsCtx, dependencies, parameters);
                    str = "core.reducer([" + lhsStr + ", " + rhsStr + "], function(x, y) {return x " + ctx.op.getText() + " y;})";
                }

                return str;
            }

            @Override
            public String visitExpression3(BindParser.Expression3Context ctx) {
                String str = parseSource(ctx.atom(), dependencies, parameters);

                for(int i = 0; i < ctx.expressionTail().expressionTailPart().size(); i++) {
                    boolean isLast = i == ctx.expressionTail().expressionTailPart().size() - 1;
                    BindParser.ExpressionTailPartContext partCtx = ctx.expressionTail().expressionTailPart(i);
                    String strSource = str;
                    str = partCtx.accept(new BindBaseVisitor<String>() {
                        @Override
                        public String visitAccess(BindParser.AccessContext ctx) {
                            return "core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")";
                        }

                        @Override
                        public String visitCall(BindParser.CallContext ctx) {
                            String memberSourceStr = "core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")";
                            String argumentsStr = ctx.expression().stream().map(x -> parseSource(x, dependencies, parameters)).collect(Collectors.joining(", "));
                            String parametersStr = IntStream.range(0, ctx.expression().size()).mapToObj(x -> "arg0").collect(Collectors.joining(", "));

                            if(!isLast) {
                                return
                                    "core.reducer([" + memberSourceStr + ", " + argumentsStr + "], " +
                                        "function(func, " + parametersStr + ") {" +
                                        "return func(" + parametersStr + ")" +
                                        "})";
                            } else {
                                return "core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")";
                            }
                        }
                    });
                }

                return str;
            }

            @Override
            public String visitAtom(BindParser.AtomContext ctx) {
                return parseSource((ParserRuleContext) ctx.getChild(0), dependencies, parameters);
            }

            @Override
            public String visitString(BindParser.StringContext ctx) {
                return "core.constSource(" + ctx.getText() + ")";
            }

            @Override
            public String visitNumber(BindParser.NumberContext ctx) {
                return "core.constSource(" + ctx.getText() + ")";
            }

            @Override
            public String visitAccess(BindParser.AccessContext ctx) {
                return "core.constSource(" + ctx.getText() + ")";
            }

            @Override
            public String visitSelf(BindParser.SelfContext ctx) {
                return "core.constSource(this)";
            }

            @Override
            public String visitFunction(BindParser.FunctionContext ctx) {
                ArrayList<String> dependents = new ArrayList<String>();
                List<String> functionParameters = ctx.functionParameters().ID().stream().map(x -> x.getText()).collect(Collectors.toList());

                List<String> dependencies = ctx.block().blockElement().stream().map(x -> parseDependency(x, dependents, functionParameters)).collect(Collectors.toList());

                return "core.dependent(core.either([" +
                    dependents.stream().collect(Collectors.joining(", ")) + "]), " +
                    "core.constSource(" + ctx.getText() + "))";
            }
        });
    }

    private static String parseBlockElementDependencies(ParserRuleContext parseCtx, List<String> dependencies, List<String> parameters) {
        return parseCtx.accept(new BindBaseVisitor<String>() {
            @Override
            public String visitBlockElement(BindParser.BlockElementContext ctx) {
                return ctx.getChild(0).accept(new BindBaseVisitor<String>() {
                    /*@Override
                    public String visitStatement(BindParser.StatementContext ctx) {
                        String targetExpressionStr =
                            "core.memberTarget(" + ctx.targetExpression.getText() + ", \"" + ctx.ID().getText() + "\")";
                        String sourceExpressionStr = parseSource(ctx.sourceExpression, dependencies, parameters);

                        return "core.bind(" + sourceExpressionStr + ", " + targetExpressionStr + ")";
                    }*/

                    @Override
                    public String visitExpression(BindParser.ExpressionContext ctx) {
                        return parseDependency(ctx, dependencies, parameters);
                    }
                });
            }
        });
    }

    private static String parseDependency(ParserRuleContext sourceCtx, List<String> dependencies, List<String> parameters) {
        return sourceCtx.accept(new BindBaseVisitor<String>() {
            @Override
            public String visitExpression(BindParser.ExpressionContext ctx) {
                return parseDependency(ctx.expression1(), dependencies, parameters);
            }

            @Override
            public String visitExpression1(BindParser.Expression1Context ctx) {
                return parseDependency(ctx.expression2(), dependencies, parameters);
            }

            @Override
            public String visitExpression2(BindParser.Expression2Context ctx) {
                return parseDependency(ctx.expression3(), dependencies, parameters);
            }

            @Override
            public String visitExpression3(BindParser.Expression3Context ctx) {
                String str = parseDependency(ctx.atom(), dependencies, parameters);

                if(str != null) {
                    for (BindParser.ExpressionTailPartContext partCtx : ctx.expressionTail().expressionTailPart()) {
                        String strSource = str;
                        str = partCtx.accept(new BindBaseVisitor<String>() {
                            @Override
                            public String visitAccess(BindParser.AccessContext ctx) {
                                return "core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")";
                            }

                            @Override
                            public String visitCall(BindParser.CallContext ctx) {
                                return "core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")";
                                /*return "core.either([" +
                                    ctx.expression().stream().map(x -> parseDependency(x, dependencies, parameters)).collect(Collectors.joining(", ")) +
                                    ", core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")])";*/
                            }
                        });
                    }

                    dependencies.add(str);
                }

                for (BindParser.ExpressionTailPartContext partCtx : ctx.expressionTail().expressionTailPart()) {
                     partCtx.accept(new BindBaseVisitor<Void>() {
                        @Override
                        public Void visitCall(BindParser.CallContext ctx) {
                            ctx.expression().stream().forEach(x -> parseDependency(x, dependencies, parameters));

                            return null;

                            /*return "core.either([" +
                                ctx.expression().stream().map(x -> parseDependency(x, dependencies, parameters)).collect(Collectors.joining(", ")) +
                                ", core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")])";*/
                        }
                    });
                }

                return null;
            }

            @Override
            public String visitAtom(BindParser.AtomContext ctx) {
                return parseDependency((ParserRuleContext) ctx.getChild(0), dependencies, parameters);
            }

            @Override
            public String visitString(BindParser.StringContext ctx) {
                return null;
            }

            @Override
            public String visitNumber(BindParser.NumberContext ctx) {
                return null;
            }

            @Override
            public String visitAccess(BindParser.AccessContext ctx) {
                if(!parameters.contains(ctx.getText()))
                    return "core.constSource(" + ctx.getText() + ")";
                return null;
            }

            @Override
            public String visitSelf(BindParser.SelfContext ctx) {
                return "core.constSource(this)";
            }

            @Override
            public String visitFunction(BindParser.FunctionContext ctx) {
                return null;
            }
        });
    }
}
