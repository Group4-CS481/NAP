package compiler;

import ast.*;

import java.util.*;
import java.io.*;

public class Interpreter implements Visitor<Value> {
    private Map<String, Value> environment;
    private Map<String, TypeBasic> typeEnvironment;
    private BufferedReader in =
	new BufferedReader(new InputStreamReader(System.in));
    // ...
    @Override
    public Value visit(ExpUnop exp) {
	switch (exp.op){
	case MINUS:
	    // We assume here that the AST is well typed
	    // for e.g. here, exp.exp is supposed to be
	    // an integer, hence the cast is valid.
	    int n = ((Value.Int) exp.exp.accept(this)).value;
	    return new Value.Int(-n);
	case NOT:
	    boolean b = ((Value.Bool) exp.exp.accept(this)).value;
	    return new Value.Bool(!b);
	}
	return Value.none;
    }

    @Override
    public Value visit(ExpBinop exp) {
	Value v1 = exp.left.accept(this);
	Value v2 = exp.right.accept(this);
	switch(exp.op){
	case ADD:
	    return new Value.Int(((Value.Int)v1).value + ((Value.Int)v2).value);
	case MUL:
	    return new Value.Int(((Value.Int)v1).value * ((Value.Int)v2).value);
	case SUB:
	    return new Value.Int(((Value.Int)v1).value - ((Value.Int)v2).value);
	case DIV:
	    return new Value.Int(((Value.Int)v1).value / ((Value.Int)v2).value);
	case MOD:
	    return new Value.Int(((Value.Int)v1).value % ((Value.Int)v2).value);
	case LT:
	    return new Value.Bool(((Value.Int)v1).value < ((Value.Int)v2).value);
	case LE:
	    return new Value.Bool(((Value.Int)v1).value <= ((Value.Int)v2).value);
	case GT:
	    return new Value.Bool(((Value.Int)v1).value > ((Value.Int)v2).value);
	case GE:
	    return new Value.Bool(((Value.Int)v1).value >= ((Value.Int)v2).value);
	case AND:
	    return new Value.Bool(((Value.Bool)v1).value&&((Value.Bool)v2).value);
	case OR:
	    return new Value.Bool(((Value.Bool)v1).value||((Value.Bool)v2).value);
	case EQ:
	    return new Value.Bool(v1.equals(v2));
	case NEQ:
	    return new Value.Bool(!v1.equals(v2));
	}
	return Value.none;
    }

    @Override
    public Value visit(ExpInt num) {
        return new Value.Int(num.value);
    }

    @Override
    public Value visit(ExpBool bool) {
        return new Value.Bool(bool.value);
    }
    
    @Override
    public Value visit(ExpChar char) {
        return new Value.Char(char.value);
    }
    
    public Value visit(ExpString string) {
        return new Value.String(string.value);
    }

    @Override
    public Value visit(ExpVar var) {
        return environment.get(var.name);
    }
    
    

    @Override
    public Value visit(InsInput ins) {
	String input = "";
	Value value = Value.none;
	TypBasic type = typeEnvironment.get(ins.var);
	System.out.print(ins.var + "? ");
	try {
	    input  = in.readLine();
	} catch (IOException e) {  
            System.out.println(e);
	    System.exit(-1);
        }         
	switch (type){
	case INTEGER:
	    value = new Value.Int(Integer.parseInt(input));
	    break;
	case BOOLEAN:
	    value = new Value.Bool(Boolean.parseBoolean(input));
	}
	environment.put(ins.var, value);
	return Value.none;
    }

    @Override
    public Value visit(StmPrint stm) {
	Value v = stm.exp.accept(this);
	if (v.isBool())
	    System.out.println(((Value.Bool)v).value);
	if (v.isInt())
	    System.out.println(((Value.Int)v).value);
	if (v.isChar())
	    System.out.println(((Value.Char)v).value);
	if (v.isFloat())
	    System.out.println(((Value.Float)v).value);
	if (v.isByte())
	    System.out.println(((Value.Byte)v).value);
	if (v.isArray())
	    System.out.println(((Value.Array)v).value);
        return Value.none;
    }

    @Override
    public Value visit(Type type) {
        return new Value.Type(type.type);
    }
    
    @Override
    public Value visit(StmIf stm) {
        Value.Bool condition = (Value.Bool) stm.exp.accept(this);
	if (condition.value)
	    return stm.then_branch.accept(this);
	else
	    return stm.else_branch.accept(this);
    }
    
    @Override
    public Value visit(StmFor stm) {
	for (((Value.Bool)stm.exp.accept(this)).value) {
	    stm.body.accept(this);
	}
	return Value.none;
    }

    @Override
    public Value visit(StmWhile stm) {
	while (((Value.Bool)stm.exp.accept(this)).value) {
	    stm.body.accept(this);
	}
	return Value.none;
    }

    @Override
    public Value visit(StmAssign stm) {
        Value value = stm.exp.accept(this);
        Expression l_value = stm.l_value;
	environment.put(l_value, value);
	return Value.none;
    }
    
    @Override
    public Value visit(StmDecl stm) {
    	for(Pair<String, Type> b : binding)
            b.accept(this);
    	return Value.none;
    }
    
    public Value visit(ExpPredefinedCall exp) {
    	OpPredefined funcName = exp.funcName;
        for (Expression a : exp.arguments)
        	exp.accept(this);
        
        return Value.none;
    }

    @Override
    public Value visit(Block block) {
        for (Statement stm : block.statements)
	    stm.accept(this);
        return Value.none;
    }

    @Override
    public Value visit(FunctionDefinition functionDefinition) {
    	for(Pair<Pair<String, Type>, Boolean> a : functionDefinition.arguments)
            f.accept(this);
    	return Value.none;
    }

    @Override
    public Value visit(Program program) {
        for(FunctionDefinition f : program.functions)
            f.accept(this);
        return program.main.accept(this);
    }

    public Interpreter(Map<String, TypeBasic> te){
	typeEnvironment = te;
	environment =
	    new TreeMap<String, Value>();
    }
}
