package com.br.firesa.vpn.config;

public enum ResourcePermission {

	// ADM
	ADMIN("ADMIN"),

	// AppUser
	USER("USER");
	
	private final String authority;

    ResourcePermission(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

}
