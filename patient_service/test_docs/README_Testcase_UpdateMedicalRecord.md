# Test Cases for UPDATE Medical Record API

This document outlines the test cases for the Update Medical Record API endpoint (`PUT /api/v1/medical-records/{medicalRecordId}`). Each test case includes the test ID, description, request, expected output, actual result, and status.

---

## MR-API-UPD-001: Update medical record successfully

**Description:** Update medical record successfully

**Request**
```http
PUT /api/v1/medical-records/MR175876833278349848bd71383
json
{
  "visitTime": "2025-09-25T10:00:00",
  "lastTestTime": "2025-09-25T11:00:00",
  "status": "ACTIVE",
  "doctorId": "D002",
  "notes": "Updated notes"
}
```

**Expected Output**
```json
{
  "success": true,
  "message": "Medical record updated successfully",
  "data": {
    "medicalRecordId": "MR175876833278349848bd71383",
    "patientId": "PT17586872710677c2777408310",
    "visitTime": "2025-09-25T10:00:00",
    "status": "OPEN",
    "lastTestTime": "2025-09-25T11:00:00",
    "doctorId": "D002",
    "notes": "Updated notes"
  },
  "status": 200
}
```

**Actual Result**
```json
{
  "success": true,
  "message": "Medical record updated successfully",
  "data": {
    "medicalRecordId": "MR175876833278349848bd71383",
    "patientId": "PT17586872710677c2777408310",
    "visitTime": "2025-09-25T10:00:00",
    "status": "OPEN",
    "lastTestTime": "2025-09-25T11:00:00",
    "doctorId": "D002",
    "notes": "Updated notes"
  },
  "status": 200
}
```
**Status:** PASS

---

## MR-API-UPD-002: Validation error - missing all fields

**Description:** Validation error when all fields are missing

**Request**
```http
PUT /api/v1/medical-records/MR175876833278349848bd71383
json
{
  "visitTime": "",
  "lastTestTime": "",
  "status": "",
  "doctorId": "",
  "notes": ""
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "Status cannot be blank",
  "details": ["Status cannot be blank"]
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "Status cannot be blank",
  "details": ["Status cannot be blank"]
}
```
**Status:** PASS

---

## MR-API-UPD-003: Validation error - invalid status

**Description:** Status is not in valid values

**Request**
```http
PUT /api/v1/medical-records/MR175876833278349848bd71383
json
{
  "visitTime": "2025-09-25T10:00:00",
  "lastTestTime": "2025-09-25T11:00:00",
  "status": "OPENn",
  "doctorId": "D002",
  "notes": "Invalid status"
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "Invalid status: OPENn. Valid values are: OPEN, IN_PROGRESS, CLOSED, ARCHIVED"
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "Invalid status: OPENn. Valid values are: OPEN, IN_PROGRESS, CLOSED, ARCHIVED"
}
```
**Status:** PASS

---

## MR-API-UPD-004: Validation error - invalid visitTime format

**Description:** Validation error when visitTime format is invalid

**Request**
```http
PUT /api/v1/medical-records/MR175876833278349848bd71383
json
{
  "visitTime": "25-09-2025 10:00",
  "lastTestTime": "2025-09-25T11:00:00",
  "status": "OPEN",
  "doctorId": "D002",
  "notes": "Invalid time"
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "JSON parse error: Cannot deserialize value of type LocalDateTime from String '25-09-2025 10:00'"
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "JSON parse error: Cannot deserialize value of type LocalDateTime from String '25-09-2025 10:00'"
}
```
**Status:** PASS

---

## MR-API-UPD-005: Status is blank

**Description:** Validation error when status is blank

**Request**
```http
PUT /api/v1/medical-records/{medicalRecordId}
json
{
  "status": ""
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "Status cannot be blank"
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "Status cannot be blank"
}
```
**Status:** PASS

---

## MR-API-UPD-006: VisitTime in the future

**Description:** Validation error when visitTime is in the future

**Request**
```http
PUT /api/v1/medical-records/{medicalRecordId}
json
{
  "visitTime": "2090-09-25T10:00:00"
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "Visit time cannot be in the future"
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "Visit time cannot be in the future"
}
```
**Status:** PASS

---

## MR-API-UPD-007: LastTestTime in the future

**Description:** Validation error when lastTestTime is in the future

**Request**
```http
PUT /api/v1/medical-records/{medicalRecordId}
json
{
  "lastTestTime": "2090-09-25T10:00:00"
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "Last test time cannot be in the future"
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "Last test time cannot be in the future"
}
```
**Status:** PASS

---

## MR-API-UPD-008: Doctor Id is blank

**Description:** Validation error when doctorId is blank

**Request**
```http
PUT /api/v1/medical-records/{medicalRecordId}
json
{
  "doctorId": ""
}
```

**Expected Output**
```json
{
  "status": 400,
  "message": "Doctor ID cannot be blank"
}
```

**Actual Result**
```json
{
  "status": 400,
  "message": "Doctor ID cannot be blank"
}
```
**Status:** PASS
