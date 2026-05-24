
import java.util.HashMap;
import java.util.Map;

class Environment {

    final Environment enclosing;
//for global scope, enclosing is null

    Environment() {
        enclosing = null;
    }
//for nested scope, enclosing is the parent environment

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    //a map to store variable names and their values in the current environment
    private final Map<String, Object> values = new HashMap<>();

    //define a new variable in the current environment
    void define(String name, Object value) {
        values.put(name, value);
    }
//look up a variable by name, starting from the current environment and going up to the enclosing environments if necessary

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    //assign a new value to an existing variable, starting from the current environment and going up to the enclosing environments if necessary
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }
}
