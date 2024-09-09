package com.br.firesa.vpn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.firesa.vpn.security.SecurityUtil;
import com.br.firesa.vpn.service.UserService;
import com.br.firesa.vpn.service.VPNService;

@RestController
@RequestMapping("/vpn")
public class VPNController {

    @Autowired
    private VPNService vpnService;
    
    @Autowired
    private UserService userService;

    // Endpoint para iniciar a VPN
    @PostMapping("/start")
    public ResponseEntity<String> startVpn() {
        vpnService.startVpnConnection(userService.findByUsername(SecurityUtil.getCurrentUserName()));
        return ResponseEntity.ok("Servidor VPN iniciado.");
    }

    // Endpoint para parar a VPN
    @PostMapping("/stop")
    public ResponseEntity<String> stopVpn() {
        vpnService.stopVpnConnection();
        return ResponseEntity.ok("Servidor VPN parado.");
    }

    // Endpoint para verificar o status da VPN
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        boolean isRunning = vpnService.isVpnRunning();
        if (isRunning) {
            return ResponseEntity.ok("Servidor VPN está em execução.");
        } else {
            return ResponseEntity.ok("Servidor VPN está parado.");
        }
    }
}
