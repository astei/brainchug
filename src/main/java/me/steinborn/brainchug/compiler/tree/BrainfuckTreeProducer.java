package me.steinborn.brainchug.compiler.tree;

import me.steinborn.brainchug.BrainfuckKeyword;

import java.util.*;

/**
 * Produces a "tree" from the keyword soup provided by the lexer.
 */
public class BrainfuckTreeProducer {
    public static ProgramBrainfuckBlock treeify(List<BrainfuckKeyword> keywords) {
        List<BrainfuckBlock> fundamental = new ArrayList<>();
        Deque<List<BrainfuckBlock>> blocks = new ArrayDeque<>(List.of(fundamental));

        for (BrainfuckKeyword keyword : keywords) {
            if (keyword == BrainfuckKeyword.LOOP_BEGIN) {
                blocks.add(new ArrayList<>());
            } else if (keyword == BrainfuckKeyword.LOOP_END) {
                if (blocks.size() <= 1) {
                    throw new IllegalStateException("Compile error: erroneous extra loop end instruction");
                }
                List<BrainfuckBlock> gathered = blocks.removeLast();
                blocks.peekLast().add(new LoopBrainfuckBlock(gathered));
            } else {
                if (SuperwordBrainfuckBlock.RELEVANT.contains(keyword)) {
                    blocks.peekLast().add(SuperwordBrainfuckBlock.valueOf(keyword, 0, 1));
                } else {
                    blocks.peekLast().add(new InputBrainfuckBlock(keyword));
                }
            }
        }

        if (blocks.size() != 1) {
            throw new IllegalStateException("Loops not terminated");
        }
        return new ProgramBrainfuckBlock(fundamental);
    }
}
