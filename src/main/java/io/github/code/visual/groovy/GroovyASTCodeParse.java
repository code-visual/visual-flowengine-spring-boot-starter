package io.github.code.visual.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * 编译器在将我们的源码编译成字节码之前，
 * 会率先将源代码转换为 AST ( Abstract Syntax Tree，抽象语法树 )，
 * 以便于语义分析。而 Groovy 提供的编译时元编程工具使得我们能够在编译器产出真正的字节码之前截获源代码的 AST，并对其修改。
 *
 * @author Levi Li
 * @since 09/18/2023
 */

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GroovyASTCodeParse implements ASTTransformation {
    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        source.getAST().getClasses().forEach(e -> e.visitContents(new GroovyShellVisitor()));
    }
}
