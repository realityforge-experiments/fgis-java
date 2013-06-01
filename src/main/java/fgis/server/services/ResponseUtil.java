package fgis.server.services;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public final class ResponseUtil
{
  private ResponseUtil()
  {
  }

  public static Response entityNotFoundResponse()
  {
    return Response.
      status( 404 ).
      entity( "{\"code\":404,\"message\":\"Unable to locate entity\"}" ).
      type( MediaType.APPLICATION_JSON ).build();
  }
}
