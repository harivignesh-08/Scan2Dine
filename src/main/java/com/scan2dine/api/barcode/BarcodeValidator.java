package com.scan2dine.api.barcode;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class BarcodeValidator {

    // Supports standard alphanumeric barcode formats (Code 128 / Code 39 / EAN-13, length 5 to 50)
    private static final Pattern BARCODE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{5,50}$");

    public boolean isValid(String barcode) {
        if (!StringUtils.hasText(barcode)) {
            return false;
        }
        return BARCODE_PATTERN.matcher(barcode.trim()).matches();
    }
}
