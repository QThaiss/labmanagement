# Test Cases for DELETE Medical Record API

This document outlines the test cases for the Delete Medical Record API endpoint (`DELETE /api/v1/medical-records/{medicalRecordId}`). Each test case includes the test ID, description, input (request details), expected output, actual result, and status.

## PAT-API-001: Delete medical record successfully

**Description:** Delete medical record successfully

**Input:**
```json
DELETE /api/v1/medical-records/MR1758810507459a73bb6423874
```

**Expected Output:**
```json
200 Ok

{
      "success": true,
      "message": "Medical record soft deleted successfully",
      "data": {
        "medicalRecordId": "MR1758810507459a73bb6423874",
        "patientId": "PT17586872710677c2777408310",
        "visitTime": "2025-09-24T09:00:00",
        "status": "OPEN",
        "lastTestTime": "2025-09-24T10:00:00",
        "doctorId": null,
        "notes": "No doctor assigned",
        "patient": {
          "patientId": "PT17586872710677c2777408310",
          "fullName": "Tran Thi Nhung",
          "dateOfBirth": "1997-06-10T00:00:00",
          "gender": "MALE"
        },
        "createdAt": "2025-09-25T21:28:27.459952",
        "updatedAt": "2025-09-28T01:54:48.480589",
        "deletedAt": "2025-09-27T16:30:39.8714729",
        "createdBy": null,
        "updatedBy": null,
      },
      "timestamp": "2025-09-28T02:05:16.4409514",
      "status": 200
}
```

**Actual Result:**
```json
200 Ok

{
      "success": true,
      "message": "Medical record soft deleted successfully",
      "data": {
        "medicalRecordId": "MR1758810507459a73bb6423874",
        "patientId": "PT17586872710677c2777408310",
        "visitTime": "2025-09-24T09:00:00",
        "status": "OPEN",
        "lastTestTime": "2025-09-24T10:00:00",
        "doctorId": null,
        "notes": "No doctor assigned",
        "patient": {
          "patientId": "PT17586872710677c2777408310",
          "fullName": "Tran Thi Nhung",
          "dateOfBirth": "1997-06-10T00:00:00",
          "gender": "MALE"
        },
        "createdAt": "2025-09-25T21:28:27.459952",
        "updatedAt": "2025-09-28T01:54:48.480589",
        "deletedAt": "2025-09-27T16:30:39.8714729",
        "createdBy": null,
        "updatedBy": null,
      },
      "timestamp": "2025-09-28T02:05:16.4409514",
      "status": 200
}
```

**Status:** PASS

## PAT-API-002: Not found: medical record ID

**Description:** Not found medical record ID

**Input:**
```json
DELETE /api/v1/medical-records/123
```

**Expected Output:**
```json
404 Not Found

{
    "status": 404,
    "message": "Medical record not found with ID: 123",
    "timestamp": "2025-09-28T02:03:30.4954711",
    "path": "/patient-service/api/v1/medical-records/123",
    "details": [
      "Medical record not found with ID: 123"
    ]
}
```

**Actual Result:**
```json
404 Not Found

{
    "status": 404,
    "message": "Medical record not found with ID: 123",
    "timestamp": "2025-09-28T02:03:30.4954711",
    "path": "/patient-service/api/v1/medical-records/123",
    "details": [
      "Medical record not found with ID: 123"
    ]
}
```

**Status:** PASS