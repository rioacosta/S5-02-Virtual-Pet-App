package S502VirtualPetApp.model;

public enum Role {
    USER, ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
