package com.github.eyesli.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
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
