/*
 * @ {#} SortFields.java   1.0     27/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.constants;

import lombok.experimental.UtilityClass;

import java.util.Set;

/*
 * @description: Utility class defining valid sort fields
 * @author: Tran Hien Vinh
 * @date:   27/09/2025
 * @version:    1.0
 */
@UtilityClass
public class SortFields {
    public static final Set<String> PATIENT_MEDICAL_RECORD_SORT_FIELDS =
            Set.of("createdAt", "fullName", "dateOfBirth", "lastTestDate");

    public static final String DEFAULT_PATIENT_MEDICAL_RECORD_SORT = "medicalRecordId";
}
