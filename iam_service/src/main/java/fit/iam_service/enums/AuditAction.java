package fit.iam_service.enums;

public enum AuditAction {
    //User actions
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    CHANGE_PASSWORD,
    VIEW_USER,
    VERIFY_EMAIL,

    //Role actions
    CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE,
    VIEW_ROLE,

    LOGIN,
    REFRESH_TOKEN,
    FORGOT_PASSWORD,
    RESET_PASSWORD,
    LOGOUT
}
