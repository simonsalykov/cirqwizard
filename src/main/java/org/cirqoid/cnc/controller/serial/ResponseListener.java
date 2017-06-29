package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.Response;

/**
 * Created by simon on 23.06.17.
 */
public interface ResponseListener
{
    void responseReceived(Response response);
}
