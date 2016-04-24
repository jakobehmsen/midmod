package reo.lang;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import reo.lang.antlr4.ReoBaseVisitor;
import reo.lang.antlr4.ReoLexer;
import reo.lang.antlr4.ReoParser;
import reo.runtime.Behavior;
import reo.runtime.Instruction;
import reo.runtime.Instructions;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Parser {
    public static Behavior parseGet(String text) {
        return null;
    }

    private interface Emitter {
        default void emit(List<Instruction> instructions, Map<String, Integer> parameters, Map<String, Integer> locals) {
            emit(instructions);
        }
        void emit(List<Instruction> instructions);
    }

    public static Behavior parse(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        ReoLexer lexer = new ReoLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReoParser parser = new ReoParser(tokenStream);

        return parseBlock(parser.block().statementOrExpression(), true, instructions -> instructions.add(Instructions.halt()), new Hashtable<>());
    }

    private static Behavior parseBlock(List<? extends ParserRuleContext> blockElementCtxs, boolean implicitReturn, Consumer<List<Instruction>> postEmitter, Map<String, Integer> parameters) {
        List<Emitter> emitters = new ArrayList<>();
        Map<String, Integer> locals = new Hashtable<>();
        locals.put("this", 0);
        //locals.putAll(parameters);

        IntStream.range(0, blockElementCtxs.size()).forEach(index ->
            parseBlockElement(blockElementCtxs.get(index), emitters, parameters, locals, implicitReturn ? index == blockElementCtxs.size() - 1 : false)
        );

        int internalCount = locals.size(); //(int)locals.keySet().stream().filter(x -> !parameters.containsKey(x)).count();
        int externalCount = parameters.size(); //(int)locals.keySet().stream().filter(x -> parameters.containsKey(x)).count();

        ArrayList<Instruction> instructions = new ArrayList<>();

        emitters.forEach(e -> e.emit(instructions, parameters, locals));
        postEmitter.accept(instructions);

        return new Behavior(instructions.toArray(new Instruction[instructions.size()]), internalCount, externalCount);
    }

    private static void parseBlockElement(ParserRuleContext ctx, List<Emitter> emitters, Map<String, Integer> parameters, Map<String, Integer> locals, boolean implicitReturn) {
        ctx.accept(new ReoBaseVisitor<Void>() {
            @Override
            public Void visitStatementOrExpression(ReoParser.StatementOrExpressionContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public Void visitExpression(ReoParser.ExpressionContext ctx) {
                parseExpression(ctx, emitters, parameters, locals, !implicitReturn);

                return null;
            }

            @Override
            public Void visitStatement(ReoParser.StatementContext ctx) {
                parseStatement((ParserRuleContext) ctx.getChild(0), emitters, parameters, locals);

                return null;
            }
        });
    }

    private static void parseStatement(ParserRuleContext ctx, List<Emitter> emitters, Map<String, Integer> parameters, Map<String, Integer> locals) {
        ctx.accept(new ReoBaseVisitor<Void>() {
            @Override
            public Void visitVariableDeclaration(ReoParser.VariableDeclarationContext ctx) {
                if(parameters.containsKey(ctx.ID().getText())) {
                    // Error
                }

                int ordinal = locals.computeIfAbsent(ctx.ID().getText(), id -> locals.size());

                if(ctx.expression() != null)
                    parseExpression(ctx.expression(), emitters, parameters, locals, false);
                else
                    emitters.add(instructions -> instructions.add(Instructions.loadNull()));

                emitters.add(instructions -> instructions.add(Instructions.storeLocal(ordinal)));

                return null;
            }

            /*@Override
            public Void visitReturnStatement(ReoParser.ReturnStatementContext ctx) {
                parseExpression(ctx.expression(), emitters, locals, false);
                emitters.add(instructions -> instructions.add(Instructions.ret()));

                return null;
            }

            @Override
            public Void visitDeclaration(ReoParser.DeclarationContext ctx) {
                int ordinal = locals.computeIfAbsent(ctx.ID().getText(), id -> locals.size());

                if(ctx.expression() != null)
                    parseExpression(ctx.expression(), emitters, locals, false);
                else
                    emitters.add(instructions -> instructions.add(Instructions.loadNull()));

                emitters.add(instructions -> instructions.add(Instructions.storeLocal(ordinal)));

                return null;
            }*/
        });
    }

    private static void parseExpression(ParserRuleContext ctx, List<Emitter> emitters, Map<String, Integer> parameters, Map<String, Integer> locals, boolean atTop) {
        ctx.accept(new ReoBaseVisitor<Void>() {
            @Override
            public Void visitExpression(ReoParser.ExpressionContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            /*@Override
            public Void visitAssignment(ReoParser.AssignmentContext ctx) {
                Integer ordinal = locals.get(ctx.ID().getText());

                if(ordinal != null) {
                    // Variable assignment
                    ctx.expression().accept(this);
                    if (!atTop)
                        emitters.add(instructions -> instructions.add(Instructions.dup()));
                    emitters.add(instructions -> instructions.add(Instructions.storeLocal(ordinal)));
                } else {
                    // Slot assignment against this
                    emitters.add(instructions -> instructions.add(Instructions.loadLocal(0)));
                    String selector = ctx.ID().getText();
                    emitters.add(instructions -> instructions.add(Instructions.loadConst(new RString(selector))));
                    parseExpression(ctx.expression(), emitters, locals, false);
                    if(!atTop)
                        emitters.add(instructions -> instructions.add(Instructions.dup2()));
                    messageSend("putSlot", 2, emitters, atTop);
                }

                return null;
            }*/

            @Override
            public Void visitSlotAssignment(ReoParser.SlotAssignmentContext ctx) {
                parseSlotAssignment(ctx, false, atTop);

                return null;
            }

            private void parseSlotAssignment(ReoParser.SlotAssignmentContext ctx, boolean hasTarget, boolean atTop) {
                ctx.getChild(0).accept(new ReoBaseVisitor<Void>() {
                    @Override
                    public Void visitFieldSlotAssignment(ReoParser.FieldSlotAssignmentContext ctx) {
                        String selector = getSelector(ctx.selector());

                        if(!hasTarget && parameters.containsKey(selector)) {
                            parseExpression(ctx.expression(), emitters, parameters, locals, false);
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.dup()));
                            emitters.add(new Emitter() {
                                @Override
                                public void emit(List<Instruction> instructions, Map<String, Integer> parameters, Map<String, Integer> locals) {
                                    int ordinal = locals.size() + parameters.get(selector);
                                    instructions.add(Instructions.storeLocal(ordinal));
                                }

                                @Override
                                public void emit(List<Instruction> instructions) {

                                }
                            });
                        } else if(!hasTarget && locals.containsKey(selector)) {
                            parseExpression(ctx.expression(), emitters, parameters, locals, false);
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.dup()));
                            int ordinal = locals.get(selector);
                            emitters.add(instructions -> instructions.add(Instructions.storeLocal(ordinal)));
                        } else {
                            if(!hasTarget)
                                emitters.add(instructions -> instructions.add(Instructions.load(0)));
                            parseExpression(ctx.expression(), emitters, parameters, locals, false);
                            emitters.add(instructions -> instructions.add(Instructions.storeSlot(selector)));
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.loadSlot(selector)));
                        }

                        /*if(sendMessage) {
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.dup2()));
                            messageSend("putSlot", 2, emitters, atTop);
                        } else
                            emitters.add(instructions -> instructions.add(Instructions.storeSlot()));*/

                        return null;
                    }

                    @Override
                    public Void visitMethodSlotAssignment(ReoParser.MethodSlotAssignmentContext ctx) {
                        String selector = getSelector(getSelectorSelectorName(ctx.selectorName()), ctx.selectorParameters());

                        Map<String, Integer> parameters = new Hashtable<>();
                        ctx.selectorParameters().ID().forEach(x -> parameters.put(x.getText(), parameters.size()));
                        Behavior behavior;

                        if(ctx.singleExpressionBody != null) {
                            behavior = parseBlock(Arrays.asList(ctx.singleExpressionBody), true, instructions -> instructions.add(Instructions.ret()), parameters);
                        } else {
                            behavior = parseBlock(ctx.blockBody.statementOrExpression(), true, instructions -> { }, parameters);
                        }


                        //emitters.add(instructions -> instructions.add(Instructions.storeSlot(selector)));
                        if(!hasTarget && parameters.containsKey(selector)) {
                            emitters.add(instructions -> instructions.add(Instructions.newMethod(behavior)));
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.dup()));
                            emitters.add(new Emitter() {
                                @Override
                                public void emit(List<Instruction> instructions, Map<String, Integer> parameters, Map<String, Integer> locals) {
                                    int ordinal = locals.size() + parameters.get(selector);
                                    instructions.add(Instructions.storeLocal(ordinal));
                                }

                                @Override
                                public void emit(List<Instruction> instructions) {

                                }
                            });
                        } else if(!hasTarget && locals.containsKey(selector)) {
                            emitters.add(instructions -> instructions.add(Instructions.newMethod(behavior)));
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.dup()));
                            int ordinal = locals.get(selector);
                            emitters.add(instructions -> instructions.add(Instructions.storeLocal(ordinal)));
                        } else {
                            if(!hasTarget)
                                emitters.add(instructions -> instructions.add(Instructions.load(0)));
                            emitters.add(instructions -> instructions.add(Instructions.newMethod(behavior)));
                            emitters.add(instructions -> instructions.add(Instructions.storeSlot(selector)));
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.loadSlot(selector)));
                        }
                        /*if(sendMessage) {
                            if(!atTop)
                                emitters.add(instructions -> instructions.add(Instructions.dup2()));
                            messageSend("putSlot", 2, emitters, atTop);
                        } else
                            emitters.add(instructions -> instructions.add(Instructions.storeSlot()));*/

                        return null;
                    }
                });
            }

            private String getSelector(ReoParser.AccessContext ctx) {
                if(ctx.ID() != null) {
                    return ctx.getText();
                } else {
                    String name = ctx.getText().substring(1, ctx.getText().length() - 1);
                    return getSelector(name, ctx.qualifiedSelector().selectorParameters());
                }
            }

            private String getSelector(ReoParser.SelectorContext ctx) {
                if(ctx.unqualifiedSelector() != null) {
                    return getSelector(ctx.unqualifiedSelector().ID().getText(), ctx.unqualifiedSelector().selectorParameters());
                } else {
                    String name = ctx.getText().substring(1, ctx.getText().length() - 1);
                    return getSelector(name, ctx.qualifiedSelector().selectorParameters());
                }
            }

            private String getSelector(String name, ReoParser.SelectorParametersContext ctx) {
                return name + (ctx != null ? "/" + ctx.ID().size() : "");
            }

            private String getSelectorSelectorName(ReoParser.SelectorNameContext ctx) {
                if(ctx.ID() != null)
                    return ctx.getText();
                return ctx.getText().substring(1, ctx.getText().length() - 1);
            }


            private void parseBinary(ParserRuleContext ctx, ParserRuleContext exprCtx, List<? extends ParserRuleContext> exprCtxTail) {
                exprCtx.accept(this);

                for (ParserRuleContext rhsCtx : exprCtxTail) {
                    String selector = ctx.children.get(ctx.children.indexOf(rhsCtx) - 1).getText();
                    rhsCtx.accept(this);
                    messageSend(selector, 1, emitters, atTop);
                }
            }

            @Override
            public Void visitAddExpression(ReoParser.AddExpressionContext ctx) {
                parseBinary(ctx, ctx.lhs, ctx.addExpressionOp());

                return null;
            }

            @Override
            public Void visitMulExpression(ReoParser.MulExpressionContext ctx) {
                parseBinary(ctx, ctx.lhs, ctx.mulExpressionOp());

                return null;
            }

            @Override
            public Void visitChainedExpression(ReoParser.ChainedExpressionContext ctx) {
                boolean atomAtTop = ctx.expressionTail().expressionTailPart().size() == 0 && ctx.expressionTail().expressionTailEnd() == null;
                parseExpression(ctx.atomExpression(), emitters, parameters, locals, atomAtTop && atTop);
                int tailCount = ctx.expressionTail().expressionTailPart().size() + (ctx.expressionTail().expressionTailEnd() != null ? 1 : 0);
                int tailNo = 0;

                for (ReoParser.ExpressionTailPartContext expressionTailPartContext : ctx.expressionTail().expressionTailPart()) {
                    int tailNoTmp = tailNo;
                    boolean partAtTop = tailNoTmp == tailCount - 1 ? atTop : false;
                    expressionTailPartContext.accept(new ReoBaseVisitor<Void>() {
                        /*@Override
                        public Void visitCall(ReoParser.CallContext ctx) {
                            ctx.expression().forEach(x ->  parseExpression(x, emitters, locals, false));
                            messageSend(ctx.ID().getText(), ctx.expression().size(), emitters, partAtTop);

                            return null;
                        }*/

                        @Override
                        public Void visitMessage(ReoParser.MessageContext ctx) {
                            ctx.expression().forEach(x ->  parseExpression(x, emitters, parameters, locals, partAtTop));
                            messageSend(ctx.ID().getText(), ctx.expression().size(), emitters, partAtTop);

                            return null;
                        }

                        @Override
                        public Void visitSlotAccess(ReoParser.SlotAccessContext ctx) {
                            /*emitters.add(instructions -> instructions.add(Instructions.loadConst(new RString(ctx.ID().getText()))));
                            messageSend("getSlot", 1, emitters, partAtTop);*/
                            String selector = getSelector(ctx.access());
                            emitters.add(instructions -> instructions.add(Instructions.loadSlot(selector)));

                            return null;
                        }

                        /*@Override
                        public Void visitIndexAccess(ReoParser.IndexAccessContext ctx) {
                            parseExpression(ctx.expression(), emitters, locals, false);
                            messageSend("[]", 1, emitters, atTop);

                            return null;
                        }*/
                    });
                    tailNo++;
                }

                if(ctx.expressionTail().expressionTailEnd() != null) {
                    ctx.expressionTail().expressionTailEnd().accept(new ReoBaseVisitor<Void>() {
                        @Override
                        public Void visitSlotAssignment(ReoParser.SlotAssignmentContext ctx) {
                            parseSlotAssignment(ctx, true, false);

                            return null;
                        }

                        /*@Override
                        public Void visitIndexAssign(ReoParser.IndexAssignContext ctx) {
                            ctx.expression().forEach(x -> parseExpression(x, emitters, locals, false));
                            messageSend("[]=", 2, emitters, atTop);

                            return null;
                        }

                        @Override
                        public Void visitKeyword(ReoParser.KeywordContext ctx) {
                            String selector = ctx.ID_LOWER().getText().substring(0, ctx.ID_LOWER().getText().length() - 1) +
                                ctx.ID_UPPER().stream().map(x -> x.getText().substring(0, x.getText().length() - 1)).collect(Collectors.joining());

                            ctx.expression0().forEach(x -> parseExpression(x, emitters, locals, false));
                            messageSend(selector, 1 + ctx.ID_UPPER().size(), emitters, atTop);

                            return null;
                        }*/
                    });
                }

                return null;
            }

            //
