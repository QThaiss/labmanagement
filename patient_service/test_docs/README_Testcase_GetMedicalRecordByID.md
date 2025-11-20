# Test Cases for GET Medical Record By ID API

This document outlines the test cases for the GET Medical Record By ID API endpoint (`GET /api/v1/medical-records/{medicalRecordId}`). Each test case includes the test ID, description, input (request details), expected output, actual result, and status.

## PAT-API-001: Get medical record by id successfully

**Description:** Get medical record by id successfully

**Input:**
```json
GET /api/v1/medical-records/MR1758809554163f27dd1f14135
```

**Expected Output:**
```json
200 Ok

{
    "success": true,
    "message": "Completed successfully",
    "data": {
    "medicalRecordId": "MR1758809554163f27dd1f14135",
    "patientId": "PT17586872710677c2777408310",
    "visitTime": "2025-09-24T09:00:00",
    "status": "OPEN",
    "lastTestTime": "2025-09-24T10:00:00",
    "doctorId": "D001",
    "notes": "First visit",
    "patient": {
    "patientId": "PT17586872710677c2777408310",
    "fullName": "Tran Thi Nhung",
    "dateOfBirth": "1997-06-10T00:00:00",
    "gender": "MALE"
    },
    "createdAt": "2025-09-25T21:12:34.162414",
    "updatedAt": "2025-09-25T21:12:34.162414",
    "deletedAt": null,
    "createdBy": null,
    "updatedBy": null
    },
    "timestamp": "2025-09-29T21:22:09.9967766",
    "status": 200
}
```

**Actual Result:**
```json
200 Ok

{
    "success": true,
    "message": "Completed successfully",
    "data": {
    "medicalRecordId": "MR1758809554163f27dd1f14135",
    "patientId": "PT17586872710677c2777408310",
    "visitTime": "2025-09-24T09:00:00",
    "status": "OPEN",
    "lastTestTime": "2025-09-24T10:00:00",
    "doctorId": "D001",
    "notes": "First visit",
    "patient": {
    "patientId": "PT17586872710677c2777408310",
    "fullName": "Tran Thi Nhung",
    "dateOfBirth": "1997-06-10T00:00:00",
    "gender": "MALE"
    },
    "createdAt": "2025-09-25T21:12:34.162414",
    "updatedAt": "2025-09-25T21:12:34.162414",
    "deletedAt": null,
    "createdBy": null,
    "updatedBy": null
    },
    "timestamp": "2025-09-29T21:22:09.9967766",
    "status": 200
}
```

**Status:** PASS

## PAT-API-002: Not found: medical record ID

**Description:** Not found medical record ID

**Input:**
```json
GET /api/v1/medical-records/123
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

## PAT-API-003: Not found: medical record soft deleted

**Description:** Not found  medical record soft deleted

**Input:**
```json
GET /api/v1/medical-records/MR1758810507459a73bb6423874
```

**Expected Output:**
```json
404 Not Found

{
    "status": 404,
    "message": "Medical record not found with ID: MR1758810507459a73bb6423874",
    "timestamp": "2025-09-29T21:24:52.5751547",
    "path": "/patient-service/api/v1/medical-records/MR1758810507459a73bb6423874",
    "details": [
    "Medical record not found with ID: MR1758810507459a73bb6423874"
    ]
}
```

**Actual Result:**
```json
404 Not Found

{
    "status": 404,
    "message": "Medical record not found with ID: MR1758810507459a73bb6423874",
    "timestamp": "2025-09-29T21:24:52.5751547",
    "path": "/patient-service/api/v1/medical-records/MR1758810507459a73bb6423874",
    "details": [
    "Medical record not found with ID: MR1758810507459a73bb6423874"
    ]
}
```

**Status:** PASS