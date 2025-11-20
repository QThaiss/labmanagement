package fit.iam_service.services;

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.CreateRoleRequest;
import fit.iam_service.dtos.request.RoleListQuery;
import fit.iam_service.dtos.request.UpdateRoleRequest;
import fit.iam_service.dtos.response.CreateRoleResponse;
import fit.iam_service.dtos.response.DeleteRoleResponse;
import fit.iam_service.dtos.response.RoleListItem;
import fit.iam_service.dtos.response.UpdateRoleResponse;

public interface RoleService {
    CreateRoleResponse createRole(CreateRoleRequest request, String actorId, String clientIp, String userAgent);
    UpdateRoleResponse updateRole(String roleId, UpdateRoleRequest request,
                                  String actorId, String ip, String userAgent);
    DeleteRoleResponse deleteRole(String roleId);
    PageResult<RoleListItem> list(RoleListQuery query);

}
