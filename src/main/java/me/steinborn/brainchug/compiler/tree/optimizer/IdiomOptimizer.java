package me.steinborn.brainchug.compiler.tree.optimizer;

import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.tree.BrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.LoopBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.ProgramBrainfuckBlock;
import me.steinborn.brainchug.compiler.tree.SuperwordBrainfuckBlock;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.CASTORE;

/**
 * Tries to optimize some common idioms in Brainfuck.
 */
public class IdiomOptimizer implements Optimizer {
    @Override
    public ProgramBrainfuckBlock optimize(ProgramBrainfuckBlock in) {
        return new ProgramBrainfuckBlock(process(in.getBlocks()));
    }

    private List<BrainfuckBlock> process(List<BrainfuckBlock> blocks) {
        List<BrainfuckBlock> optimized = new ArrayList<>();

        for (BrainfuckBlock block : blocks) {
            if (block instanceof LoopBrainfuckBlock) {
                List<BrainfuckBlock> loopBlocks = ((LoopBrainfuckBlock) block).getBlocks();
                if (loopBlocks.size() == 1 && loopBlocks.get(0) instanceof SuperwordBrainfuckBlock &&
                        mightDecrement((SuperwordBrainfuckBlock) loopBlocks.get(0))) {
                    // Common idiom: [-] will set the value in the given cell at the position to 0.
                    optimized.add(new ZeroIdiomInstruction());
                } else {
                    optimized.add(new LoopBrainfuckBlock(process(((LoopBrainfuckBlock) block).getBlocks())));
                }
            } else {
                optimized.add(block);
            }
        }

        return optimized;
    }

    private static boolean mightDecrement(SuperwordBrainfuckBlock block) {
        return block.getKeyword() == BrainfuckKeyword.DECREMENT_VAL
                || (block.getKeyword() == BrainfuckKeyword.INCREMENT_VAL && Math.signum(block.getCount()) == -1);
    }

    private static class ZeroIdiomInstruction implements BrainfuckBlock {

        @Override
        public void emit(MethodVisitor mv) {
            mv.visitInsn(DUP2);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(CASTORE);
        }

        @Override
        public String toString() {
            return "ZERO";
        }
    }
}
