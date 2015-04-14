package org.bch.i2me2.core.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: CH176656
 * Date: 4/5/15
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class WrapperService {
    //@Inject
    private Logger LOG;


    protected void log(Level level, String message) {
        if (this.LOG == null) {
            this.LOG = Logger.getAnonymousLogger();
        }
        this.LOG.log(level, message);
    }
}
