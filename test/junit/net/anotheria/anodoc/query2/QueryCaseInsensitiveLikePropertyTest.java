package net.anotheria.anodoc.query2;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link QueryCaseInsensitiveLikeProperty}.
 *
 * @author Alex Osadchy
 */
public class QueryCaseInsensitiveLikePropertyTest {

    @Test
    public void test() {
        QueryCaseInsensitiveLikeProperty qp = new QueryCaseInsensitiveLikeProperty("name", "ValUE");
        assertTrue("Should match", qp.doesMatch("value"));
        assertTrue("Should match", qp.doesMatch("VALUE"));
        assertTrue("Should match", qp.doesMatch("VALuE"));
        assertFalse("Should match", qp.doesMatch("VaUE"));
    }

}