package com.bikerental.dto;

import com.bikerental.model.Role;

public record LoginResponse(String token, String username, Role role) {}
