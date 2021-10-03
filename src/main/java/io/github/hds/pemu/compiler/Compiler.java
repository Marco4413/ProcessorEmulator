package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.parser.*;
import io.github.hds.pemu.processor.IProcessor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Compiler {

    private static class LabelUsage {
        public final ArrayList<Integer> USAGES = new ArrayList<>();
        public final LabelNode NODE;

        protected LabelUsage(@NotNull LabelNode node) {
            NODE = node;
        }
    }

    public static @NotNull CompiledProgram compileFile(@NotNull File file, @NotNull IProcessor processor) {
        long compilationStartTimestamp = System.nanoTime();

        List<INode> nodes = Parser.parseFile(file, processor);
        ArrayList<Integer> program = new ArrayList<>();

        HashMap<String, Integer> labelDeclarations = new HashMap<>();
        HashMap<String, LabelUsage> labelUsages = new HashMap<>();

        int memoryOffset = processor.getProgramAddress();

        for (INode node : nodes) {
            switch (node.getType()) {
                case ARRAY: {
                    assert node instanceof ArrayNode;
                    int arrayLength = ((ArrayNode) node).getLength();
                    for (int i = 0; i < arrayLength; i++) {
                        program.add(0);
                    }

                    break;
                }
                case VALUE:
                    assert node instanceof ValueNode;
                    program.add(((ValueNode) node).getValue());

                    break;
                case OFFSET:
                    assert node instanceof OffsetNode;
                    program.add(memoryOffset + program.size() + ((OffsetNode) node).getValue());

                    break;
                case STRING: {
                    assert node instanceof StringNode;
                    String str = ((StringNode) node).getString();
                    for (int i = 0; i < str.length(); i++)
                        program.add((int) str.charAt(i));

                    break;
                }
                case LABEL: {
                    assert node instanceof LabelNode;
                    LabelNode labelNode = (LabelNode) node;
                    String labelName = labelNode.getName();

                    if (labelNode.isDeclaration()) {
                        if (labelDeclarations.containsKey(labelName))
                            throw new CompilerError.ReferenceError(
                                    labelNode.getFile(), labelNode.getLine(), labelNode.getLineChar(),
                                    "Label", labelName, "was already declared."
                            );
                        labelDeclarations.put(labelName, memoryOffset + program.size());
                    } else {
                        labelUsages.putIfAbsent(labelName, new LabelUsage(labelNode));
                        labelUsages.get(labelName).USAGES.add(program.size());
                        program.add(labelNode.getOffset());
                    }

                    break;
                }
                default:
                    throw new IllegalStateException("Invalid Node Reached Compiler.");
            }
        }

        labelUsages.forEach(
                (labelName, labelUsage) -> {
                    if (!labelDeclarations.containsKey(labelName))
                        throw new CompilerError.ReferenceError(
                                labelUsage.NODE.getFile(), labelUsage.NODE.getLine(), labelUsage.NODE.getLineChar(),
                                "Label", labelName, "was not declared."
                        );
                    for (int i : labelUsage.USAGES) {
                        program.set(
                                i, program.get(i) + labelDeclarations.get(labelName)
                        );
                    }
                }
        );

        int[] primitiveIntProgram = new int[program.size()];
        for (int i = 0; i < primitiveIntProgram.length; i++)
            primitiveIntProgram[i] = program.get(i);

        return new CompiledProgram(
                processor, nodes, primitiveIntProgram, System.nanoTime() - compilationStartTimestamp
        );
    }

    private static final char[] RANDOM_STRING_CHARACTERS;
    static {
        // Not allowing uppercase characters because Strings may be
        //  the same as Instruction names which should always be uppercase
        RANDOM_STRING_CHARACTERS = new char['z' - 'a' + 1];
        for (int i = 0; i < RANDOM_STRING_CHARACTERS.length; i++) {
            RANDOM_STRING_CHARACTERS[i] = (char) ('a' + i);
        }
    }

    /**
     * Function that generates a pseudo-random String using the specified seed
     * @param seed The seed to generate the String from
     * @return A pseudo-randomly generated String based on the specified seed
     */
    private static @NotNull String generateRandomString(int seed) {
        // The fun thing about this whole function...
        // Is that it's basically a Decimal to whatever base converter
        //  but the output String is reversed
        StringBuilder str = new StringBuilder();

        do {
            int remainder = seed % RANDOM_STRING_CHARACTERS.length;
            str.append(RANDOM_STRING_CHARACTERS[remainder]);
            seed /= RANDOM_STRING_CHARACTERS.length;
        } while (seed != 0);

        return str.toString();
    }

    public static @NotNull String obfuscateProgram(@NotNull CompiledProgram compiledProgram) {
        // TODO: Implement this Again
        return "";
    }
}
