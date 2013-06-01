package fgis.server.support;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FieldFilter
{
  private static final FieldFilter ALLOW_ALL = new FieldFilter( null );

  /**
   * If this field is null then all fields are accepted.
   */
  @Nullable
  private final Map<String, FieldFilter> _fields;

  private FieldFilter( final Map<String, FieldFilter> fields )
  {
    _fields = fields;
  }

  public boolean allow( @Nonnull final String field )
  {
    return null == _fields || _fields.containsKey( field );
  }

  @Nonnull
  public FieldFilter subFilter( @Nonnull final String field )
  {
    if ( null == _fields )
    {
      return this;
    }
    else
    {
      final FieldFilter filter = _fields.get( field );
      if ( null == filter )
      {
        throw new IllegalArgumentException( "Requested sub-filter for filtered field: " + field );
      }
      return filter;
    }
  }

  @Nullable
  public Set<String> allowedFields()
  {
    return null == _fields ? null : _fields.keySet();
  }

  @Nonnull
  public static FieldFilter parse( @Nullable final String path )
    throws ParseException
  {
    if ( null == path || "".equals( path ) )
    {
      return ALLOW_ALL;
    }
    final Map<String, FieldFilter> fields = new HashMap<String, FieldFilter>();
    int start = 0;
    int current = start;
    int initialBracket = start;
    int depth = 0;
    boolean complex = false;
    while ( current <= path.length() )
    {
      if ( 0 != depth && current == path.length() )
      {
        throw new ParseException( path, current );
      }
      else if ( 0 == depth && ( current == path.length() || ',' == path.charAt( current ) ) )
      {
        if ( complex )
        {
          final String fieldKey = path.substring( start, initialBracket );
          fields.put( fieldKey, parse( path.substring( initialBracket + 1, current - 1 ) ) );
        }
        else
        {
          final String fieldName = path.substring( start, current );
          if( 0 == fieldName.length() )
          {
            throw new ParseException( path, current );
          }
          fields.put( fieldName, ALLOW_ALL );
        }
        start = current + 1;
        complex = false;
        initialBracket = -1;
      }
      else if ( '[' == path.charAt( current ) )
      {
        if ( 0 == depth )
        {
          initialBracket = current;
        }
        depth++;
        complex = true;
      }
      else if ( ']' == path.charAt( current ) )
      {
        if ( 0 == depth )
        {
          throw new ParseException( path, current );
        }
        else
        {
          depth--;
        }
      }
      current++;
    }

    return new FieldFilter( Collections.unmodifiableMap( fields ) );
  }
}
