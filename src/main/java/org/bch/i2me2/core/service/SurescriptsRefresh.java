package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.external.IDM;
import org.bch.i2me2.core.external.RXConnect;
import org.bch.i2me2.core.external.I2B2CellFR;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.io.IOException;

/**
 * IME-29
 * Refresh to 12b2 cell
 * Created by CH176656 on 3/19/2015.
 */
@Stateless
public class SurescriptsRefresh extends WrapperService{

    private String token;

    @Inject
    private IDM idm;

    @Inject
    private RXConnect rxConnect;

    @Inject
    private I2B2CellFR i2b2;

    @Inject
    private MapperRxToPDO mapper;

    /**
     * Refresh medication list by making a new call to surescripts and storing the results to i2b2
     * @param token             The token to grab personal information from IDM
     * @throws IOException      If network connection is produced
     * @throws I2ME2Exception   Other errors
     */
    public void refresh(String token) throws IOException, I2ME2Exception {
        this.token = token;
        validate();

        // Get personal information from IDM
        IDM.PersonalInfo phiId = this.getIDM().getPersonalSubjectId(this.token);
        IDM.PersonalInfo phiOther = this.getIDM().getPersonalInfo(this.token);

        // Get data from surescripts
        String json = this.getRXConnect().getMedicationsList(
                phiOther.getFirstName(),
                phiOther.getLastName(),
                phiOther.getZipCode(),
                phiOther.getBirthDate(),
                phiOther.getGender());

        // Transform it to xml pdo
        String xmlPdo = this.getMapper().getPDOXML(
                json,
                phiId.getSubjectId(),
                phiOther.getBirthDate(),
                phiOther.getGender(),
                AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH),
                AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_SURESCRIPT));

        // Push it into i2b2 instance
        this.getI2B2().pushPDOXML(xmlPdo);

    }

    private void validate() throws I2ME2Exception {
        if (token == null) throw new I2ME2Exception ("Token cannot be null");
    }

    private IDM getIDM() {
        if (this.idm==null) {
            this.idm = new IDM();
        }
        return this.idm;
    }

    private RXConnect getRXConnect() {
        if (this.rxConnect==null) {
            this.rxConnect = new RXConnect();
        }
        return this.rxConnect;
    }

    private I2B2CellFR getI2B2() {
        if (this.i2b2==null) {
            this.i2b2 = new I2B2CellFR();
        }
        return this.i2b2;
    }

    private MapperRxToPDO getMapper() {
        if (this.mapper==null) {
            this.mapper = new MapperRxToPDO();
        }
        return this.mapper;
    }
    // Only for testing purposes

    public void setIdm(IDM idm) {
        this.idm = idm;
    }

    public void setRXConnect(RXConnect rxConnect) {
        this.rxConnect = rxConnect;
    }

    public void setI2B2(I2B2CellFR i2b2) {
        this.i2b2 = i2b2;
    }

    public void setMapper(MapperRxToPDO mapper) {
        this.mapper=mapper;
    }
}
