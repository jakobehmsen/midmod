package midmod.parse;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Parsers {
    public static <R> Parser<Character, R> seq(CharSequence charSequence) {
        return seq(() -> new CharSequenceInput(charSequence));
    }

    public static <T, R> Parser<T, R> seq(Supplier<Input<T>> sequence) {
        return new Parser<T, R>() {
            public boolean parse(Input<T> input, Output<R> output) {
                return input.parseSequence(sequence.get(), output);
            }

            @Override
            public Parser<T, R> not() {
                return new Parser<T, R>() {
                    public boolean parse(Input<T> input, Output<R> output) {
                        return input.parseSequenceNot(sequence.get(), output);
                    }

                    @Override
                    public Parser<T, R> not() {
                        return seq(sequence);
                    }
                };
            }
        };


        //return (input, output) -> input.parseSequence(sequence.get(), output);
    }

    public static <T, R> Parser<T, R> copyAndConsume() {
        return (input, output) -> {
            if(!input.atEnd()) {
                input.copyTo(output);
                input.consume();
                return true;
            }

            return false;
        };
    }

    public static <T, R> Parser<T, R> copy(Parser<T, R> parser) {
        return (input, output) -> {
            Input inputToWrap = input.forCopy();
            Input<T> copyInput = new Input<T>() {
                @Override
                public void begin() {
                    inputToWrap.begin();
                }

                @Override
                public void commit() {
                    inputToWrap.commit();
                }

                @Override
                public void rollback() {
                    inputToWrap.rollback();
                }

                @Override
                public void consume() {
                    copyTo(output);
                    inputToWrap.consume();
                }

                @Override
                public boolean peeksMatchesPeek(Input<T> input) {
                    return inputToWrap.peeksMatchesPeek(input);
                }

                @Override
                public boolean atEnd() {
                    return inputToWrap.atEnd();
                }

                @Override
                public char peekChar() {
                    return inputToWrap.peekChar();
                }

                @Override
                public <R> void copyTo(Output<R> output) {
                    inputToWrap.copyTo(output);
                }

                @Override
                public InputState<T> getState() {
                    return input.getState();
                }

                @Override
                public Input<T> forCopy() {
                    return inputToWrap.forCopy();
                }

                @Override
                public CharSequence peekChars() {
                    return inputToWrap.peekChars();
                }
            };
            return parser.parse(copyInput, output);
        };
    }

    public static <T, R> Parser<T, R> any() {
        return (input, output) -> {
            return true;
            /*if(!input.atEnd()) {
                input.consume();
                return true;
            }

            return false;*/
        };
    }

    public static <T, R> Parser<T, R> until(Parser<T, R> defaultParser, Parser<T, R> eventParser) {
        return (input, output) -> {
            if(eventParser.tryParse(input, output))
                return true;

            Input<T> eventInput = new Input<T>() {
                boolean atEnd = false;

                @Override
                public void consume() {
                    input.consume();

                    if(eventParser.tryParse(input, output))
                        atEnd = true;
                }

                @Override
                public boolean peeksMatchesPeek(Input<T> otherInput) {
                    return input.peeksMatchesPeek(otherInput);
                }

                @Override
                public boolean atEnd() {
                    return atEnd || input.atEnd();
                }

                @Override
                public char peekChar() {
                    return input.peekChar();
                }

                @Override
                public <R> void copyTo(Output<R> output) {
                    input.copyTo(output);
                }

                @Override
                public InputState<T> getState() {
                    return input.getState();
                }

                @Override
                public CharSequence peekChars() {
                    return input.peekChars();
                }

                @Override
                public void begin() {
                    input.begin();
                }

                @Override
                public void commit() {
                    input.commit();
                }

                @Override
                public void rollback() {
                    input.rollback();
                }
            };

            return defaultParser.parse(eventInput, output);
        };
    }

    public static <T, R> Parser<T, R> ref(Supplier<Parser<T, R>> parserSupplier) {
        return (input, output) -> parserSupplier.get().parse(input, output);
    }

    public static <T> Parser<T, T> replace(Parser<T, T> parser, BiConsumer<Input<T>, Output<T>> outputter) {
        //Parser<T, T> copyParser = copy(parser);

        return (input, output) -> {
            //InputState<T> state = input.getState();
            Output<T> capturer = output.createEmpty();

            if(parser.parse(input, capturer)) {
                Input<T> captured = capturer.toInput();
                outputter.accept(captured, output);
                return true;
            }

            return false;
        };
    }

    public static <T, R> Parser<T, R> repeat(Parser parser) {
        return (input, output) -> {
            while(parser.tryParse(input, output));

            return true;
        };
    }

    public static <T, R> Parser<T, R> skip(Parser<T, R> parser) {
        return (input, output) -> {
            Output<R> skipOutput = new Output<R>() {
                @Override
                public void putChar(char ch) {

                }

                @Override
                public Output<R> createEmpty() {
                    return this;
                }

                @Override
                public Input<R> toInput() {
                    return null;
                }

                @Override
                public void begin() {

                }

                @Override
                public void commit() {

                }

                @Override
                public void rollback() {

                }
            };

            return parser.parse(input, skipOutput);
        };
    }
}
