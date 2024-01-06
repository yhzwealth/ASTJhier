package edu.fudan.selab.visitor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import edu.fudan.selab.entity.ClassInfo;
import edu.fudan.selab.entity.FieldInfo;
import edu.fudan.selab.entity.MethodInfo;
import edu.fudan.selab.utils.SolverUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ClassInfoCollector extends VoidVisitorAdapter<ClassInfo> {
    private static final Logger logger = LoggerFactory.getLogger(ClassInfoCollector.class);
    private final Map<String, MethodInfo> mim = new HashMap<>();
    private final Map<String, FieldInfo> fdm = new HashMap<>();

    @Override
    public void visit(ClassOrInterfaceDeclaration n, ClassInfo arg) {
        super.visit(n, arg);
        if (n.isInterface()) {
            arg.setClassType("interface");
            visitInterface(n, arg);
            return;
        } else if (n.isAbstract()) {
            arg.setClassType("abstract");
        }
        visitClass(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, ClassInfo arg) {
        super.visit(n, arg);
        List<FieldInfo> fis = new ArrayList<>();
        new FieldInfoCollector().visit(n, fis);
        // TODO: a little bit hacky, due to some String manipulation
        fis.forEach(fi -> this.fdm.put(
                fi.getType() + " " + fi.getName(), fi));
    }

    @Override
    public void visit(ConstructorDeclaration n, ClassInfo arg) {
        super.visit(n, arg);
        HashMap<String, Type> map = new HashMap<>();
        String modifier = n.isPublic() ? "public" : n.isPrivate() ? "private" : n.isProtected() ? "protected" : "";
        for (Parameter parameter : n.getParameters()) {
            map.put(parameter.getName().getIdentifier(), parameter.getType());
        }
        mim.put(n.getDeclarationAsString(false, false, true),
                new MethodInfo(modifier, n.getNameAsString(), map, null,
                        n.getBody(), n.isStatic(), n.isAbstract(), true, false));
    }

    @Override
    public void visit(MethodDeclaration n, ClassInfo arg) {
        super.visit(n, arg);
        HashMap<String, Type> map = new HashMap<>();
        String modifier = n.isPublic() ? "public" : n.isPrivate() ? "private" : n.isProtected() ? "protected" : "";
        for (Parameter parameter : n.getParameters()) {
            map.put(parameter.getName().getIdentifier(), parameter.getType());
        }
        mim.put(n.getDeclarationAsString(false, false, true),
                new MethodInfo(modifier, n.getNameAsString(), map, n.getType(),
                        n.getBody().orElse(null), n.isStatic(), n.isAbstract(), false, false));
    }

    private void visitClass(ClassOrInterfaceDeclaration cid, ClassInfo classInfo) {
        classInfo.setClassName(getFullyQualifiedClassName(cid));
        classInfo.setSuperClassName(getFullyQualifiedSuperClassName(cid));
        classInfo.setSuperClassType(getSuperClassType(cid));
        classInfo.setInterfaces(getFullyQualifiedInterfacesName(cid));
        classInfo.setMethods(mim);
        classInfo.setFields(this.fdm);
    }

    private void visitInterface(ClassOrInterfaceDeclaration cid, ClassInfo classInfo) {
        classInfo.setClassName(getFullyQualifiedClassName(cid));
        classInfo.setSuperClassName(getFullyQualifiedSuperClassName(cid));
        classInfo.setSuperClassType(getSuperClassType(cid));
        classInfo.setMethods(mim);
        classInfo.setFields(this.fdm);
    }

    private String getFullyQualifiedClassName(ClassOrInterfaceDeclaration cid) {
        Optional<String> oFullyQualifiedName = cid.getFullyQualifiedName();
        if (oFullyQualifiedName.isPresent()) {
            return oFullyQualifiedName.get();
        }
        if (SolverUtils.isPresentSymbolSolver()) {
            return cid.resolve().getQualifiedName();
        }
        return cid.getNameAsString();
    }

    private String getFullyQualifiedSuperClassName(ClassOrInterfaceDeclaration cid) {
        // ClassOrInterfaceDeclaration support multi-inherit for interface.
        // Hence, it uses NodeList here.
        NodeList<ClassOrInterfaceType> cts = cid.getExtendedTypes();
        if (cts.isEmpty()) {
            return "java.lang.Object";
        }
        ClassOrInterfaceType cit = cts.get(0);
        if (SolverUtils.isPresentSymbolSolver()) {
            try {
                return cit.resolve().asReferenceType().getQualifiedName();
            }catch (UnsolvedSymbolException ignored){
                logger.error(cit.getNameWithScope() + " cannot be parsed successfully");
            }
        }
        return cit.getNameWithScope();
    }

    private ClassOrInterfaceType getSuperClassType(ClassOrInterfaceDeclaration cid) {
//        assert !cid.isInterface();
        NodeList<ClassOrInterfaceType> extendedTypes = cid.getExtendedTypes();
        if (extendedTypes.isEmpty()) {
            // TODO: java.lang.Object
            return null;
        } else {
            return extendedTypes.get(0);
        }
    }

    private Map<String, ClassOrInterfaceType> getFullyQualifiedInterfacesName(ClassOrInterfaceDeclaration cid) {
        Map<String, ClassOrInterfaceType> idm = new HashMap<>();
        NodeList<ClassOrInterfaceType> its = cid.getImplementedTypes();
        for (ClassOrInterfaceType it : its) {
            if (SolverUtils.isPresentSymbolSolver()) {
                try {
                    idm.put(it.resolve().asReferenceType().getQualifiedName(), it);
                } catch (UnsolvedSymbolException e){
                    logger.error("Type: " + it.getNameAsString() + " don't exist in the libraries");
                    idm.put(it.getNameAsString(), it);
                }
            } else {
                idm.put(it.getNameWithScope(), it);
            }
        }
        return idm;
    }
}
