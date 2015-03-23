package org.bch.i2me2.core.service;

import org.bch.i2me2.core.exception.I2ME2Exception;

import java.io.IOException;

/**
 * IME-29
 * Refresh to 12b2 cell
 * Created by CH176656 on 3/19/2015.
 */
public class SurescriptsRefresh {

    private String token;

    public String refresh(String token) throws IOException, I2ME2Exception {
        this.token = token;
        validate();
        return "";
    }

    private void validate() throws I2ME2Exception {
        if (token == null) throw new I2ME2Exception ("Token cannot be null");
    }

}
