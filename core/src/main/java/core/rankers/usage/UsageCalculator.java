package core.rankers.usage;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM6;

/**
 * Calculates the usages of method calls in a project.
 */
public class UsageCalculator {

    /**
     * Map of every method called and its the usage count.
     */
    private HashMap<String, Integer> usages = new HashMap<>();

    /**
     * Constructor: analyze a project and store the usage count of each method.
     *
     * @param classesDir The classes directory of the project that contains all the .class files.
     */
    public UsageCalculator(String classesDir) {
        List<File> files = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);
        for (File file : files) {
            try {
                traverseClass(file);
            } catch (IOException e) {
                // Should not happen
                throw new RuntimeException("Failed processing: " + file.toString());
            }
        }
    }

    /**
     * Get the usage count of a method.
     *
     * @param method Name of the method, including its package name (e.g. some.package.method).
     * @return The usage count, -1 if method does not exist.
     */
    public int getUsageCount(String method) {
        return usages.getOrDefault(method, -1);
    }

    /**
     * Travers a given class file and store the usage count of each call.
     *
     * @param classFile The class file to be traversed.
     * @throws IOException In case of an IO exception.
     */
    private void traverseClass(File classFile) throws IOException {
        InputStream stream = new FileInputStream(classFile);
        ClassReader reader = new ClassReader(stream);
        reader.accept(new MyClassVisitor(), 0);
        stream.close();
    }

    /**
     * Overridden MethodVisitor from ASM.
     */
    private class MyMethodVisitor extends MethodVisitor {

        /**
         * Constructor.
         */
        public MyMethodVisitor() {
            super(ASM6);
        }

        /**
         * Add the method call to the counter.
         */
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean ifx) {
            // TODO: also add `desc` to make distinction between multiple implementations with same method name?
            final String methodName = owner.replace('/', '.') + "." + name;
            usages.put(methodName, usages.getOrDefault(methodName, 0) + 1);
        }
    }

    /**
     * Overridden ClassVisitor from ASM.
     */
    private class MyClassVisitor extends ClassVisitor {

        /**
         * The MethodVisitor that is used in this ClassVisitor.
         */
        private MyMethodVisitor methodVisitor = new MyMethodVisitor();

        /**
         * Constructor.
         */
        public MyClassVisitor() {
            super(ASM6);
        }

        /**
         * Visit a method of the class.
         */
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return methodVisitor;
        }
    }

}
