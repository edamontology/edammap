package mapper.core;

import org.apache.jena.rdf.model.Statement;

import java.util.function.Predicate;

/**
 * @author Rabie Saidi
 */
public class StatementFilterFactory {

    public static Predicate<Statement> getFilter(String predicateName){
        Predicate<Statement> filter = new Predicate<Statement>(){
            @Override
            public boolean test(Statement statement) {
                return statement.getPredicate().getLocalName().equals(predicateName);
            }
        };
        return filter;
    }
}
