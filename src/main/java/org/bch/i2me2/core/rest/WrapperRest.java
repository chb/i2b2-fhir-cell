package org.bch.i2me2.core.rest;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by CH176656 on 4/10/2015.
 */
public class WrapperRest {
    @Inject
    private Logger LOG;


    protected void log(Level level, String message) {
        if (this.LOG == null) {
            this.LOG = Logger.getAnonymousLogger();
        }
        this.LOG.log(level, message);
    }

}
