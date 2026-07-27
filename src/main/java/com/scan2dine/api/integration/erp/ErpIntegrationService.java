package com.scan2dine.api.integration.erp;

import com.scan2dine.api.dto.request.ErpConfigRequest;
import com.scan2dine.api.dto.response.ErpStatusResponse;
import com.scan2dine.api.dto.response.StudentResponse;

public interface ErpIntegrationService {
    void configureErp(ErpConfigRequest request);
    ErpStatusResponse getErpStatus();
    ErpStudentDto fetchStudentFromErp(String rollNumber);
    StudentResponse syncStudent(String rollNumber);
}
