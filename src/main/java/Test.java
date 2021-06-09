import me.steinborn.brainchug.BrainfuckKeyword;
import me.steinborn.brainchug.compiler.BrainfuckClassCompiler;
import me.steinborn.brainchug.reader.BrainfuckLexer;
import me.steinborn.brainchug.tree.BrainfuckTreeProducer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Test {
    public static void main(String... args) throws Exception {
        List<BrainfuckKeyword> lexed = BrainfuckLexer.lex(Files.newBufferedReader(Paths.get("test.bf")));
        System.out.println(lexed);

        byte[] klazz = BrainfuckClassCompiler.compile(BrainfuckTreeProducer.treeify(lexed));
        CheckClassAdapter.verify(new ClassReader(klazz), true, new PrintWriter(System.err));

        try (OutputStream s = Files.newOutputStream(Paths.get("Produced.class"), StandardOpenOption.CREATE)) {
            s.write(klazz);
        }

        Class<?> x = MethodHandles.lookup().defineClass(klazz);
        ((Runnable) x.newInstance()).run();
    }
}
