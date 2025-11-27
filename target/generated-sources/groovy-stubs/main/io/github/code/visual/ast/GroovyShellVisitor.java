package io.github.code.visual.ast;

public class GroovyShellVisitor
  extends org.codehaus.groovy.ast.ClassCodeVisitorSupport  implements
    groovy.lang.GroovyObject {
;
public GroovyShellVisitor
(org.codehaus.groovy.control.SourceUnit source) {}
@groovy.transform.Generated() @groovy.transform.Internal() @java.beans.Transient() public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
@groovy.transform.Generated() @groovy.transform.Internal() public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  org.codehaus.groovy.control.SourceUnit getSource() { return (org.codehaus.groovy.control.SourceUnit)null;}
public  void setSource(org.codehaus.groovy.control.SourceUnit value) { }
@java.lang.Override() public  void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression expression) { }
@java.lang.Override() public  void visitMethod(org.codehaus.groovy.ast.MethodNode methodNode) { }
@java.lang.Override() public  void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression call) { }
@java.lang.Override() public  void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression call) { }
@java.lang.Override() public  void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression expression) { }
@java.lang.Override() public  void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression expression) { }
@java.lang.Override() protected  org.codehaus.groovy.control.SourceUnit getSourceUnit() { return (org.codehaus.groovy.control.SourceUnit)null;}
}
