package net.anotheria.anodoc.query2;

/**
 * Case-insensitive version of {@link QueryLikeProperty}.
 */
public class QueryCaseInsensitiveLikeProperty extends QueryLikeProperty {

    /**
     * Constructor.
     *
     * @param aName  property name
     * @param aValue property value
     */
    public QueryCaseInsensitiveLikeProperty(String aName, Object aValue) {
        super(aName, aValue);
    }

    @Override
    public String getComparator() {
        return " ilike ";
    }

    @Override
    public boolean doesMatch(Object o) {
        return o == null ? getOriginalValue() == null :
                o.toString().toLowerCase().indexOf(getOriginalValue().toString().toLowerCase()) != -1;
    }

}