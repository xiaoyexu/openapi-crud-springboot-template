package com.xuxiaoye.api.adapter.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.adapter.api.server.RolesApiDelegate;
import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.interfaces.RoleService;

@Log4j2
public class RoleAdapter implements RolesApiDelegate {

    private final CommonMapper commonMapper;
    private final RoleService roleService;

    public RoleAdapter(
            CommonMapper commonMapper,
            RoleService roleService
    ) {
        this.commonMapper = commonMapper;
        this.roleService = roleService;
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'role', 'create')")
    public ResponseEntity<CreateRoleResponse> createSingleRole(
            String authorization,
            Role createRoleRequest
    ) {
        return this.roleService.create(createRoleRequest)
                .toResponseEntity(
                        data -> CreateRoleResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> CreateRoleResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #roleId, 'role', 'delete') or @P.hasPermission(authentication, #roleId, 'role', 'delete_own')")
    public ResponseEntity<DeleteRoleResponse> deleteSingleRole(
            String authorization,
            String roleId
    ) {
        return this.roleService.deleteById(roleId)
                .toResponseEntity(
                        data -> DeleteRoleResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> DeleteRoleResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #roleId, 'role', 'get') or @P.hasPermission(authentication, #roleId, 'role', 'get_own')")
    public ResponseEntity<GetRoleResponse> getSingleRole(
            String authorization,
            String roleId
    ) {
        return this.roleService.get(roleId)
                .toResponseEntity(
                        data -> GetRoleResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> GetRoleResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'role', 'search')")
    public ResponseEntity<SearchRoleResponse> searchRoles(
            String authorization,
            SearchRoleRequest searchRoleRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.roleService.search(searchRoleRequest, Pagination.of(offset, limit, sortBy))
                .toResponseEntity(
                        data -> SearchRoleResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> SearchRoleResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #roleId, 'role', 'update') or @P.hasPermission(authentication, #roleId, 'role', 'update_own')")
    public ResponseEntity<UpdateRoleResponse> updateSingleRole(
            String authorization,
            String roleId,
            Role updateRoleRequest
    ) {
        return this.roleService.updateById(roleId, updateRoleRequest)
                .toResponseEntity(
                        data -> UpdateRoleResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> UpdateRoleResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'role', 'export')")
    public ResponseEntity<org.springframework.core.io.Resource> exportRoles(
            String authorization,
            SearchRoleRequest searchRoleRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        AppResponse<FileResponse> resourceAppResponse = this.roleService.exportData(searchRoleRequest, Pagination.of(offset, limit, sortBy), "Roles");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resourceAppResponse.getData().getContentType());
        headers.setContentDisposition(resourceAppResponse.getData().getContentDisposition());
        return new ResponseEntity<>(resourceAppResponse.getData().getResource(), headers, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'role', 'import')")
    public ResponseEntity<ImportRoleResponse> importRoles(
            String authorization,
            MultipartFile file
    ) {
        return this.roleService.importData(file)
                .toResponseEntity(
                        data -> ImportRoleResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> ImportRoleResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }
}