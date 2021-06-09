import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.BrainfuckClassCompiler;
import me.steinborn.brainchug.reader.BrainfuckLexer;
import me.steinborn.brainchug.tree.BrainfuckTreeProducer;
import me.steinborn.brainchug.tree.ProgramBrainfuckBlock;
import me.steinborn.brainchug.tree.optimizer.GreedyPeepholeOptimizer;

import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Test {
    public static void main(String... args) throws Exception {
        List<BrainfuckKeyword> lexed = BrainfuckLexer.lex(Files.newBufferedReader(Paths.get("test.bf")));
        System.out.println(lexed);

        ProgramBrainfuckBlock tree = BrainfuckTreeProducer.treeify(lexed);
        ProgramBrainfuckBlock opt = new GreedyPeepholeOptimizer().optimize(tree);

        System.out.println("ORIG: " + tree);
        System.out.println("OPT1: " + opt);

        byte[] klazz = BrainfuckClassCompiler.compile(opt);
//        CheckClassAdapter.verify(new ClassReader(klazz), true, new PrintWriter(System.err));

        try (OutputStream s = Files.newOutputStream(Paths.get("Produced.class"), StandardOpenOption.CREATE)) {
            s.write(klazz);
        }

        Class<?> x = MethodHandles.lookup().defineClass(klazz);
        ((Runnable) x.newInstance()).run();
    }
}
