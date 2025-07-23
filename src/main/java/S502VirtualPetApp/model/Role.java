package S502VirtualPetApp.model;

public enum Role {
    USER, ADMIN, ROLE_ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
