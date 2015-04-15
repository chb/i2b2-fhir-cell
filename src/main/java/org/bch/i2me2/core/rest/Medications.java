package org.bch.i2me2.core.rest;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.external.IDM;
import org.bch.i2me2.core.service.MedicationsManagement;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by CH176656 on 4/7/2015.
 */

@Path("/medications")
@RequestScoped
@Stateful
public class Medications extends WrapperRest {

    @Inject
    private IDM idm;

    @Inject
    private MedicationsManagement medicationsManagement;

    private static String MODULE = "[REST][MEDICATIONS]";
    private static String OP_GET_MEDICATIONS = "[GET_MEDICATIONS]";
    private static String OP_GET_MEDICATIONS_BYPASS = "[GET_MEDICATIONS_BYPASS]";
    private static String OP_PUT_MEDICATIONS = "[PUT_MEDICATIONS]";
    private static String OP_PUT_MEDICATIONS_BYPASS = "[PUT_MEDICATIONS_BYPASS]";


    @POST
    @Path("/getMedications/{subject_token}")
    @Produces("application/xml")
    public Response getMedications(@PathParam("subject_token") String token, @Context SecurityContext sc) {
        this.log(Level.INFO, MODULE + OP_GET_MEDICATIONS + "IN. Auth User:" + sc.getUserPrincipal().getName());
        IDM.PersonalInfo phi;
        String pdoxml;

        try {
            phi = idm.getPersonalSubjectId(token);
            pdoxml = this.medicationsManagement.getMedications(phi.getSubjectId(), token);
        } catch (I2ME2Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.OK).entity(pdoxml).build();
    }

    @POST
    @Path("/getMedicationsByPass/{subjectId}")
    @Produces("application/xml")
    public Response getMedicationsByPass(@PathParam("subjectId") String subjectId, @Context SecurityContext sc) {
        this.log(Level.INFO, MODULE + OP_GET_MEDICATIONS_BYPASS + "IN. Auth User:" + sc.getUserPrincipal().getName());
        try {
            if (AppConfig.getProp(AppConfig.BYPASS_IDM).trim().toLowerCase().equals("no")) {
                Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        String pdoxml;
        try {
            pdoxml = this.medicationsManagement.getMedications(subjectId, null);
        } catch (I2ME2Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.OK).entity(pdoxml).build();
    }

    @POST
    @Path("/putMedicationsByPass/{subjectId}")
    @Consumes("application/json")
    public Response putMedicationsByPass(String json, @PathParam("subjectId") String subjectId, @Context SecurityContext sc) {
        this.log(Level.INFO, MODULE + OP_PUT_MEDICATIONS_BYPASS + "IN. Auth User:" + sc.getUserPrincipal().getName());
        try {
            if (AppConfig.getProp(AppConfig.BYPASS_IDM).trim().toLowerCase().equals("no")) {
                Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        try {
            this.medicationsManagement.putMedications(subjectId, json);
        } catch (I2ME2Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Path("/putMedications/{subject_token}")
    @Consumes("application/json")
    public Response putMedications(String json, @PathParam("subject_token") String token, @Context SecurityContext sc) {
        this.log(Level.INFO, MODULE + OP_PUT_MEDICATIONS + "IN. Auth User:" + sc.getUserPrincipal().getName());
        IDM.PersonalInfo phi;

        try {
            phi = idm.getPersonalSubjectId(token);
            this.medicationsManagement.putMedications(phi.getSubjectId(), json);
        } catch (I2ME2Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    // For testing purposes only
    public void setIDM(IDM idm) {
        this.idm = idm;
    }

    public void setMedicationsManagement(MedicationsManagement medicationsManagement) {
        this.medicationsManagement = medicationsManagement;
    }
}
