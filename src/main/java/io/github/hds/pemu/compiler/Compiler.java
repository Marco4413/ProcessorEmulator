package io.github.hds.pemu.compiler;

import io.github.hds.pemu.compiler.parser.*;
import io.github.hds.pemu.instructions.Instruction;
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
                case INSTRUCTION:
                case REGISTER:
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

    private static final char[] ALPHANUMERIC_CHARACTERS = new char[] {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

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
            int remainder = seed % ALPHANUMERIC_CHARACTERS.length;
            str.append(ALPHANUMERIC_CHARACTERS[remainder]);
            seed /= ALPHANUMERIC_CHARACTERS.length;
        } while (seed != 0);

        return str.toString();
    }

    public static @NotNull String obfuscateProgram(@NotNull CompiledProgram compiledProgram) {
        INode[] nodes = compiledProgram.getNodes();
        if (nodes.length == 0)
            return "; This program was obfuscated so much that it's become invisible!";

        // This StringBuilder will store the obfuscated program
        StringBuilder obfProgram = new StringBuilder();
        // An HashMap which contains the renamed names of labels (k: Original Name, v: New Name)
        HashMap<String, String> renamedLabels = new HashMap<>();
        // This is increased each time generateRandomString is called
        int randomStrSeed = 0;

        // Whether or not we're into an Array (Instructions can't be inside Arrays!)
        boolean isInArray = false;
        // How many arguments are left to fulfill the last InstructionNode
        int argsCount = 0;

        for (INode node : nodes) {
            if (
                    // If we're not in an array
                    !isInArray &&
                    // (and) We're not parsing Instruction arguments
                    argsCount <= 0 &&
                    // (and) The current Node isn't an Instruction
                    node.getType() != NodeType.INSTRUCTION &&
                    // (and) The current Node isn't a Label Declaration
                    !(node.getType() == NodeType.LABEL && ((LabelNode) node).isDeclaration())
            ) {
                // Set that we're in an Array and start defining one
                isInArray = true;
                obfProgram.append("#DA { ");

                // This whole thing should make sure to produce the following PEMU code:
                // `l0: #DA { l1 l2: 0 l1: } MOV l2 l1 l3: HLT #DA { 0 0 0 }`
                // The Label declaration check is just to make sure that we're not defining an Array
                //  just for a single label declaration after an Instruction or at the start of a program:
                //  e.g. `... MOV l2 l1 #DA { l3: } HLT ...`
            }

            switch (node.getType()) {
                case ARRAY: {
                    assert isInArray;
                    assert node instanceof ArrayNode;
                    int arrayLength = ((ArrayNode) node).getLength();
                    for (int i = 0; i < arrayLength; i++) {
                        obfProgram.append("0 ");
                    }

                    break;
                }
                case INSTRUCTION: {
                    assert node instanceof InstructionNode;
                    Instruction instruction = ((InstructionNode) node).getInstruction();

                    if (isInArray) obfProgram.append("} ");
                    isInArray = false;

                    obfProgram.append(
                            instruction.getKeyword()
                    ).append(' ');

                    argsCount = instruction.getArgumentsCount();

                    break;
                }
                case REGISTER:
                    assert node instanceof RegisterNode;
                    obfProgram.append(
                            ((RegisterNode) node).getName()
                    ).append(' ');

                    if (argsCount > 0) argsCount--;
                    break;
                case VALUE:
                    assert node instanceof ValueNode;
                    obfProgram.append(
                            ((ValueNode) node).getValue()
                    ).append(' ');

                    if (argsCount > 0) argsCount--;
                    break;
                case OFFSET:
                    assert node instanceof OffsetNode;
                    obfProgram
                            .append('[')
                            .append(((OffsetNode) node).getValue())
                            .append("] ");

                    if (argsCount > 0) argsCount--;
                    break;
                case STRING: {
                    assert isInArray;
                    assert node instanceof StringNode;
                    String str = ((StringNode) node).getString();
                    for (int i = 0; i < str.length(); i++)
                        obfProgram
                                .append((int) str.charAt(i))
                                .append(' ');

                    break;
                }
                case LABEL: {
                    assert node instanceof LabelNode;
                    LabelNode labelNode = (LabelNode) node;
                    String labelName = labelNode.getName();

                    if (!renamedLabels.containsKey(labelName))
                        renamedLabels.put(labelName, "_" + generateRandomString(randomStrSeed++));

                    obfProgram.append(renamedLabels.get(labelName));
                    if (labelNode.isDeclaration())
                        obfProgram.append(": ");
                    else {
                        obfProgram.append(' ');
                        if (argsCount > 0) argsCount--;
                    }

                    break;
                }
                default:
                    throw new IllegalStateException("Invalid Node Reached Compiler.");
            }
        }

        if (isInArray) obfProgram.append('}');
        return obfProgram.toString();
    }
}
