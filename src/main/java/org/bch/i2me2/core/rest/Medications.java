package org.bch.i2me2.core.rest;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.external.IDM;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

/**
 * Created by CH176656 on 4/7/2015.
 */

@Path("/medications")
@RequestScoped
public class Medications {

    @Inject
    private IDM idm;

    @POST
    @Path("/getMedications")
    @Produces("application/xml")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEcho(@QueryParam("subject_token") String token, @Context SecurityContext sc) {
        IDM.PersonalInfo phi;
        try {
            phi = idm.getPersonalSubjectId(token);
        } catch (I2ME2Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.OK).build();
    }
}
