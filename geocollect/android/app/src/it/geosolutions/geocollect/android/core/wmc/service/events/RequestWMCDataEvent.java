package it.geosolutions.geocollect.android.core.wmc.service.events;

import it.geosolutions.geocollect.android.core.wmc.model.WMCCommand;

/**
 * Created by Robert Oehler on 21.11.16.
 *
 * Class to request data from the WMC, providing an optional argument
 */

public class RequestWMCDataEvent {

    private WMCCommand mCommand;
    private Object mArg;

    public RequestWMCDataEvent(final WMCCommand command, final Object arg){

        this.mCommand = command;
        this.mArg = arg;
    }

    public WMCCommand getCommand() {
        return mCommand;
    }

    public Object getArg() {
        return mArg;
    }
}
