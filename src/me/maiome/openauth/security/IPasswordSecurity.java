package me.maiome.openauth.security;

public interface IPasswordSecurity {

    // fields
    String name = null;
    int rank = 0;

    // getters
    String getName();
    int getRank();

    // activation checker
    boolean isActive();

    // explains why the password failed validation: ends "Your registration was cancelled because "
    String explain();

    // password strength validator
    boolean validate(String password);

}