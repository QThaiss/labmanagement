# Test Cases for Create Medical Record API

This document outlines the test cases for the **Create Medical Record API** endpoint:

**Endpoint:** `POST /api/v1/medical-records`

Each test case includes:
- **Test ID**
- **Description**
- **Input (request)**
- **Expected Output**
- **Actual Result**
- **Status**

---

## MR-API-001: Create medical record successfully
**Description:** Create medical record successfully

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "doctorId": "BS557",
  "visitTime": "2025-09-25T14:11:00",
  "lastTestTime": "2025-09-25T14:10:00",
  "notes": "Patient's condition is stable.",
  "status": "COMPLETED"
}
```

**Expected Output:**
```json
201 Created
{
  "success": true,
  "message": "Medical record created successfully",
  "data": {
    "recordId": "MR17587842882926fc316bd2710",
    "patientId": "PT17587842882926fc316bd2710",
    "doctorId": "BS557",
    "visitTime": "2025-09-25T14:11:00",
    "lastTestTime": "2025-09-25T14:10:00",
    "notes": "Patient's condition is stable.",
    "status": "COMPLETED",
    "createdAt": "...",
    "updatedAt": "..."
  },
  "timestamp": "...",
  "status": 201
}
```

**Actual Result:** Same as expected  
**Status:** ✅ PASS

---

## MR-API-002: Validation error - missing patientId
**Description:** Validation error when `patientId` is missing

**Input:**
```http
POST /api/v1/medical-records
{
  "doctorId": "BS557",
  "visitTime": "2025-09-25T14:11:00",
  "lastTestTime": "2025-09-25T14:10:00",
  "notes": "Patient's condition is stable.",
  "status": "COMPLETED"
}
```

**Expected Output / Actual Result:**
```json
400 Bad Request
{
  "status": 400,
  "message": "Validation failed: patientId: Patient ID is required",
  "timestamp": "...",
  "path": "/patient-service/api/v1/medical-records",
  "details": ["patientId: Patient ID is required"]
}
```

**Status:** ✅ PASS

---

## MR-API-003: Validation error - missing visitTime
**Description:** Validation error when `visitTime` is missing

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "doctorId": "BS557",
  "lastTestTime": "2025-09-25T14:10:00",
  "notes": "Patient's condition is stable.",
  "status": "COMPLETED"
}
```

**Expected Output / Actual Result:**
```json
400 Bad Request
{
  "status": 400,
  "message": "Validation failed: visitTime: Visit time is required",
  "timestamp": "...",
  "path": "/patient-service/api/v1/medical-records",
  "details": ["visitTime: Visit time is required"]
}
```

**Status:** ✅ PASS

---

## MR-API-004: Validation error - missing lastTestTime
**Description:** Validation error when `lastTestTime` is missing

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "doctorId": "BS557",
  "visitTime": "2025-09-25T14:11:00",
  "notes": "Patient's condition is stable.",
  "status": "COMPLETED"
}
```

**Expected Output / Actual Result:**
```json
400 Bad Request
{
  "status": 400,
  "message": "Validation failed: lastTestTime: Last test time is required",
  "timestamp": "...",
  "path": "/patient-service/api/v1/medical-records",
  "details": ["lastTestTime: Last test time is required"]
}
```

**Status:** ✅ PASS

---

## MR-API-005: Validation error - invalid status
**Description:** Validation error when status value is invalid

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "doctorId": "BS557",
  "visitTime": "2025-09-25T14:11:00",
  "lastTestTime": "2025-09-25T14:10:00",
  "notes": "Patient's condition is stable.",
  "status": "INVALID_STATUS"
}
```

**Expected Output / Actual Result:**
```json
400 Bad Request
{
  "status": 400,
  "message": "Validation failed: status: Invalid status value",
  "timestamp": "...",
  "path": "/patient-service/api/v1/medical-records",
  "details": ["status: Invalid status value"]
}
```

**Status:** ✅ PASS

---

## MR-API-006: Validation error - missing status
**Description:** Validation error when `status` is missing

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "doctorId": "BS557",
  "visitTime": "2025-09-25T14:11:00",
  "lastTestTime": "2025-09-25T14:10:00",
  "notes": "Patient's condition is stable."
}
```

**Expected Output / Actual Result:**
```json
400 Bad Request
{
  "status": 400,
  "message": "Validation failed: status: Status is required",
  "timestamp": "...",
  "path": "/patient-service/api/v1/medical-records",
  "details": ["status: Status is required"]
}
```

**Status:** ✅ PASS

---

## MR-API-007: Create medical record without doctorId (still success)
**Description:** Create medical record without doctorId

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "visitTime": "2025-09-25T14:11:00",
  "lastTestTime": "2025-09-25T14:10:00",
  "notes": "Patient's condition is stable.",
  "status": "COMPLETED"
}
```

**Expected Output / Actual Result:**
```json
201 Created
{
  "success": true,
  "message": "Medical record created successfully",
  "data": {
    "recordId": "MR17587842882926fc316bd2710",
    "patientId": "PT17587842882926fc316bd2710",
    "doctorId": null,
    "visitTime": "2025-09-25T14:11:00",
    "lastTestTime": "2025-09-25T14:10:00",
    "notes": "Patient's condition is stable.",
    "status": "COMPLETED",
    "createdAt": "...",
    "updatedAt": "..."
  },
  "timestamp": "...",
  "status": 201
}
```

**Status:** ✅ PASS

---

## MR-API-008: Create medical record without notes (still success)
**Description:** Create medical record without notes

**Input:**
```http
POST /api/v1/medical-records
{
  "patientId": "PT17587842882926fc316bd2710",
  "doctorId": "BS557",
  "visitTime": "2025-09-25T14:11:00",
  "lastTestTime": "2025-09-25T14:10:00",
  "status": "COMPLETED"
}
```

**Expected Output / Actual Result:**
```json
201 Created
{
  "success": true,
  "message": "Medical record created successfully",
  "data": {
    "recordId": "MR17587842882926fc316bd2710",
    "patientId": "PT17587842882926fc316bd2710",
    "doctorId": "BS557",
    "visitTime": "2025-09-25T14:11:00",
    "lastTestTime": "2025-09-25T14:10:00",
    "notes": null,
    "status": "COMPLETED",
    "createdAt": "...",
    "updatedAt": "..."
  },
  "timestamp": "...",
  "status": 201
}
```

**Status:** ✅ PASS  
