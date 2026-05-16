# AST Generator Guide

## Overview

`GenerateAst.java` is a code generator that automatically creates an Abstract Syntax Tree (AST) class hierarchy. It takes a specification of expression types and generates Java code with full visitor pattern implementation.

## What It Does

1. Reads expression type definitions (e.g., `Binary: Expr left, Token operator, Expr right`)
2. Generates an abstract base class (`Expr`) with concrete subclasses for each expression type
3. Creates a `Visitor` interface for implementing the visitor pattern
4. Generates `accept()` methods in each AST node class

## How It Works

```bash
java GenerateAst <output_directory>
```

Example:

```bash
java GenerateAst ../lox
```

This generates `../lox/Expr.java` containing:

- `Binary`, `Grouping`, `Literal`, `Unary` classes (AST node types)
- Constructors and field initialization
- Visitor pattern support methods

## The Visitor Pattern (Brief)

A design pattern that separates data structures (AST nodes) from operations on them:

- Each AST node implements `accept(Visitor v)` that calls `v.visitNodeType(this)`
- The `Visitor` interface defines a method for each node type
- To add new operations, create a new Visitor implementation instead of modifying AST classes
- This avoids adding logic directly to node classes and keeps concerns separated

**Example**: `AstPrinter` implements `Visitor` to print the AST; `Interpreter` implements `Visitor` to evaluate it.

## Output Structure

Generated `Expr.java` contains:

- **Interface `Visitor<R>`**: Defines visit methods for each expression type
- **Static classes**: `Binary`, `Grouping`, `Literal`, `Unary` extending `Expr`
- **Abstract method `accept()`**: Implemented by each node class to dispatch to visitors
