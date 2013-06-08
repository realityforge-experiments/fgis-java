package fgis.server.services;

import javax.persistence.NoResultException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Map the NoResultException to an appropriate HTTP response.
 */
@Provider
public class NoResultExceptionMapper
  implements ExceptionMapper<NoResultException>
{
  public Response toResponse( final NoResultException nre )
  {
    return Response.
      status( 404 ).
      entity( nre.getMessage() ).
      type( MediaType.TEXT_PLAIN ).build();
  }
}
