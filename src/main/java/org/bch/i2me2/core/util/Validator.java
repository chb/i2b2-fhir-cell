package org.bch.i2me2.core.util;

import org.bch.i2me2.core.exception.I2ME2Exception;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Perfromes basic validations
 * Created by CH176656 on 3/23/2015.
 */
public class Validator {

    /**
     * validates that the string is not null and not empty
     * @param str   The input
     * @param fieldName The field name that will appear in the error message if exception is produced
     * @throws I2ME2Exception   If str is null or empty
     */
    public static void NotNullEmptyStr(String str, String fieldName) throws I2ME2Exception {
        validateNotNull(str, fieldName);
        String field = fieldName;
        if (fieldName==null){
            field = "?";
        }
        if (str.trim().equals("")) throw new I2ME2Exception(field + " cannot be empty");
    }


    /**
     * Validates that the given str is a valid date in the given format
     * @param str           The input
     * @param format        The date format
     * @param fieldName     The field name that will appear in the error message if exception is produced
     * @throws I2ME2Exception If str does not follow the format, or if its null or empty
     */
    public static void validDate(String str, String format, String fieldName) throws I2ME2Exception {
        Validator.validateNotNull(str, fieldName);
        Validator.NotNullEmptyStr(format, "Format expression");
        String field = fieldName;
        if (fieldName==null){
            field = "?";
        }

        SimpleDateFormat dateFormatInput = new SimpleDateFormat(format);
        Date date;
        try {
            date = dateFormatInput.parse(str);
            if (!dateFormatInput.format(date).equals(str)) {
                throw new I2ME2Exception(field + " contains an invalid date:" + str);
            }
        } catch (ParseException e) {
            throw new I2ME2Exception(field + " not formatted properly. Expected format: " + format, e);
        }
    }

    public static void validOptions(String str, List<String> options, String fieldName) throws I2ME2Exception {
        if (options==null) throw new I2ME2Exception("Options list cannot be null");
        String field = fieldName;
        if (fieldName==null){
            field = "?";
        }
        if (!options.contains(str)) throw new I2ME2Exception(str + " is not in the possible options of " + fieldName);
    }

    public static void validRegExp(String str, String format, String fieldName) throws I2ME2Exception {
        Validator.validateNotNull(str, fieldName);
        Validator.NotNullEmptyStr(format, "Format expression");
        Pattern pattern = Pattern.compile(format);
        if (!pattern.matcher(str).find()) {
            String msg = str + " does not follow the correct pattern for " + fieldName + ": " + format;
            throw new I2ME2Exception(msg);
        }
    }

    public static void validateNotNull(String str, String fieldName) throws I2ME2Exception{
        String field = fieldName;
        if (fieldName==null){
            field = "?";
        }
        if (str==null) throw new I2ME2Exception(field + " cannot be null");
    }

}
