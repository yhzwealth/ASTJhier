package edu.fudan.selab.visitor;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.fudan.selab.entity.FieldInfo;

import java.util.List;

/**
 * In a single FieldDeclaration, there might be multiple variables declared.
 */
public class FieldInfoCollector extends VoidVisitorAdapter<List<FieldInfo>> {
    @Override
    public void visit(FieldDeclaration n, List<FieldInfo> arg) {
        super.visit(n, arg);
        n.getVariables().forEach(v -> {
            FieldInfo fi = new FieldInfo();
            fi.setName(v.getNameAsString());
            fi.setType(v.getType());
            fi.setFd(n);
            arg.add(fi);
        });
    }
}
