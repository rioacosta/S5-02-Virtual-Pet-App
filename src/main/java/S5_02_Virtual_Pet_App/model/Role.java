package S5_02_Virtual_Pet_App.model;

public enum Role {
    USER, ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
