package tech.itpark.configuration;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AppParams {
    private static String ROLE_ANONYMOUS;

    private static String ROLE_ATTRIBUTE_PATIENT;
    private static String ROLE_ATTRIBUTE_DOCTOR;
    private static String ROLE_ATTRIBUTE_CHIEF;
    private static String ROLE_ATTRIBUTE_ADMIN;

    private static  Map<String,Map<String, Boolean>> roleAttributes;

    private static String ROLE_PROPERTY_ACTIVE;
    private static String ROLE_PROPERTY_NAME;


    public static void init(String roleAnonymous, String rolePropertyName, String rolePropertyActive,
        String roleAttributePatient,String roleAttributeDoctor, String roleAttributeChief, String roleAttributeAdmin){
        ROLE_ATTRIBUTE_PATIENT  = roleAttributePatient;
        ROLE_ATTRIBUTE_DOCTOR   = roleAttributeDoctor;
        ROLE_ATTRIBUTE_CHIEF    = roleAttributeChief;
        ROLE_ATTRIBUTE_ADMIN    = roleAttributeAdmin;
        ROLE_PROPERTY_ACTIVE    = rolePropertyActive;
        ROLE_PROPERTY_NAME      = rolePropertyName;
        ROLE_ANONYMOUS          = roleAnonymous;
    }

    public static String rolePropertyNAME(){
        return ROLE_PROPERTY_NAME;
    }
    public static String rolePropertyACTIVE(){
        return ROLE_PROPERTY_ACTIVE;
    }

    public static String roleAttributePATIENT(){
        return ROLE_ATTRIBUTE_PATIENT;
    }

    public static String roleAttributeDOCTOR(){
        return ROLE_ATTRIBUTE_DOCTOR;
    }

    public static String roleAttributeCHIEF(){
        return ROLE_ATTRIBUTE_CHIEF;
    }

    public static String roleAttributeADMIN(){
        return ROLE_ATTRIBUTE_ADMIN;
    }




    public static boolean isPatient(Set<String> userRoles) {
        return hasRoleAttribute(userRoles, ROLE_ATTRIBUTE_PATIENT);
    }

    public static boolean isDoctor(Set<String> userRoles){
        return hasRoleAttribute(userRoles, ROLE_ATTRIBUTE_DOCTOR);
    }

    public static boolean isChief(Set<String> userRoles){
        return hasRoleAttribute(userRoles, ROLE_ATTRIBUTE_CHIEF);
    }

    public static boolean isAdmin(Set<String> userRoles){
        return hasRoleAttribute(userRoles, ROLE_ATTRIBUTE_ADMIN);
    }


    public static void setRoleAttributes(Map<String,Map<String, Boolean>> roleAttributes){
        AppParams.roleAttributes = roleAttributes;
    }

    public static Map<String,Map<String, Boolean>> getRoleAttributes(){
        return roleAttributes;
    }


    public static Map<String, Boolean> getRolesActiveDefaults(Set<String> roles){
        return roleAttributes.entrySet().stream()
                .filter(entry -> roles.contains(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().get(ROLE_PROPERTY_ACTIVE)));
    }


    public static Set<String> rolesAdmin(){
        return getRolesByAttribute(ROLE_ATTRIBUTE_ADMIN);
    }

    public static Set<String> rolesChief(){
        return getRolesByAttribute(ROLE_ATTRIBUTE_CHIEF);
    }

    public static Set<String> rolesDoctor(){
        return getRolesByAttribute(ROLE_ATTRIBUTE_DOCTOR);
    }

    public static Set<String> rolesPatient(){
        return getRolesByAttribute(ROLE_ATTRIBUTE_PATIENT);
    }

    public static String roleAnonymous(){
        return ROLE_ANONYMOUS;
    }

    public static Set<String> filterUnknownRoles(Set<String> roles){
        return roles.stream().filter(r -> !roleAttributes.containsKey(r)).collect(Collectors.toSet());
    }


    private static Set<String> getRolesByAttribute(String role_attribute){
        return  roleAttributes.entrySet()
                .stream()
                .filter(entry -> (entry.getValue()).get(role_attribute))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    private static boolean hasRoleAttribute(Set<String> roles, String roleAttribute){
        final var rolesOfAttribute = roleAttributes.entrySet().stream()
                .filter(entry -> (entry.getValue()).get(roleAttribute))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return roles.stream().anyMatch(rolesOfAttribute::contains);
    }

}
