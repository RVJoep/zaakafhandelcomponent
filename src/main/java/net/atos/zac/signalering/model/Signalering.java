/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.signalering.model;

import static net.atos.zac.signalering.model.SignaleringSubject.INFORMATIEOBJECT;
import static net.atos.zac.signalering.model.SignaleringSubject.TAAK;
import static net.atos.zac.signalering.model.SignaleringSubject.ZAAK;
import static net.atos.zac.signalering.model.SignaleringTarget.GROEP;
import static net.atos.zac.signalering.model.SignaleringTarget.MEDEWERKER;
import static net.atos.zac.util.FlywayIntegrator.SCHEMA;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.flowable.idm.api.Group;
import org.flowable.task.api.TaskInfo;

import net.atos.client.zgw.drc.model.EnkelvoudigInformatieobject;
import net.atos.client.zgw.shared.util.URIUtil;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.zac.authentication.Medewerker;

/* Construction is easiest with the factory method in SignaleringService. */
@Entity
@Table(schema = SCHEMA, name = "signalering")
@SequenceGenerator(schema = SCHEMA, name = "sq_signalering", sequenceName = "sq_signalering", allocationSize = 1)
public class Signalering {
    @Id
    @GeneratedValue(generator = "sq_signalering", strategy = GenerationType.SEQUENCE)
    @Column(name = "id_signalering")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "signaleringtype_enum", referencedColumnName = "signaleringtype_enum", nullable = false)
    private SignaleringType type;

    @NotNull
    @Column(name = "targettype_enum", nullable = false)
    @Enumerated(EnumType.STRING)
    private SignaleringTarget targettype;

    @NotBlank
    @Column(name = "target", nullable = false)
    private String target;

    @NotBlank
    @Column(name = "subject", nullable = false)
    private String subject;

    @NotNull
    @Column(name = "tijdstip", nullable = false)
    private ZonedDateTime tijdstip;

    public Long getId() {
        return id;
    }

    public SignaleringType getType() {
        return type;
    }

    public void setType(final SignaleringType type) {
        this.type = type;
    }

    public SignaleringTarget getTargettype() {
        return targettype;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(final Group target) {
        this.targettype = GROEP;
        this.target = target.getId();
    }

    public void setTarget(final Medewerker target) {
        this.targettype = MEDEWERKER;
        this.target = target.getGebruikersnaam();
    }

    public SignaleringSubject getSubjecttype() {
        return getType().getSubjecttype();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final Zaak subject) {
        validSubjecttype(ZAAK);
        this.subject = subject.getUuid().toString();
    }

    public void setSubject(final TaskInfo subject) {
        validSubjecttype(TAAK);
        this.subject = subject.getId();
    }

    public void setSubject(final EnkelvoudigInformatieobject subject) {
        validSubjecttype(INFORMATIEOBJECT);
        this.subject = URIUtil.parseUUIDFromResourceURI(subject.getUrl()).toString();
    }

    private void validSubjecttype(final SignaleringSubject subjecttype) {
        if (type.getSubjecttype() != subjecttype) {
            throw new IllegalArgumentException(String.format("SignaleringType %s expects a %s-type subject", type, type.getSubjecttype()));
        }
    }

    public ZonedDateTime getTijdstip() {
        return tijdstip;
    }

    public void setTijdstip(final ZonedDateTime tijdstip) {
        this.tijdstip = tijdstip;
    }

    @Override
    public String toString() {
        return String.format("%s-signalering voor %s %s (over %s %s)", getType(), getTargettype(), getTarget(), getSubjecttype(), getSubject());
    }
}
