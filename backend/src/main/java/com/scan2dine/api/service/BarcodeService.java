package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.BarcodeRegistrationRequest;
import com.scan2dine.api.dto.request.BarcodeScanRequest;
import com.scan2dine.api.dto.response.BarcodeScanResponse;

public interface BarcodeService {
    void registerBarcode(BarcodeRegistrationRequest request);
    BarcodeScanResponse scanBarcode(BarcodeScanRequest request);
}
