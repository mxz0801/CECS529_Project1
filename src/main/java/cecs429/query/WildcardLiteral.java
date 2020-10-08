package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

public class WildcardLiteral implements Query {
    public WildcardLiteral(String substring) {
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return null;
    }
}
