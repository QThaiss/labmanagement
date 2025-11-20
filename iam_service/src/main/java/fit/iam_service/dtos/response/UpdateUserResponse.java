package fit.iam_service.dtos.response;

import fit.iam_service.enums.Gender;

import java.time.LocalDate;

public record UpdateUserResponse(
        String userId,
        String fullName,
        LocalDate dateOfBirth,
        Integer ageYears,
        Gender gender,
        String address,
        String email,
        String phone
) {
}
