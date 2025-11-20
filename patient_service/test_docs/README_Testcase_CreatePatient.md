# Test Cases for Create Patient API

This document outlines the test cases for the Create Patient API endpoint (`POST /api/v1/patients`). Each test case includes the test ID, description, input (request details), expected output, actual result, and status.

## PAT-API-001: Create patient successfully

**Description:** Create patient successfully

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS557"
}
```

**Expected Output:**
```json
201 Created

{
    "success": true,
    "message": "Patient created successfully",
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
        "updatedAt": "2025-09-25T14:11:28.290913",
        "createdBy": "BS557",
        "updatedBy": null
    },
    "timestamp": "2025-09-25T14:11:30.455615",
    "status": 201
}
```

**Actual Result:**
```json
201 Created

{
    "success": true,
    "message": "Patient created successfully",
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
        "updatedAt": "2025-09-25T14:11:28.290913",
        "createdBy": "BS557",
        "updatedBy": null
    },
    "timestamp": "2025-09-25T14:11:30.455615",
    "status": 201
}
```

**Status:** PASS

## PAT-API-002: Validation error: missing fullName

**Description:** Validation error: missing fullName

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandaugmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS558"
}
```

**Expected Output:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: fullName: Full name is required",
    "timestamp": "2025-09-25T14:16:27.3619523",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "fullName: Full name is required"
    ]
}
```

**Actual Result:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: fullName: Full name is required",
    "timestamp": "2025-09-25T14:16:27.3619523",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "fullName: Full name is required"
    ]
}
```

**Status:** PASS

## PAT-API-003: Validation error: invalid email format

**Description:** Validation error: invalid email format

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandaugmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS559"
}
```

**Expected Output:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: email: Invalid email format",
    "timestamp": "2025-09-25T14:28:19.3259724",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "email: Invalid email format"
    ]
}
```

**Actual Result:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: email: Invalid email format",
    "timestamp": "2025-09-25T14:28:19.3259724",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "email: Invalid email format"
    ]
}
```

**Status:** PASS

## PAT-API-004: Validation error: invalid phone format

**Description:** Validation error: invalid phone format

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "02312",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS560"
}
```

**Expected Output:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: phone: Phone number must be 10 digits and start with 0",
    "timestamp": "2025-09-25T14:17:33.3400648",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "phone: Phone number must be 10 digits and start with 0"
    ]
}
```

**Actual Result:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: phone: Phone number must be 10 digits and start with 0",
    "timestamp": "2025-09-25T14:17:33.3400648",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "phone: Phone number must be 10 digits and start with 1"
    ]
}
```

**Status:** PASS

## PAT-API-005: Validation error: invalid gender enum

**Description:** Validation error: invalid gender enum

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEM",
    "phone": "0231222552",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS561"
}
```

**Expected Output:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: gender: Gender must be MALE, FEMALE, or OTHER",
    "timestamp": "2025-09-25T14:19:00.9122446",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "gender: Gender must be MALE, FEMALE, or OTHER"
    ]
}
```

**Actual Result:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: gender: Gender must be MALE, FEMALE, or OTHER",
    "timestamp": "2025-09-25T14:19:00.9122446",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "gender: Gender must be MALE, FEMALE, or OTHER"
    ]
}
```

**Status:** PASS

## PAT-API-006: Validation error: invalid date format

**Description:** Validation error: invalid date format

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "01-01-2002",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS562"
}
```

**Expected Output:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: dateOfBirth: Date of birth must be in format yyyy-MM-dd",
    "timestamp": "2025-09-25T14:21:27.9403003",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "dateOfBirth: Date of birth must be in format yyyy-MM-dd"
    ]
}
```

**Actual Result:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: dateOfBirth: Date of birth must be in format yyyy-MM-dd",
    "timestamp": "2025-09-25T14:21:27.9403003",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "dateOfBirth: Date of birth must be in format yyyy-MM-dd"
    ]
}
```

**Status:** PASS

## PAT-API-007: Conflict: duplicate email

**Description:** Conflict: duplicate email

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS563"
}
```

**Expected Output:**
```json
409 Conflict

{
    "status": 409,
    "message": "Patient with email already exists",
    "timestamp": "2025-09-25T14:22:13.7188258",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "Patient with email already exists"
    ]
}
```

**Actual Result:**
```json
409 Conflict

{
    "status": 409,
    "message": "Patient with email already exists",
    "timestamp": "2025-09-25T14:22:13.7188258",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "Patient with email already exists"
    ]
}
```

**Status:** PASS

## PAT-API-008: Conflict: duplicate phone

**Description:** Conflict: duplicate phone

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS564"
}
```

**Expected Output:**
```json
409 Conflict

{
    "status": 409,
    "message": "Patient with phone number already exists",
    "timestamp": "2025-09-25T14:23:11.5503399",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "Patient with phone number already exists"
    ]
}
```

**Actual Result:**
```json
409 Conflict

{
    "status": 409,
    "message": "Patient with phone number already exists",
    "timestamp": "2025-09-25T14:23:11.5503399",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "Patient with phone number already exists"
    ]
}
```

**Status:** PASS

## PAT-API-009: Validation error: missing createBy

**Description:** Validation error: missing createBy

**Input:**
```json
POST /api/v1/patients

{
    "fullName": "Nguyen Van Dau",
    "dateOfBirth": "1995-01-22",
    "gender": "FEMALE",
    "phone": "0231222552",
    "email": "nguyenvandau@gmail.com",
    "address": "4 Nguyen Du Boulevard, District 12, Ho Chi Minh City, Vietnam",
    "createdBy": "BS564"
}
```

**Expected Output:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: createdBy: Created by is required",
    "timestamp": "2025-09-25T14:24:47.9064156",
    "path": "/patient-service/api/v1/patients",
    "details": [
        "createdBy: Created by is required"
    ]
}
```

**Actual Result:**
```json
400 Bad Request

{
    "status": 400,
    "message": "Validation failed: createdBy: Created by is required",
    "timestamp": "2025-09-25T14:24:47.9064156",
    "path": "/patient-service/api/v2/patients",
    "details": [
        "createdBy: Created by is required"
    ]
}
```

**Status:** PASS