//            @Override
//            public Void visitExpression1(ReoParser.Expression1Context ctx) {
//                parseBinary(ctx, ctx.expression2(), ctx.expression1());
//
//                return null;
//            }
//
//            @Override
//            public Void visitExpression2(ReoParser.Expression2Context ctx) {
//                parseBinary(ctx, ctx.expression3(), ctx.expression2());
//
//                return null;
//            }
//
//            @Override
//            public Void visitExpression3(ReoParser.Expression3Context ctx) {
//                parseBinary(ctx, ctx.expression4(), ctx.expression3());
//
//                return null;
//            }
//
//            @Override
//            public Void visitExpression4(ReoParser.Expression4Context ctx) {
//                parseBinary(ctx, ctx.expression5(), ctx.expression4());
//
//                return null;
//            }
//
//            @Override
//            public Void visitExpression5(ReoParser.Expression5Context ctx) {
//                boolean atomAtTop = ctx.expressionTail().expressionTailPart().size() == 0 && ctx.expressionTail().expressionTailEnd() == null;
//                parseExpression(ctx.atom(), emitters, locals, atomAtTop && atTop);
//                int tailCount = ctx.expressionTail().expressionTailPart().size() + (ctx.expressionTail().expressionTailEnd() != null ? 1 : 0);
//                int tailNo = 0;
//
//                for (ReoParser.ExpressionTailPartContext expressionTailPartContext : ctx.expressionTail().expressionTailPart()) {
//                    int tailNoTmp = tailNo;
//                    boolean partAtTop = tailNoTmp == tailCount - 1 ? atTop : false;
//                    expressionTailPartContext.accept(new ReoBaseVisitor<Void>() {
//                        @Override
//                        public Void visitCall(ReoParser.CallContext ctx) {
//                            ctx.expression().forEach(x ->  parseExpression(x, emitters, locals, false));
//                            messageSend(ctx.ID().getText(), ctx.expression().size(), emitters, partAtTop);
//
//                            return null;
//                        }
//
//                        @Override
//                        public Void visitSlotAccess(ReoParser.SlotAccessContext ctx) {
//                            emitters.add(instructions -> instructions.add(Instructions.loadConst(new RString(ctx.ID().getText()))));
//                            messageSend("getSlot", 1, emitters, partAtTop);
//
//                            return null;
//                        }
//
//                        @Override
//                        public Void visitIndexAccess(ReoParser.IndexAccessContext ctx) {
//                            parseExpression(ctx.expression(), emitters, locals, false);
//                            messageSend("[]", 1, emitters, atTop);
//
//                            return null;
//                        }
//                    });
//                    tailNo++;
//                }
//
//                if(ctx.expressionTail().expressionTailEnd() != null) {
//                    ctx.expressionTail().expressionTailEnd().accept(new ReoBaseVisitor<Void>() {
//                        @Override
//                        public Void visitSlotAssignment(ReoParser.SlotAssignmentContext ctx) {
//                            parseSlotAssignment(ctx, true);
//
//                            return null;
//                        }
//
//                        @Override
//                        public Void visitIndexAssign(ReoParser.IndexAssignContext ctx) {
//                            ctx.expression().forEach(x -> parseExpression(x, emitters, locals, false));
//                            messageSend("[]=", 2, emitters, atTop);
//
//                            return null;
//                        }
//
//                        @Override
//                        public Void visitKeyword(ReoParser.KeywordContext ctx) {
//                            String selector = ctx.ID_LOWER().getText().substring(0, ctx.ID_LOWER().getText().length() - 1) +
//                                ctx.ID_UPPER().stream().map(x -> x.getText().substring(0, x.getText().length() - 1)).collect(Collectors.joining());
//
//                            ctx.expression0().forEach(x -> parseExpression(x, emitters, locals, false));
//                            messageSend(selector, 1 + ctx.ID_UPPER().size(), emitters, atTop);
//
//                            return null;
//                        }
//                    });
//                }
//
//                return null;
//            }
//
//            private String getSelectorName(ReoParser.SelectorNameContext ctx) {
//                if(ctx.ID() != null)
//                    return ctx.ID().getText();
//                return ctx.SELECTOR().getText().substring(1, ctx.SELECTOR().getText().length() - 1);
//            }
//
//            private void parseSlotAssignment(ReoParser.SlotAssignmentContext ctx, boolean sendMessage) {
//                ctx.getChild(0).accept(new ReoBaseVisitor<Void>() {
//                    @Override
//                    public Void visitFieldSlotAssignment(ReoParser.FieldSlotAssignmentContext ctx) {
//                        String selector = getSelectorName(ctx.selector().selectorName()) +
//                            (ctx.selector().isMethod != null ? "/" + ctx.selector().ID().size() : "");
//                        emitters.add(instructions -> instructions.add(Instructions.loadConst(new RString(selector))));
//                        parseExpression(ctx.expression(), emitters, locals, false);
//                        if(sendMessage) {
//                            if(!atTop)
//                                emitters.add(instructions -> instructions.add(Instructions.dup2()));
//                            messageSend("putSlot", 2, emitters, atTop);
//                        } else
//                            emitters.add(instructions -> instructions.add(Instructions.storeSlot()));
//
//                        return null;
//                    }
//
//                    @Override
//                    public Void visitMethodSlotAssignment(ReoParser.MethodSlotAssignmentContext ctx) {
//                        String selector = getSelectorName(ctx.selector().selectorName()) +
//                            (ctx.selector().isMethod != null ? "/" + ctx.selector().ID().size() : "");
//                        emitters.add(instructions -> instructions.add(Instructions.loadConst(new RString(selector))));
//
//                        Map<String, Integer> parameters = new Hashtable<>();
//                        ctx.selector().ID().forEach(x -> parameters.put(x.getText(), parameters.size() + 1 /*Add one because zero is this*/));
//                        Behavior behavior;
//
//                        if(ctx.singleExpressionBody != null) {
//                            behavior = parseBlock(Arrays.asList(ctx.singleExpressionBody), true, instructions -> instructions.add(Instructions.ret()), parameters);
//                        } else {
//                            behavior = parseBlock(ctx.blockBody.blockElement(), true, instructions -> { }, parameters);
//                        }
//
//                        emitters.add(instructions -> instructions.add(Instructions.loadConst(new FunctionRObject(behavior))));
//                        if(sendMessage) {
//                            if(!atTop)
//                                emitters.add(instructions -> instructions.add(Instructions.dup2()));
//                            messageSend("putSlot", 2, emitters, atTop);
//                        } else
//                            emitters.add(instructions -> instructions.add(Instructions.storeSlot()));
//
//                        return null;
//                    }
//                });
//            }
//
            private Void messageSend(String name, int arity, List<Emitter> emitters, boolean atTop) {
                String selector = name + "/" + arity;
                emitters.add(instructions -> instructions.add(Instructions.messageSend(selector, arity)));
                if(atTop)
                    emitters.add(instructions -> instructions.add(Instructions.pop()));

                return null;
            }

            @Override
            public Void visitSelfSend(ReoParser.SelfSendContext ctx) {
                ctx.message().expression().forEach(x -> parseExpression(x, emitters, parameters, locals, false));
                messageSend(ctx.message().ID().getText(), ctx.message().expression().size(), emitters, atTop);

                return null;
            }

            //
