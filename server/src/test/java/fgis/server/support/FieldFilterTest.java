package fgis.server.support;

import java.text.ParseException;
import java.util.Collection;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class FieldFilterTest
{
  @Test
  public void simpleFiltering()
    throws Exception
  {
    final FieldFilter filter = FieldFilter.parse( "foo,bar" );

    assertTrue( filter.allow( "foo" ) );
    assertTrue( filter.allow( "bar" ) );
    assertFalse( filter.allow( "baz" ) );
    final Collection<String> fields = filter.allowedFields();
    assertNotNull( fields );
    assertEquals( fields.size(), 2 );
    assertTrue( fields.contains( "foo" ) );
    assertTrue( fields.contains( "bar" ) );
    assertFalse( fields.contains( "baz" ) );
  }

  @Test
  public void nullOpenFilter()
    throws Exception
  {
    final FieldFilter filter = FieldFilter.parse( null );

    assertTrue( filter.allow( "foo" ) );
    assertTrue( filter.allow( "bar" ) );
    assertTrue( filter.allow( "baz" ) );
    assertNull( filter.allowedFields() );
  }

  @Test
  public void emptyOpenFilter()
    throws Exception
  {
    final FieldFilter filter = FieldFilter.parse( "" );

    assertTrue( filter.allow( "foo" ) );
    assertTrue( filter.allow( "bar" ) );
    assertTrue( filter.allow( "baz" ) );
    assertNull( filter.allowedFields() );
  }

  @Test
  public void subEmptyField()
    throws Exception
  {
    final FieldFilter filter = FieldFilter.parse( "foo[]" );

    assertTrue( filter.allow( "foo" ) );

    assertTrue( filter.subFilter( "foo" ).allow( "X" ) );
  }

  @Test
  public void multipleSubFields()
    throws Exception
  {
    final FieldFilter filter = FieldFilter.parse( "foo[],bar[]" );

    assertTrue( filter.allow( "foo" ) );
    assertTrue( filter.allow( "bar" ) );

    assertTrue( filter.subFilter( "foo" ).allow( "X" ) );
    assertTrue( filter.subFilter( "bar" ).allow( "X" ) );
  }

  @Test
  public void subFieldsWithFilters()
    throws Exception
  {
    final FieldFilter filter = FieldFilter.parse( "foo[bar,baz[bik]]" );

    assertTrue( filter.allow( "foo" ) );
    assertFalse( filter.allow( "bar" ) );

    assertFalse( filter.subFilter( "foo" ).allow( "X" ) );
    assertTrue( filter.subFilter( "foo" ).allow( "bar" ) );
    assertTrue( filter.subFilter( "foo" ).subFilter( "bar" ).allow( "X" ) );
    assertTrue( filter.subFilter( "foo" ).allow( "baz" ) );
    assertFalse( filter.subFilter( "foo" ).subFilter( "baz" ).allow( "X" ) );
    assertTrue( filter.subFilter( "foo" ).subFilter( "baz" ).allow( "bik" ) );
  }

  @Test( expectedExceptions = ParseException.class )
  public void incompleteField()
    throws ParseException
  {
    FieldFilter.parse( "foo," );
  }

  @Test( expectedExceptions = ParseException.class )
  public void badSubField()
    throws ParseException
  {
    FieldFilter.parse( "foo[" );
  }

  @Test( expectedExceptions = ParseException.class )
  public void badSubSubField()
    throws ParseException
  {
    FieldFilter.parse( "foo[x,b[c,d]" );
  }
}
