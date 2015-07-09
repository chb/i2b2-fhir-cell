package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireAnswers;
import ca.uhn.fhir.parser.IParser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ipinyol on 7/9/15.
 */
public class QAnswerToI2B2Test {
    protected FhirContext ctx = FhirContext.forDstu2();

    @Test
    public void basicTest() throws Exception {
        String jsonStr = readTextFile("fhirQA_0.json");
        IParser jsonParser = this.ctx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        QuestionnaireAnswers qa = (QuestionnaireAnswers) jsonParser.parseResource(jsonStr);
        QAnswersToI2B2 mapper = new QAnswersToI2B2();
        String pdoxml = mapper.getPDOXML(qa);
        System.out.println(pdoxml);
    }

    private String readTextFile(String fileName) throws Exception {
        InputStream in = QAnswerToI2B2Test.class.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sBuffer = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sBuffer.append(line).append('\n');
            }
        } catch(Exception e) {
            e.printStackTrace();

        } finally {
            in.close();
        }
        return sBuffer.toString();
    }
}