//            @Override
//            public Void visitSelfCall(ReoParser.SelfCallContext ctx) {
//                ctx.call().expression().forEach(x -> parseExpression(x, emitters, locals, false));
//                messageSend(ctx.call().ID().getText(), ctx.call().expression().size(), emitters, atTop);
//
//                return null;
//            }
//
//            @Override
//            public Void visitSelfKeyword(ReoParser.SelfKeywordContext ctx) {
//                String selector = ctx.keyword().ID_LOWER().getText().substring(0, ctx.keyword().ID_LOWER().getText().length() - 1) +
//                    ctx.keyword().ID_UPPER().stream().map(x -> x.getText().substring(0, x.getText().length() - 1)).collect(Collectors.joining());
//
//                ctx.keyword().expression0().forEach(x -> parseExpression(x, emitters, locals, false));
//                messageSend(selector, 1 + ctx.keyword().ID_UPPER().size(), emitters, atTop);
//
//                return null;
//            }
//
            @Override
            public Void visitNumber(ReoParser.NumberContext ctx) {
                Object valueTmp;

                try {
                    valueTmp =  Integer.parseInt(ctx.getText());
                } catch (NumberFormatException e1) {
                    try {
                        valueTmp =  Long.parseLong(ctx.getText());
                    } catch (NumberFormatException e2) {
                        valueTmp = Double.parseDouble(ctx.getText());
                    }
                }

                Object value = valueTmp;

                emitters.add(instructions -> instructions.add(Instructions.loadConstant(value)));

                return null;
            }

            @Override
            public Void visitString(ReoParser.StringContext ctx) {
                String value = parseString(ctx);

                emitters.add(instructions -> instructions.add(Instructions.loadConstant(value)));

                return null;
            }

            private String parseString(ParserRuleContext ctx) {
                String rawString = ctx.getText().substring(1, ctx.getText().length() - 1);
                return rawString.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
            }

            @Override
            public Void visitSelf(ReoParser.SelfContext ctx) {
                emitters.add(instructions -> instructions.add(Instructions.load(0)));

                return null;
            }
