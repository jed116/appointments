package tech.itpark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.itpark.configuration.AppParams;
import tech.itpark.dto.*;
import tech.itpark.exception.AuthErrorException;
import tech.itpark.exception.BadRequestException;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.model.User;
import tech.itpark.repository.RolePermissionRepository;
import tech.itpark.security.Auth;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolePermissionService {
    private final RolePermissionRepository repository;

    private void checkAuthAdmin(Auth auth){
        long user_id = auth.getId();
        if (user_id <= 0){
            throw new AuthErrorException("USER NOT AUTHORIZED !!!");
        }

        User user = (User) auth;
        final var userRoles = user.getRoles();

        boolean isAdmin = AppParams.isAdmin(userRoles);
        if (!isAdmin){
            throw new PermissionDeniedException("ADMINS ONLY OPERATION !!!");
        }
    }

    public PermissionsFindResponseDto findPermissions(PermissionsFindRequestDto requestDto, Auth auth) {
        checkAuthAdmin(auth);

        return new PermissionsFindResponseDto(repository.findPermissions(requestDto.getOperations(), requestDto.getRoles()));

    }

    public PermissionsAppendRemoveResponseDto appendPermissions(PermissionsAppendRemoveRequestDto requestDto, Auth auth) {
        checkAuthAdmin(auth);

        final var operation = requestDto.getOperation();
        if (operation.isEmpty()){
            throw new BadRequestException("EMPTY OPERATION !!!");
        }
        final var rolesToAppend = requestDto.getRoles();
        if (rolesToAppend.size() == 0){
            throw new BadRequestException("EMPTY ROLES !!!");
        }
        final var unknownRolesToAppend = AppParams.filterUnknownRoles(rolesToAppend);
        if (unknownRolesToAppend.size() > 0){
            throw new BadRequestException("UNKNOWN ROLES: " + String.join(", ", unknownRolesToAppend) + " !!!");
        }

        final var operationRoles = repository.getOperationRoles(operation);
        final var assignedOperationRoles = rolesToAppend.stream().filter(r -> operationRoles.contains(r)).collect(Collectors.toSet());
        if (assignedOperationRoles.size() > 0){
            throw new BadRequestException("The operation '" + operation + "' has been already assigned the following roles: " + String.join(", ", assignedOperationRoles) + " !!!");
        }

        repository.appendPermissions(operation, rolesToAppend);

        return new PermissionsAppendRemoveResponseDto(operation, repository.getOperationRoles(operation));
    }

    public PermissionsAppendRemoveResponseDto removePermissions(PermissionsAppendRemoveRequestDto requestDto, Auth auth) {
        checkAuthAdmin(auth);

        final var operation = requestDto.getOperation();
        if (operation.isEmpty()){
            throw new BadRequestException("EMPTY OPERATION !!!");
        }
        final var rolesToRemove = requestDto.getRoles();
        if (rolesToRemove.size() == 0){
            throw new BadRequestException("EMPTY ROLES !!!");
        }
        final var unknownRolesToAppend = AppParams.filterUnknownRoles(rolesToRemove);
        if (unknownRolesToAppend.size() > 0){
            throw new BadRequestException("UNKNOWN ROLES: " + String.join(", ", unknownRolesToAppend) + " !!!");
        }

        final var operationRoles = repository.getOperationRoles(operation);
        final var notAssignedOperationRoles = rolesToRemove.stream().filter(r -> !operationRoles.contains(r)).collect(Collectors.toSet());
        if (notAssignedOperationRoles.size() > 0){
            throw new BadRequestException("The operation '" + operation + "' was not assigned the following roles: " + String.join(", ", notAssignedOperationRoles) + " !!!");
        }

        repository.removePermissions(operation, rolesToRemove);

        return new PermissionsAppendRemoveResponseDto(operation, repository.getOperationRoles(operation));
    }


    public RolesFindResponseDto findRoles(RolesFindRequestDto requestDto, Auth auth) {
        checkAuthAdmin(auth);

        return new RolesFindResponseDto(repository.findRoles(requestDto.getAttributes()));
    }

    public RolesAppendResponseDto appendRole(RolesAppendRequestDto requestDto, Auth auth) {
        checkAuthAdmin(auth);
        final var attributes = requestDto.getAttributes();
        if (attributes == null){
            throw new BadRequestException("Incorrect attributes !!!");
        }

        Boolean permissionsAttributes = false;

        if (!attributes.containsKey(AppParams.rolePropertyNAME())){
            throw new BadRequestException("The '" + AppParams.rolePropertyNAME() + "' property not set !!!");
        }
        if (!attributes.containsKey(AppParams.rolePropertyACTIVE())){
            throw new BadRequestException("The '" + AppParams.rolePropertyACTIVE() + "' property not set !!!");
        }
        if (!attributes.containsKey(AppParams.roleAttributeADMIN())){
            throw new BadRequestException("The '" + AppParams.roleAttributeADMIN() + "' attribute not set !!!");
        }else {
            permissionsAttributes = permissionsAttributes |  (Boolean) attributes.get(AppParams.roleAttributeADMIN());
        }

        if (!attributes.containsKey(AppParams.roleAttributeCHIEF())){
            throw new BadRequestException("The '" + AppParams.roleAttributeCHIEF() + "' attribute not set !!!");
        }else{
            permissionsAttributes = permissionsAttributes |  (Boolean) attributes.get(AppParams.roleAttributeCHIEF());
        }

        if (!attributes.containsKey(AppParams.roleAttributeDOCTOR())){
            throw new BadRequestException("The '" + AppParams.roleAttributeDOCTOR() + "' attribute not set !!!");
        }else{
            permissionsAttributes = permissionsAttributes |  (Boolean) attributes.get(AppParams.roleAttributeDOCTOR());
        }

        if (!attributes.containsKey(AppParams.roleAttributePATIENT())){
            throw new BadRequestException("The '" + AppParams.roleAttributePATIENT() + "' attribute not set !!!");
        }else{
            permissionsAttributes = permissionsAttributes |  (Boolean) attributes.get(AppParams.roleAttributePATIENT());
        }

        if (!permissionsAttributes){
            throw new BadRequestException("Incorrect attributes value !!!");
        }

        Map<String, ?> preparedAttributes = Map.ofEntries(
            new AbstractMap.SimpleEntry<String, String>(AppParams.rolePropertyNAME(),   (String) attributes.get(AppParams.rolePropertyNAME())),
            new AbstractMap.SimpleEntry<String, Boolean>(AppParams.rolePropertyACTIVE(),(Boolean) attributes.get(AppParams.rolePropertyACTIVE())),
            new AbstractMap.SimpleEntry<String, Boolean>(AppParams.roleAttributeADMIN(), (Boolean) attributes.get(AppParams.roleAttributeADMIN())),
            new AbstractMap.SimpleEntry<String, Boolean>(AppParams.roleAttributeCHIEF(), (Boolean) attributes.get(AppParams.roleAttributeCHIEF())),
            new AbstractMap.SimpleEntry<String, Boolean>(AppParams.roleAttributeDOCTOR(), (Boolean) attributes.get(AppParams.roleAttributeDOCTOR())),
            new AbstractMap.SimpleEntry<String, Boolean>(AppParams.roleAttributePATIENT(), (Boolean) attributes.get(AppParams.roleAttributePATIENT()))
                                                          );

        return new RolesAppendResponseDto(repository.appendRole(preparedAttributes));
    }

    public RolesRemoveResponseDto removeRole(RolesRemoveRequestDto requestDto, Auth auth) {
        checkAuthAdmin(auth);

        final var role = requestDto.getRole();
        final var id = repository.roleId(role);
        if( id == 0){
            throw new BadRequestException("Role '" + role + "' not exist !!!");
        }
        repository.removeRole(id);

        return new RolesRemoveResponseDto(id);
    }


    public Map<String,Map<String, Boolean>> initRoleAttributes() {
        return repository.initRoleAttributes();
    }

    public Map<String, Set<String>> initRolePermissions() {
        return repository.initRolePermissions();
    }

}
