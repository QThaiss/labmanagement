/*
 * @ (#) UserServiceImpl.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.AuditLog;
import fit.iam_service.entities.PasswordHistory;
import fit.iam_service.entities.Role;
import fit.iam_service.entities.User;
import fit.iam_service.enums.AuditAction;
import fit.iam_service.enums.Gender;
import fit.iam_service.exceptions.UnauthorizedException;
import fit.iam_service.repositories.*;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.services.OtpService;
import fit.iam_service.services.UserService;
import fit.iam_service.utils.RsaDecryptUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordHistoryRepository pwdHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionRepository authSessionRepository;
    private final OtpService otpService;
    private final RsaDecryptUtils rsaUtils;

    private static LocalDate parse(String dobStr) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, f);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date of birth must be in MM/DD/YYYY format");
        }
        return dob;
    }

    private static String normalizeRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return "ROLE_USER";
        return roleCode.trim().toUpperCase();
    }

    private static String safeTrim(String s) {
        return (s == null) ? null : s.trim();
    }

    /**
     * Tạo detailsJson dạng {"after": {"userId": "...", "email": "...", "phone": "..."}}
     * (Bạn có thể thay thế bằng ObjectMapper nếu thích.)
     */
    private static String buildAfterJson(User u) {
        String uid = u.getUserId() == null ? "" : u.getUserId();
        String email = u.getEmail() == null ? "" : u.getEmail();
        String phone = u.getPhone() == null ? "" : u.getPhone();
        return "{\"after\":{\"userId\":\"" + uid + "\",\"email\":\"" + email + "\",\"phone\":\"" + phone + "\"}}";
    }

    private boolean computedAgeEquals(Integer provided, int computed) {
        return provided != null && provided == computed;
    }

    private static String j(String s) {
        // escape rất gọn cho JSON string
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Tạo JSON before/after cho các field quan trọng (có thể mở rộng sau)
     */
    private static String buildDiffJson(
            String beforeFullName, String beforeEmail, String beforePhone, String beforeAddress,
            LocalDate beforeDob, Gender beforeGender,
            User after // dùng user sau update để lấy "after"
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');

        sb.append("\"before\":{")
                .append("\"fullName\":\"").append(j(beforeFullName)).append("\",")
                .append("\"email\":\"").append(j(beforeEmail)).append("\",")
                .append("\"phone\":\"").append(j(beforePhone)).append("\",")
                .append("\"address\":\"").append(j(beforeAddress)).append("\",")
                .append("\"dateOfBirth\":\"").append(beforeDob == null ? "" : beforeDob).append("\",")
                .append("\"gender\":\"").append(beforeGender == null ? "" : beforeGender.name()).append("\"")
                .append("},");

        sb.append("\"after\":{")
                .append("\"userId\":\"").append(j(after.getUserId())).append("\",")
                .append("\"fullName\":\"").append(j(after.getFullName())).append("\",")
                .append("\"email\":\"").append(j(after.getEmail())).append("\",")
                .append("\"phone\":\"").append(j(after.getPhone())).append("\",")
                .append("\"address\":\"").append(j(after.getAddress())).append("\",")
                .append("\"dateOfBirth\":\"").append(after.getDateOfBirth() == null ? "" : after.getDateOfBirth()).append("\",")
                .append("\"gender\":\"").append(after.getGender() == null ? "" : after.getGender().name()).append("\"")
                .append("}");

        sb.append('}');
        return sb.toString();
    }

    @Override
    @Transactional
    public CreateUserResponse create(CreateUserRequest req, String clientIp, String userAgent) {
        //0)Resolve actor and privileges (optional auth)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAuth = auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()));
        boolean isAdmin = hasAuth
                && auth.getAuthorities() != null
                && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

        String actorId = null;
        if (hasAuth && auth.getPrincipal() instanceof UserDetailsImpl) {
            actorId = ((UserDetailsImpl) auth.getPrincipal()).getId();
        } else if (hasAuth) {
            actorId = auth.getName(); // fallback if principal is not UserDetailsImpl
        }

        // Enforce role: only admin can choose; public sign-up always ROLE_USER
        String effectiveRoleCode = isAdmin ? normalizeRole(req.roleCode()) : "ROLE_USER";

        // 1) Uniqueness pre-checks
        if (userRepository.existsByEmail(req.email().trim().toLowerCase()))
            throw new IllegalArgumentException("Email already in use");
        if (userRepository.existsByPhone(req.phone().trim()))
            throw new IllegalArgumentException("Phone already in use");
        if (userRepository.existsByIdentifyNumber(req.identifyNumber().trim()))
            throw new IllegalArgumentException("Identify number already in use");
        if (req.username() != null && !req.username().isBlank()
                && userRepository.existsByUsername(req.username().trim()))
            throw new IllegalArgumentException("Username already in use");

        // 2) Cross-field validation – Age vs DoB (UTC)
        LocalDate dob = parse(req.dateOfBirth());
        int computedAge = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();
        if (req.age() == null || req.age() != computedAge)
            throw new IllegalArgumentException("Age does not match date of birth");

        // 3) Resolve role
        Role role = roleRepository.findByRoleCode(effectiveRoleCode)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + effectiveRoleCode));

        String rawPassword = rsaUtils.decrypt(req.password());
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Regex check
        String regex = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}$";
        if (!rawPassword.matches(regex)) {
            throw new IllegalArgumentException("Password must contain letters and digits, and be 8–128 characters long");
        }

        // 4) Build user
        User u = new User();
        u.setUsername(safeTrim(req.username()));
        u.setEmail(req.email().trim().toLowerCase());
        u.setPhone(req.phone().trim());
        u.setFullName(req.fullName().trim());
        u.setIdentifyNumber(req.identifyNumber().trim());
        u.setGender(Gender.valueOf(req.gender().trim().toUpperCase()));
        u.setAddress(req.address().trim());
        u.setDateOfBirth(dob);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setCreatedBy(actorId);

        userRepository.saveAndFlush(u);

        // 5) Password history
        PasswordHistory ph = PasswordHistory.builder()
                .user(u)
                .oldPasswordHash(u.getPasswordHash())
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .changedBy(actorId)
                .build();
        pwdHistoryRepository.save(ph);

        // NEW: gửi OTP xác minh email (5 phút)
        otpService.createAndSendEmailVerifyOtp(u);

        // 6) Audit log
        AuditLog log = AuditLog.builder()
                .actorId(actorId)                // null for public sign-up; userId for admin
                .targetId(u.getUserId())
                .entity("USER")
                .action(AuditAction.CREATE_USER)
                .detailsJson(buildAfterJson(u))
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);

        // 7) Response
        return new CreateUserResponse(
                u.getUserId(),
                u.getEmail(),
                u.getPhone(),
                u.getUsername(),
                u.getFullName(),
                u.getRole().getRoleCode()
        );

    }

    @Override
    @Transactional
    public EmailVerifyResponse verifyEmail(EmailVerifyRequest req, String clientIp, String userAgent) {
        String userId = req.userId().trim();
        String rawOtp = req.otp().trim();

        User user = userRepository.findActiveById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại hoặc đã bị xóa: " + userId));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            // đã xác minh rồi thì không cần verify lại
            return new EmailVerifyResponse(user.getUserId(), true, user.getUpdatedAt());
        }

        boolean ok = otpService.verifyEmailOtp(user, rawOtp);
        if (!ok) {
            throw new IllegalArgumentException("OTP không chính xác");
        }

        // cập nhật trạng thái xác minh email
        user.setEmailVerified(true);
        user.setUpdatedBy(user.getUserId()); // hoặc actorId nếu bạn muốn theo dõi
        userRepository.saveAndFlush(user);

        // audit
        AuditLog log = AuditLog.builder()
                .actorId(user.getUserId())      // người xác minh là chính chủ
                .targetId(user.getUserId())
                .entity("USER")
                .action(AuditAction.VERIFY_EMAIL)
                .detailsJson("{\"event\":\"VERIFY_EMAIL\",\"result\":\"SUCCESS\"}")
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);

        return new EmailVerifyResponse(user.getUserId(), true, user.getUpdatedAt());
    }

    @Override
    @Transactional
    public CreateUserResponse createByAdmin(AdminCreateUserRequest req, String clientIp, String userAgent) {

        // 0) Resolve actor and ensure ADMIN
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean hasAuth = auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equals(String.valueOf(auth.getPrincipal()));

        if (!hasAuth) {
            throw new UnauthorizedException("Only admin can create user");
        }

        boolean isAdmin = auth.getAuthorities() != null
                && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));

        if (!isAdmin) {
            throw new UnauthorizedException("Only users with ROLE_ADMIN can create new users");
        }

        String actorId = null;
        if (auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            actorId = userDetails.getId();
        } else {
            actorId = auth.getName();
        }

        // 1) Uniqueness validations
        if (userRepository.existsByEmail(req.email().trim().toLowerCase()))
            throw new IllegalArgumentException("Email already in use");

        if (userRepository.existsByPhone(req.phone().trim()))
            throw new IllegalArgumentException("Phone already in use");

        if (userRepository.existsByIdentifyNumber(req.identifyNumber().trim()))
            throw new IllegalArgumentException("Identify number already in use");

        // 2) Cross-check Age vs DoB
        LocalDate dob = parse(req.dateOfBirth());
        int computedAge = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();
        if (!computedAgeEquals(req.age(), computedAge)) {
            throw new IllegalArgumentException("Age does not match date of birth");
        }

        // 3) Resolve Role for admin-created user
        String normalizedRole = normalizeRole(req.roleCode());
        Role role = roleRepository.findByRoleCode(normalizedRole)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRole));

        // 4) Decrypt & validate password
        String rawPassword = "P@ssword123";

        // 5) Build new User
        User u = new User();
        u.setUsername(generateUsername(req.fullName(), req.email()));
        u.setEmail(req.email().trim().toLowerCase());
        u.setPhone(req.phone().trim());
        u.setFullName(req.fullName().trim());
        u.setIdentifyNumber(req.identifyNumber().trim());
        u.setGender(Gender.valueOf(req.gender().trim().toUpperCase()));
        u.setAddress(req.address().trim());
        u.setDateOfBirth(dob);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setCreatedBy(actorId);

        userRepository.saveAndFlush(u);

        // 6) Save password history
        PasswordHistory ph = PasswordHistory.builder()
                .user(u)
                .oldPasswordHash(u.getPasswordHash())
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .changedBy(actorId)
                .build();
        pwdHistoryRepository.save(ph);

        // 7) Audit Log
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .targetId(u.getUserId())
                .entity("USER")
                .action(AuditAction.CREATE_USER)
                .detailsJson(buildAfterJson(u))
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);

        // 8) Response
        return new CreateUserResponse(
                u.getUserId(),
                u.getEmail(),
                u.getPhone(),
                u.getUsername(),
                u.getFullName(),
                u.getRole().getRoleCode()
        );
    }

    private String generateUsername(String fullName, String email) {

        String rawBase;

        if (fullName != null && !fullName.isBlank()) {
            rawBase = removeAccent(fullName).toLowerCase(); // <--- FIX
        } else {
            rawBase = email.substring(0, email.indexOf("@")).toLowerCase();
        }

        String base = rawBase.replaceAll("[^a-z0-9]", "");

        if (base.isBlank()) {
            base = "user";
        }

        String username = base + String.format("%04d", new Random().nextInt(10000));

        while (userRepository.existsByUsername(username)) {
            username = base + String.format("%04d", new Random().nextInt(10000));
        }

        return username;
    }

    public static String removeAccent(String s) {
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}", "");
    }

    @Override
    @Transactional
    public UpdateUserResponse update(User authUser, String userId, UpdateUserRequest req, String clientIp, String userAgent) {
        // actor
        String actorId = (authUser != null ? authUser.getUserId() : null);

        // allow if ADMIN
        boolean isAdmin = authUser != null
                && authUser.getRole() != null
                && "ROLE_ADMIN".equalsIgnoreCase(authUser.getRole().getRoleCode());

        // or owner (chính chủ)
        boolean isOwner = authUser != null
                && authUser.getUserId() != null
                && authUser.getUserId().equals(userId);

        // Chỉ cho phép ADMIN hoặc chính chủ
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to update this user");
        }

        // 1) Load user
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 2) Parse & validate dateOfBirth (MM/dd/yyyy)
        LocalDate dob = parse(req.dateOfBirth());
        // 3) So khớp tuổi theo UTC
        int computedAge = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();
        if (!computedAgeEquals(req.age(), computedAge)) {
            throw new IllegalArgumentException("Age does not match date of birth");
        }

        // 4) Gender: male/female -> enum
        Gender gender = "male".equalsIgnoreCase(req.gender()) ? Gender.MALE : Gender.FEMALE;

        // 5) Chuẩn hóa email/phone
        String email = req.email().trim().toLowerCase();
        String phone = req.phone().trim();

        // 6) Check unique (trừ chính mình)
        if (userRepository.existsByEmailAndUserIdIsNot(email, userId)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByPhoneAndUserIdIsNot(phone, userId)) {
            throw new IllegalArgumentException("Phone already in use");
        }
        if (userRepository.existsByIdentifyNumberAndUserIdIsNot(u.getIdentifyNumber(), userId)) {
            throw new IllegalArgumentException("Identify number already in use");
        }

        // 7) Snapshot BEFORE for audit diff
        String beforeFullName = u.getFullName();
        String beforeEmail = u.getEmail();
        String beforePhone = u.getPhone();
        String beforeAddress = u.getAddress();
        LocalDate beforeDob = u.getDateOfBirth();
        Gender beforeGender = u.getGender();

        // 8) Gán dữ liệu
        u.setFullName(req.fullName().trim());
        u.setDateOfBirth(dob);
        u.setGender(gender);
        u.setAddress(req.address().trim());
        u.setEmail(email);
        u.setPhone(phone);
        u.setUpdatedBy(actorId);

        userRepository.saveAndFlush(u);

        // 9) Audit log
        String details = buildDiffJson(
                beforeFullName, beforeEmail, beforePhone, beforeAddress, beforeDob, beforeGender, u
        );

        AuditLog log = AuditLog.builder()
                .actorId(actorId)                // người thực hiện (admin hoặc chính user)
                .targetId(u.getUserId())         // đối tượng bị tác động
                .entity("USER")
                .action(AuditAction.UPDATE_USER) // enum của bạn
                .detailsJson(details)            // JSON diff before/after
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);

        // 10) Response
        return new UpdateUserResponse(
                u.getUserId(),
                u.getFullName(),
                u.getDateOfBirth(),
                u.getAgeYears(),
                u.getGender(),
                u.getAddress(),
                u.getEmail(),
                u.getPhone()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserListItem> list(UserListQuery q) {
        // defaults
        int page = (q.page() == null || q.page() < 0) ? 0 : q.page();
        int size = (q.size() == null || q.size() <= 0) ? 20 : Math.min(q.size(), 100);
        String sortBy = (q.sortBy() == null || q.sortBy().isBlank()) ? "fullName" : q.sortBy();
        Sort.Direction dir = "desc".equalsIgnoreCase(q.sortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Chỉ cho phép sort theo whitelist để tránh sort injection
        sortBy = switch (sortBy) {
            case "email", "phone", "identifyNumber", "gender", "dateOfBirth", "fullName" -> sortBy;
            default -> "fullName";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        Specification<User> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // keyword: fullName/email/phone/identifyNumber
            if (q.q() != null && !q.q().isBlank()) {
                String kw = "%" + q.q().trim().toLowerCase() + "%";
                ps.add(
                        cb.or(
                                cb.like(cb.lower(root.get("fullName")), kw),
                                cb.like(cb.lower(root.get("email")), kw),
                                cb.like(cb.lower(root.get("phone")), kw),
                                cb.like(cb.lower(root.get("identifyNumber")), kw)
                        )
                );
            }

            // gender
            if (q.gender() != null && !q.gender().isBlank()) {
                String g = q.gender().trim().toUpperCase();
                // chỉ nhận MALE/FEMALE
                if ("MALE".equals(g) || "FEMALE".equals(g)) {
                    ps.add(cb.equal(root.get("gender"), Gender.valueOf(g)));
                }
            }

            // dob range
            if (q.dobFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("dateOfBirth"), q.dobFrom()));
            }
            if (q.dobTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("dateOfBirth"), q.dobTo()));
            }

            // age range -> quy đổi sang dob range theo UTC
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            if (q.minAge() != null) {
                // tuổi >= minAge  => dob <= today - minAge years
                LocalDate dobMax = today.minusYears(q.minAge());
                ps.add(cb.lessThanOrEqualTo(root.get("dateOfBirth"), dobMax));
            }
            if (q.maxAge() != null) {
                // tuổi <= maxAge  => dob >= today - (maxAge+1) years + 1 day ~ today.minusYears(maxAge).plusDays(1) an toàn hơn
                LocalDate dobMin = today.minusYears(q.maxAge() + 1L).plusDays(1);
                ps.add(cb.greaterThanOrEqualTo(root.get("dateOfBirth"), dobMin));
            }

            ps.add(cb.isFalse(root.get("isDeleted")));

            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<User> pageData = userRepository.findAll(spec, pageable);

        Page<UserListItem> mapped = pageData.map(u -> new UserListItem(
                u.getUserId(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getIdentifyNumber(),
                u.getGender(),
                u.getAgeYears(),          // nếu không có @Formula, có thể tính Period.between(u.getDateOfBirth(), today).getYears()
                u.getAddress(),
                u.getDateOfBirth()
        ));

        return PageResult.of(mapped);
    }

    @Override
    @Transactional
    public DeleteUserResult deleteUser(User authUser, String targetUserId) {
        // actor
        String actorId = (authUser != null ? authUser.getUserId() : null);
        if (actorId == null) {
            throw new AccessDeniedException("Bạn phải đăng nhập để xóa tài khoản.");
        }

        if (targetUserId.equals(actorId)) {
            throw new AccessDeniedException("Không thể tự xóa tài khoản của chính mình.");
        }

        // Lấy snapshot trước xóa (để ghi audit)
        User before = userRepository.findActiveById(targetUserId)
                .orElse(null);

        // Nếu before == null, vẫn cần phân biệt: không tồn tại hay đã xóa
        if (before == null) {
            Boolean deletedFlag = userRepository.isDeletedFlag(targetUserId); // null = không tồn tại; true = đã xóa
            if (deletedFlag == null) {
                throw new EntityNotFoundException("Người dùng không tồn tại: " + targetUserId);
            } else if (Boolean.TRUE.equals(deletedFlag)) {
                throw new IllegalStateException("Tài khoản đã bị xóa từ trước: " + targetUserId);
            } else {
                // Rất hiếm khi rơi vào đây (vì findActiveById đã lọc isDeleted=false)
                throw new EntityNotFoundException("Không thể tải thông tin người dùng: " + targetUserId);
            }
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int updated = userRepository.softDelete(targetUserId, actorId, now);
        if (updated == 0) {

            Boolean deletedFlag = userRepository.isDeletedFlag(targetUserId);
            if (Boolean.TRUE.equals(deletedFlag)) {
                throw new IllegalStateException("Tài khoản đã bị xóa từ trước: " + targetUserId);
            }
            throw new EntityNotFoundException("Người dùng không tồn tại: " + targetUserId);
        }

        authSessionRepository.deleteAllByUserId(targetUserId);
        // Ghi audit
        writeDeleteAudit(actorId, before, now);

        return DeleteUserResult.builder()
                .userId(targetUserId)
                .deletedBy(actorId)
                .deletedAt(now)
                .build();
    }

    private void writeDeleteAudit(String actorId, User before, LocalDateTime when) {
        try {
            Map<String, Object> details = new HashMap<>();
            Map<String, Object> beforeJson = new HashMap<>();
            beforeJson.put("userId", before.getUserId());
            beforeJson.put("email", before.getEmail());
            beforeJson.put("username", before.getUsername());
            beforeJson.put("fullName", before.getFullName());
            beforeJson.put("roleId", (before.getRole() != null ? before.getRole().getRoleId() : null));

            details.put("before", beforeJson);
            details.put("after", Map.of("status", "DELETED", "deletedAt", when.toString(), "deletedBy", actorId));

            String detailsJson = buildDeleteDetailsJson(before, actorId, when);

            AuditLog log = AuditLog.builder()
                    .action(AuditAction.DELETE_USER)
                    .entity("USER")
                    .actorId(actorId)
                    .targetId(before.getUserId())
                    .detailsJson(detailsJson)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignore) {
            // không chặn flow xóa nếu audit ghi lỗi; có thể log cảnh báo
        }
    }

    private static String buildDeleteDetailsJson(User before, String actorId, LocalDateTime when) {
        String a = (actorId == null) ? "" : actorId;
        String roleId = (before.getRole() == null || before.getRole().getRoleId() == null)
                ? "" : before.getRole().getRoleId();

        StringBuilder sb = new StringBuilder(256);
        sb.append('{');

        sb.append("\"before\":{")
                .append("\"userId\":\"").append(j(before.getUserId())).append("\",")
                .append("\"email\":\"").append(j(before.getEmail())).append("\",")
                .append("\"username\":\"").append(j(before.getUsername())).append("\",")
                .append("\"fullName\":\"").append(j(before.getFullName())).append("\",")
                .append("\"roleId\":\"").append(j(roleId)).append("\"")
                .append("},");

        sb.append("\"after\":{")
                .append("\"status\":\"DELETED\",")
                .append("\"deletedAt\":\"").append(when).append("\",")
                .append("\"deletedBy\":\"").append(j(a)).append("\"")
                .append("}");

        sb.append('}');
        return sb.toString();
    }

    // --- Helpers for change password and view detail  ---
    private static void ensurePasswordPolicy(String raw) {
        if (raw == null) throw new IllegalArgumentException("New password is required");
        if (!raw.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,128}$")) {
            throw new IllegalArgumentException("New password must be 8-128 chars and contain letters and digits");
        }
    }

    private static String buildChangePwdDetailsJson(String actorId, String targetId, String mode, String result) {
        // Không log password; chỉ log metadata
        // {"event":"CHANGE_PASSWORD","actorId":"...","targetId":"...","mode":"SELF|ADMIN","result":"SUCCESS"}
        return "{\"event\":\"CHANGE_PASSWORD\",\"actorId\":\"" + j(actorId) +
                "\",\"targetId\":\"" + j(targetId) +
                "\",\"mode\":\"" + j(mode) +
                "\",\"result\":\"" + j(result) + "\"}";
    }

    private static String buildViewDetailsJson(String actorId, String targetId, String mode) {
        // Không log dữ liệu nhạy cảm – chỉ log metadata thao tác xem
        // {"event":"VIEW_DETAIL","viewerId":"...","targetId":"...","mode":"SELF|ADMIN|MANAGER"}
        String a = j(actorId);
        String t = j(targetId);
        String m = j(mode);
        return "{\"event\":\"VIEW_DETAIL\",\"viewerId\":\"" + a + "\",\"targetId\":\"" + t + "\",\"mode\":\"" + m + "\"}";
    }

    @Override
    @Transactional
    public ChangePasswordResponse changePassword(
            User authUser, String targetUserId, ChangePasswordRequest req, String clientIp, String userAgent) {

        // 1) Quyền: ADMIN hoặc chính chủ
        if (authUser == null) throw new AccessDeniedException("You must be logged in");
        String actorId = authUser.getUserId();
        String roleCode = (authUser.getRole() != null ? authUser.getRole().getRoleCode() : null);
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(roleCode);
        boolean isOwner = actorId != null && actorId.equals(targetUserId);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to change this user's password");
        }

        // 2) Tải user mục tiêu (chưa bị xóa)
        User u = userRepository.findActiveById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found or deleted: " + targetUserId));

        // 3) Kiểm tra newPassword khớp confirm
        if (!req.newPassword().equals(req.confirmNewPassword())) {
            throw new IllegalArgumentException("Confirm password does not match");
        }

        // 4) Chính chủ: phải nhập currentPassword đúng
        if (isOwner && (req.currentPassword() == null || req.currentPassword().isBlank())) {
            throw new AccessDeniedException("Current password is required");
        }
        if (isOwner && !passwordEncoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        // 5) Kiểm tra policy & không trùng mật khẩu cũ
        ensurePasswordPolicy(req.newPassword());
        if (passwordEncoder.matches(req.newPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the old password");
        }

        // 6) Ghi PasswordHistory (lưu hash cũ)
        PasswordHistory ph = PasswordHistory.builder()
                .user(u)
                .oldPasswordHash(u.getPasswordHash())
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .changedBy(actorId)
                .build();
        pwdHistoryRepository.save(ph);

        // 7) Cập nhật mật khẩu
        String newHash = passwordEncoder.encode(req.newPassword());
        u.setPasswordHash(newHash);
        u.setPasswordChangedAt(LocalDateTime.now(ZoneOffset.UTC));
        u.setUpdatedBy(actorId);
        userRepository.saveAndFlush(u);

        // 8) Revoke toàn bộ phiên / token đang hoạt động của user target
        authSessionRepository.deleteAllByUserId(u.getUserId());

        // 9) Audit
        String mode = isOwner ? "SELF" : "ADMIN";
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .targetId(u.getUserId())
                .entity("USER")
                .action(AuditAction.CHANGE_PASSWORD)
                .detailsJson(buildChangePwdDetailsJson(actorId, u.getUserId(), mode, "SUCCESS"))
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);

        return new ChangePasswordResponse(u.getUserId(), u.getPasswordChangedAt());
    }

    @Override
    @Transactional
    public UserDetailResponse viewDetail(User authUser, String targetUserId, String clientIp, String userAgent) {
        // 1) Tải user đích (chỉ user chưa xóa)
        User target = userRepository.findActiveByIdFetchRole(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại hoặc đã bị xóa: " + targetUserId));

        // 2) Quyền: ADMIN hoặc MANAGER hoặc CHÍNH CHỦ
        String actorId = (authUser != null ? authUser.getUserId() : null);
        String roleCode = (authUser != null && authUser.getRole() != null) ? authUser.getRole().getRoleCode() : null;

        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(roleCode);
        boolean isManager = "ROLE_MANAGER".equalsIgnoreCase(roleCode);
        boolean isOwner = (authUser != null && target.getUserId().equals(authUser.getUserId()));

        if (!isAdmin && !isManager && !isOwner) {
            throw new AccessDeniedException("You are not allowed to view this user");
        }

        // 3) Map sang DTO
        Integer ageYears = null;
        if (target.getDateOfBirth() != null) {
            ageYears = Period.between(target.getDateOfBirth(), LocalDate.now(ZoneOffset.UTC)).getYears();
        }

        String identifyMasked = null;
        if (target.getIdentifyNumber() != null && !target.getIdentifyNumber().isBlank()) {
            String idn = target.getIdentifyNumber();
            int keep = Math.min(3, idn.length());
            identifyMasked = "*".repeat(Math.max(0, idn.length() - keep)) + idn.substring(idn.length() - keep);
        }

        Role role = target.getRole();
        UserDetailResponse resp = UserDetailResponse.builder()
                .userId(target.getUserId())
                .username(target.getUsername())
                .fullName(target.getFullName())
                .email(target.getEmail())
                .phone(target.getPhone())
                .address(target.getAddress())
                .dateOfBirth(target.getDateOfBirth())
                .ageYears(ageYears)
                .gender(target.getGender())
                .identifyNumberMasked(identifyMasked)
                .roleId(role != null ? role.getRoleId() : null)
                .roleCode(role != null ? role.getRoleCode() : null)
                .roleName(role != null ? role.getRoleName() : null)
                .emailVerified(target.getEmailVerified())
                .createdAt(target.getCreatedAt())
                .updatedAt(target.getUpdatedAt())
                .deletedAt(target.getDeletedAt())
                .build();

        // 4) Audit VIEW
        String mode = isOwner ? "SELF" : (isAdmin ? "ADMIN" : "MANAGER");
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .targetId(target.getUserId())
                .entity("USER")
                .action(AuditAction.VIEW_USER)
                .detailsJson(buildViewDetailsJson(actorId, target.getUserId(), mode))
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);

        return resp;
    }
}