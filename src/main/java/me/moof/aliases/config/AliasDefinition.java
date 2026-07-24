package me.moof.aliases.config;

public class AliasDefinition {
    private String name;
    private String target;
    private int permissionLevel;
    private String description;

    public AliasDefinition() {
    }

    public AliasDefinition(String name, String target, int permissionLevel, String description) {
        this.name = name;
        this.target = target;
        this.permissionLevel = permissionLevel;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
