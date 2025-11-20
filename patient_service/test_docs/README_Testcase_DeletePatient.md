# Test Cases for DELETE Patient API

This document outlines the test cases for the Delete Patient API endpoint (`DELETE /api/v1/patients/{patientId}`). Each test case includes the test ID, description, input (request details), expected output, actual result, and status.

## PAT-API-001: Delete patient successfully

**Description:** Delete patient successfully

**Input:**
```json
DELETE /api/v1/patients/PT17587842882926fc316bd2710
```

**Expected Output:**
```json
200 Ok

{
    "success": true,
    "message": "Patient soft deleted successfully",
    "data": {
        "patientId": "PT17587842882926fc316bd2710",
        "patientCode": "PC1758784288292928a8bfd9110",
        "fullName": "Nguyen Van Dau",
        "dateOfBirth": "1995-01-22T00:00:00",
        "gender": "FEMALE",
        "phone": "0231222552",
        "email": "nguyenvandau@gmail.com",
        "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
        "createdAt": "2025-09-25T14:11:28.290913",
        "updatedAt": "2025-09-27T16:30:39.9130201",
        "deletedAt": "2025-09-27T16:30:39.8714729",
        "createdBy": "BS557",
        "updatedBy": null
    },
    "timestamp": "2025-09-25T14:11:30.455615",
    "status": 200
}
```

**Actual Result:**
```json
200 Ok

{
    "success": true,
    "message": "Patient soft deleted successfully",
    "data": {
        "patientId": "PT17587842882926fc316bd2710",
        "patientCode": "PC1758784288292928a8bfd9110",
        "fullName": "Nguyen Van Dau",
        "dateOfBirth": "1995-01-22T00:00:00",
        "gender": "FEMALE",
        "phone": "0231222552",
        "email": "nguyenvandau@gmail.com",
        "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
        "createdAt": "2025-09-25T14:11:28.290913",
        "updatedAt": "2025-09-27T16:30:39.9130201",
        "deletedAt": "2025-09-27T16:30:39.8714729",
        "createdBy": "BS557",
        "updatedBy": null
    },
    "timestamp": "2025-09-25T14:11:30.455615",
    "status": 200
}
```

**Status:** PASS

## PAT-API-002: Not found: patient ID

**Description:** Not found patient ID

**Input:**
```json
DELETE /api/v1/patients/123
```

**Expected Output:**
```json
404 Not Found

{
    "status": 404,
    "message": "Patient not found with ID: 123",
    "timestamp": "2025-09-27T16:33:58.5627275",
    "path": "/patient-service/api/v1/patients/123",
    "details": [
        "Patient not found with ID: 123"
    ]
}
```

**Actual Result:**
```json
404 Not Found

{
    "status": 404,
    "message": "Patient not found with ID: 123",
    "timestamp": "2025-09-27T16:33:58.5627275",
    "path": "/patient-service/api/v1/patients/123",
    "details": [
        "Patient not found with ID: 123"
    ]
}
```

**Status:** PASS