package cecs429.query;

import cecs429.index.Index;
import cecs429.index.KgramIndex;
import cecs429.index.Posting;

import java.util.List;

/**
 * A Query is a piece or whole of a Boolean query, whether that piece is a literal string or represents a merging of
 * other queries. All nodes in a query parse tree are Query objects.
 */
public interface Query{
    /**
     * Retrieves a list of postings for the query, using an Index as the source.
     */
    List<Posting> getPostings(Index index);
    List<Posting> getPostings(Index index, KgramIndex index2);
}