/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.websocket;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import net.atos.zac.websocket.event.ScreenEvent;

/**
 * This Registry is used to maintain a list of active sessions.
 * EventSessions contains all (from the browser) registered client sessions
 */
@ApplicationScoped
public class SessionRegistry {

    private static final Pattern QUOTED = Pattern.compile("^\"(.*)\"$");

    private final SetMultimap<ScreenEvent, Session> eventSessions = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    /**
     * Return a set of all active sessions for a particular event.
     *
     * @return Set with active sessions
     */
    public Set<Session> listSessions(final ScreenEvent event) { // TODO ESUITEDEV-25898 Support wildcard for the opcode?
        return Collections.unmodifiableSet(eventSessions.get(fix(event)));
    }

    /**
     * Add a session for a specific event
     *
     * @param event   event
     * @param session session
     */
    public void create(final ScreenEvent event, final Session session) {
        if (session != null) {
            eventSessions.put(fix(event), session);
        }
    }

    /**
     * Delete a session for a specific event
     *
     * @param event   event
     * @param session session
     */
    public void delete(final ScreenEvent event, final Session session) {
        if (session != null) {
            eventSessions.get(fix(event)).remove(session);
        }
    }

    /**
     * Delete a session for all events
     *
     * @param session session
     */
    public void deleteAll(final Session session) {
        if (session != null) {
            eventSessions.values().removeAll(Collections.singleton(session));
        }
    }

    /**
     * This method is applied to all event arguments to make sure that the opbjectId being quoted (by Angular?) doesn't cause any problems.
     * Events that are otherwise equal except for the quoted/unquoted objectIds should in all cases be regarded as the same event.
     *
     * @param event the event in which the objectId may have been quoted
     * @return an event in which the objectId is stripped of any quotes
     */
    public ScreenEvent fix(final ScreenEvent event) {
        final Matcher matcher = QUOTED.matcher(event.getObjectId());
        return matcher.matches() ? fix(new ScreenEvent(event.getOperation(), event.getObjectType(), matcher.replaceAll("$1"))) : event;
    }
}