//
//            @Override
//            public Void visitThisFrame(ReoParser.ThisFrameContext ctx) {
//                emitters.add(instructions -> instructions.add(Instructions.loadFrame()));
//
//                return null;
//            }
//
            @Override
            public Void visitAccess(ReoParser.AccessContext ctx) {
                String selector = getSelector(ctx);

                if(!atTop) {
                    if(parameters.containsKey(selector)) {
                        emitters.add(new Emitter() {
                            @Override
                            public void emit(List<Instruction> instructions, Map<String, Integer> parameters, Map<String, Integer> locals) {
                                int ordinal = locals.size() + parameters.get(selector);
                                instructions.add(Instructions.load(ordinal));
                            }

                            @Override
                            public void emit(List<Instruction> instructions) {

                            }
                        });
                    } else if(locals.containsKey(selector)) {
                        int ordinal = locals.get(selector);
                        emitters.add(instructions -> instructions.add(Instructions.load(ordinal)));
                    } else {
                        emitters.add(instructions -> instructions.add(Instructions.load(0)));
                        emitters.add(instructions -> instructions.add(Instructions.loadSlot(selector)));
                    }

                    /*Integer ordinal = locals.get(selector);

                    if(ordinal != null) // If is local
                        emitters.add(instructions -> instructions.add(Instructions.load(ordinal)));
                    else { // Else is assumed to be instance slot

                        emitters.add(instructions -> instructions.add(Instructions.load(0)));
                        emitters.add(instructions -> instructions.add(Instructions.loadSlot(selector)));
                    }*/
                }

                return null;
            }
