/*
 * @ {#} Hl7ValidatorImpl.java   1.0     23/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.PID;
import fit.test_order_service.dtos.response.Hl7ValidationResult;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.Gender;
import fit.test_order_service.repositories.TestOrderRepository;
import fit.test_order_service.services.Hl7Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/*
 * @description: Implementation of the Hl7Validator interface.
 * @author: Tran Hien Vinh
 * @date:   23/10/2025
 * @version:    1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class Hl7ValidatorImpl implements Hl7Validator {
    private final TestOrderRepository testOrderRepository;

    private static final List<String> ALLOWED_VALUE_TYPES = Arrays.asList("NM", "ST", "TX", "CE", "DTM", "TM", "TS"); // Numeric, String, Text, Coded Element, Date/Time, Time, Time Stamp
    private static final List<String> ALLOWED_RESULT_STATUS = Arrays.asList("F", "C", "P", "R", "I", "X", "D"); // Final, Corrected, Preliminary, Revised, Incomplete, Results cannot be obtained, Deleted
    private static final List<String> FINAL_RESULT_STATUS = Arrays.asList("F", "C"); // Final, Corrected

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Hl7ValidationResult validateHl7Structure(ORU_R01 oruMessage, String orderId) {
        try {
            // 1. Validate MSH segment
            Hl7ValidationResult mshResult = validateMSH(oruMessage.getMSH());
            if (!mshResult.isValid()) {
                return mshResult;
            }

            // 2. Validate PID segment
            Hl7ValidationResult pidResult = validatePID(oruMessage.getPATIENT_RESULT().getPATIENT().getPID(), orderId);
            if (!pidResult.isValid()) {
                return pidResult;
            }

            // 3. Validate OBR and OBX segments
            int orderObservationReps = oruMessage.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
            for (int i = 0; i < orderObservationReps; i++) {
                ORU_R01_ORDER_OBSERVATION orderObs = oruMessage.getPATIENT_RESULT().getORDER_OBSERVATION(i);

                // Validate OBR
                Hl7ValidationResult obrResult = validateOBR(orderObs.getOBR());
                if (!obrResult.isValid()) {
                    return obrResult;
                }

                // Validate OBX segments
                int observationReps = orderObs.getOBSERVATIONReps();
                for (int j = 0; j < observationReps; j++) {
                    OBX obx = orderObs.getOBSERVATION(j).getOBX();
                    Hl7ValidationResult obxResult = validateOBX(obx);
                    if (!obxResult.isValid()) {
                        return obxResult;
                    }
                }
            }

            return Hl7ValidationResult.success();

        } catch (Exception e) {
            log.error("Error during HL7 validation: {}", e.getMessage());
            return Hl7ValidationResult.error("GENERAL", "HL7 validation error: " + e.getMessage());
        }
    }

    private Hl7ValidationResult validateMSH(MSH msh) throws HL7Exception {
        // MSH-10: Message Control ID (required)
        if (isEmpty(msh.getMessageControlID().getValue())) {
            return Hl7ValidationResult.error("MSH-10", "Message Control ID is required");
        }

        // MSH-9: Message Type (required)
        if (msh.getMessageType() == null ||
                isEmpty(msh.getMessageType().getMessageCode().getValue()) ||
                isEmpty(msh.getMessageType().getTriggerEvent().getValue())) {
            return Hl7ValidationResult.error("MSH-9", "Message Type is required");
        }

        // MSH-7: Date/Time (required)
        if (isEmpty(msh.getDateTimeOfMessage().getTime().getValue())) {
            return Hl7ValidationResult.error("MSH-7", "Message Date/Time is required");
        }

        // MSH-12: Version (required)
        if (isEmpty(msh.getVersionID().getVersionID().getValue())) {
            return Hl7ValidationResult.error("MSH-12", "HL7 Version is required");
        }

        return Hl7ValidationResult.success();
    }

    private Hl7ValidationResult validatePID(PID pid, String orderId) throws HL7Exception {
        // PID-3: Patient ID (required)
        if (pid.getPatientIdentifierList().length == 0 ||
                isEmpty(pid.getPatientIdentifierList()[0].getIDNumber().getValue())) {
            return Hl7ValidationResult.error("PID-3", "Patient ID is required");
        }

        // PID-5: Patient Name (required)
        if (pid.getPatientName().length == 0 ||
                isEmpty(pid.getPatientName()[0].getFamilyName().getSurname().getValue())) {
            return Hl7ValidationResult.error("PID-5", "Patient Name is required");
        }

        // PID-7: Date of Birth (required)
        if (isEmpty(pid.getDateTimeOfBirth().getTime().getValue())) {
            return Hl7ValidationResult.error("PID-7", "Patient Date of Birth is required");
        }

        // PID-8: Administrative Sex (required)
        if (isEmpty(pid.getAdministrativeSex().getValue())) {
            return Hl7ValidationResult.error("PID-8", "Patient Sex is required");
        }

        // Validate against existing TestOrder
        return validatePIDAgainstOrder(pid, orderId);
    }

    private Hl7ValidationResult validatePIDAgainstOrder(PID pid, String orderId) throws HL7Exception {
        try {
            TestOrder testOrder = testOrderRepository.findById(orderId).orElse(null);
            if (testOrder == null) {
                return Hl7ValidationResult.error("PID", "Test Order not found for ID: " + orderId);
            }

            // Validate Patient ID
            String hl7PatientId = pid.getPatientIdentifierList()[0].getIDNumber().getValue();
            if (!testOrder.getMedicalRecordId().equals(hl7PatientId)) {
                return Hl7ValidationResult.error("PID-3",
                        String.format("Patient ID mismatch: HL7=%s, Order=%s", hl7PatientId, testOrder.getMedicalRecordId()));
            }

            // Validate Date of Birth
            String hl7DobStr = pid.getDateTimeOfBirth().getTime().getValue();
            LocalDate hl7Dob = parseDateFromHl7(hl7DobStr);
            if (hl7Dob != null && !hl7Dob.equals(testOrder.getDateOfBirth())) {
                return Hl7ValidationResult.error("PID-7",
                        String.format("Date of Birth mismatch: HL7=%s, Order=%s", hl7Dob, testOrder.getDateOfBirth()));
            }

            // Validate Gender
            String hl7Gender = pid.getAdministrativeSex().getValue();
            Gender orderGender = testOrder.getGender();
            if (!isGenderMatch(hl7Gender, orderGender)) {
                return Hl7ValidationResult.error("PID-8",
                        String.format("Gender mismatch: HL7=%s, Order=%s", hl7Gender, orderGender));
            }

            return Hl7ValidationResult.success();

        } catch (Exception e) {
            return Hl7ValidationResult.error("PID", "Error validating PID against order: " + e.getMessage());
        }
    }

    private Hl7ValidationResult validateOBR(OBR obr) throws HL7Exception {
        // OBR-3: Test Order Number (required)
        if (isEmpty(obr.getFillerOrderNumber().getEntityIdentifier().getValue())) {
            return Hl7ValidationResult.error("OBR-3", "Test Order Number is required");
        }

        // OBR-4: Universal Service ID (required)
        if (obr.getUniversalServiceIdentifier() == null ||
                isEmpty(obr.getUniversalServiceIdentifier().getIdentifier().getValue())) {
            return Hl7ValidationResult.error("OBR-4", "Panel code/name is required");
        }

        // OBR-7: Observation time (required)
        boolean hasObservationTime = !isEmpty(obr.getObservationDateTime().getTime().getValue());

        if (!hasObservationTime) {
            return Hl7ValidationResult.error("OBR-7", "Observation time is required");
        }

        return Hl7ValidationResult.success();
    }

    private Hl7ValidationResult validateOBX(OBX obx) throws HL7Exception {
        // OBX-2: Value Type (required)
        String valueType = obx.getValueType().getValue();
        if (isEmpty(valueType)) {
            return Hl7ValidationResult.error("OBX-2", "Value Type is required");
        }

        if (!ALLOWED_VALUE_TYPES.contains(valueType)) {
            return Hl7ValidationResult.error("OBX-2",
                    "Invalid Value Type: " + valueType + ". Allowed: " + ALLOWED_VALUE_TYPES);
        }

        // OBX-3: Observation Identifier (required)
        CE observationId = obx.getObservationIdentifier();
        if (observationId == null ||
                isEmpty(observationId.getIdentifier().getValue()) ||
                isEmpty(observationId.getText().getValue())) {
            return Hl7ValidationResult.error("OBX-3", "Test Item Code and Name are required");
        }

        // OBX-5: Observation Value (required)
        if (obx.getObservationValue().length == 0 || obx.getObservationValue()[0] == null) {
            return Hl7ValidationResult.error("OBX-5", "Observation Value is required");
        }

        // Validate value type compatibility
        Hl7ValidationResult valueCompatibility = validateValueTypeCompatibility(valueType, obx);
        if (!valueCompatibility.isValid()) {
            return valueCompatibility;
        }

        // OBX-11: Observation Result Status (required)
        String resultStatus = obx.getObservationResultStatus().getValue();
        if (isEmpty(resultStatus)) {
            return Hl7ValidationResult.error("OBX-11", "Result Status is required");
        }

        if (!ALLOWED_RESULT_STATUS.contains(resultStatus)) {
            return Hl7ValidationResult.error("OBX-11",
                    "Invalid Result Status: " + resultStatus + ". Allowed: " + ALLOWED_RESULT_STATUS);
        }

        return Hl7ValidationResult.success();
    }

    private Hl7ValidationResult validateValueTypeCompatibility(String valueType, OBX obx) throws HL7Exception {
        String value = obx.getObservationValue()[0].getData().toString();

        switch (valueType) {
            case "NM": // Numeric
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return Hl7ValidationResult.error("OBX-5",
                            "Value Type NM requires numeric value, got: " + value);
                }
                break;
            case "ST": // String
            case "TX": // Text
                if (isEmpty(value)) {
                    return Hl7ValidationResult.error("OBX-5",
                            "Value Type " + valueType + " requires non-empty text value");
                }
                break;
            case "CE": // Coded Element
                // For CE, value should have code structure
                if (isEmpty(value) || (!value.contains("^") && value.length() < 2)) {
                    return Hl7ValidationResult.error("OBX-5",
                            "Value Type CE requires coded value format");
                }
                break;
            case "DTM": // Date/Time
            case "TM":  // Time
            case "TS":  // Time Stamp
                if (isEmpty(value) || value.length() < 8) {
                    return Hl7ValidationResult.error("OBX-5",
                            "Value Type " + valueType + " requires valid date/time format");
                }
                break;
        }

        return Hl7ValidationResult.success();
    }

    @Override
    public boolean isFinalResult(String resultStatus) {
        return FINAL_RESULT_STATUS.contains(resultStatus);
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private LocalDate parseDateFromHl7(String hl7Date) {
        if (isEmpty(hl7Date)) return null;

        try {
            if (hl7Date.length() >= 8) {
                String dateStr = hl7Date.substring(0, 8);
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse HL7 date: {}", hl7Date);
        }
        return null;
    }

    private boolean isGenderMatch(String hl7Gender, Gender orderGender) {
        if (isEmpty(hl7Gender) || orderGender == null) return false;

        return switch (hl7Gender.toUpperCase()) {
            case "M" -> orderGender == Gender.MALE;
            case "F" -> orderGender == Gender.FEMALE;
            case "O" -> orderGender == Gender.OTHER;
            default -> false;
        };
    }
}
