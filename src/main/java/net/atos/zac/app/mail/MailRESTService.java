package net.atos.zac.app.mail;

import com.mailjet.client.errors.MailjetException;

import net.atos.zac.app.mail.model.RESTMailObject;
import net.atos.zac.mail.MailService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.UUID;

@Singleton
@Path("mail")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MailRESTService {

    @Inject
    private MailService mailService;

    @POST
    @Path("send/{zaakUuid}")
    public int sendMail(@PathParam("zaakUuid") final UUID zaakUuid, final RESTMailObject restMailObject) throws MailjetException {
        return mailService.sendMail(restMailObject.ontvanger, restMailObject.onderwerp,
                                    restMailObject.body, zaakUuid);
    }

}