//
//            @Override
//            public Void visitPrimitive(ReoParser.PrimitiveContext ctx) {
//                ctx.expression().forEach(x -> parseExpression(x, emitters, locals, false));
//                try {
//                    // Support instruction creation arguments
//                    // Perhaps, something like:
//                    // $loadConst["A const"]()
//                    // where, [...] denotes the creation arguments
//                    Instruction instruction = (Instruction) Instructions.class.getMethod(ctx.ID().getText()).invoke(null);
//
//                    if(atTop) {
//                        if(instruction.isFunctional()) {
//                            emitters.add(instructions -> instructions.add(instruction));
//                            // Ignore result
//                            emitters.add(instructions -> Instructions.pop());
//                        } else // Emit imperative instruction
//                            emitters.add(instructions -> instructions.add(instruction));
//                    } else {
//                        if(!instruction.isFunctional()) {
//                            emitters.add(instructions -> instructions.add(instruction));
//                            emitters.add(instructions -> Instructions.loadLocal(0));
//                            //// TODO: Consider: Implicitly emit load of this/local(0)?
//                            //throw new RuntimeException("Imperative instructions must be at top level/cannot be expressions.");
//                        } else // Emit functional instruction
//                            emitters.add(instructions -> instructions.add(instruction));
//                    }
//
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//
//            @Override
//            public Void visitFunction(ReoParser.FunctionContext ctx) {
//                if(!atTop) {
//                    Map<String, Integer> parameters = new Hashtable<>();
//                    if(ctx.functionParameters() != null)
//                        ctx.functionParameters().ID().forEach(x -> parameters.put(x.getText(), parameters.size() + 1 /*Add one because zero is this*/));
//                    Behavior behavior;
//
//                    if (ctx.singleExpressionBody != null) {
//                        behavior = parseBlock(Arrays.asList(ctx.singleExpressionBody), true, instructions -> instructions.add(Instructions.ret()), parameters);
//                    } else {
//                        behavior = parseBlock(Arrays.asList(ctx.blockBody), true, instructions -> {
//                        }, parameters);
//                    }
//
//                    emitters.add(instructions -> instructions.add(Instructions.loadConst(new FunctionRObject(behavior))));
//                }
//
//                return null;
//            }
//
//            @Override
//            public Void visitArrayLiteral(ReoParser.ArrayLiteralContext ctx) {
//                ctx.expression().forEach(x -> parseExpression(x, emitters, locals, false));
//                emitters.add(instructions -> instructions.add(Instructions.loadConst(new IntegerRObject(ctx.expression().size()))));
//                emitters.add(instructions -> instructions.add(Instructions.newa()));
//                if(atTop)
//                    emitters.add(instructions -> instructions.add(Instructions.pop()));
//
//                return null;
//            }
//
            @Override
            public Void visitObjectLiteral(ReoParser.ObjectLiteralContext ctx) {
                // Just load self implicitly as prototype
                emitters.add(instructions -> instructions.add(Instructions.load(0)));

                emitters.add(instructions -> instructions.add(Instructions.newDict()));
                ctx.slotAssignment().forEach(x -> {
                    emitters.add(instructions -> instructions.add(Instructions.dup()));
                    parseSlotAssignment(x, true, true);
                });

                if(atTop)
                    emitters.add(instructions -> instructions.add(Instructions.pop()));

                return null;
            }

            @Override
            public Void visitEmbeddedExpression(ReoParser.EmbeddedExpressionContext ctx) {
                return ctx.expression().accept(this);
            }
        });
    }
}
