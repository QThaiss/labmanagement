package fit.iam_service.dtos.response;

import fit.iam_service.enums.Gender;

import java.time.LocalDate;

public record UserListItem(
        String userId,
        String fullName,
        String email,
        String phone,
        String identifyNumber,
        Gender gender,
        Integer age,          // từ getAgeYears() hoặc tự tính
        String address,
        LocalDate dateOfBirth
) {
}
