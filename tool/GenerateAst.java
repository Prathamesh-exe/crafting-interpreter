
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        // Validate command-line arguments
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        // Generate the Expr AST class with Binary, Grouping, Literal, Unary, and Ternary expressions
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Ternary  : Expr condition, Expr thenExpr, Expr elseExpr",
                "Unary    : Token operator, Expr right",
                "Variable  : Token name",
                "Assign   : Token name, Expr value"
        ));
// Generate the Stmt AST class with Expression and Print statements
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer"
        ));
    }

    // Generates the abstract base class and all AST node types
    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        try (PrintWriter writer = new PrintWriter(path, "UTF-8")) {
            writer.println("import java.util.List;");
            writer.println();
            writer.println("abstract class " + baseName + " {");

            // Generate the Visitor interface for implementing the visitor pattern
            defineVisitor(writer, baseName, types);

            // Generate concrete AST node classes for each expression type
            for (String type : types) {
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }

            // Generate the abstract accept() method that subclasses must implement
            writer.println();
            writer.println("  abstract <R> R accept(Visitor<R> visitor);");

            writer.println("}");
        }
    }

    // Generates a concrete AST node class with constructor, fields, and visitor method
    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        writer.println("  static class " + className + " extends "
                + baseName + " {");

        // Generate the constructor that initializes fields
        writer.println("    " + className + "(" + fieldList + ") {");

        // Assign constructor parameters to instance fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Generate the accept() method for visitor pattern dispatch
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit"
                + className + baseName + "(this);");
        writer.println("    }");

        // Generate the final fields that hold the node's data
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }

    // Generates the Visitor interface with visit methods for each AST node type
    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        // Create a visit method for each AST node type
        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "("
                    + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
}
