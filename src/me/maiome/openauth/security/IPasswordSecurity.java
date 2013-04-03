package me.maiome.openauth.security;

public interface IPasswordSecurity {

    // fields
    String name = null;

    // getters
    String getName();

    // activity checker checker
    boolean isActive();

    // explains why the password failed validation: finishes "Your registration was cancelled because "
    String explain();

    // password strength validator
    boolean validate(String password);

}