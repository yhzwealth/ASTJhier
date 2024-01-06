package edu.fudan.selab.entity;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldInfo {
    String name;
    Type type;
    // maybe FieldDeclaration is not a good property for FieldInfo
    FieldDeclaration fd;

    // if type instanceof ClassOrInterfaceType,
    // but now it cannot be retrieved.
    // ClassOrInterfaceDeclaration cid;
}
