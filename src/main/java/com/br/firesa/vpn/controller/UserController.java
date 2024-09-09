package com.br.firesa.vpn.controller;

//@RestController
//@RequestMapping("/user")
//public class UserController {
//	
//	@Autowired
//	private UserService userService;
//
//	@Autowired
//	private MapConverter mapConverter;
//
//	// Login com validação e resposta adequada
//	@PostMapping("/login")
//    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
//		LoginResponse response = userService.login(loginRequest);
//		return ResponseEntity.ok(mapConverter.toJsonMap(response)); // Retorna 200 OK
//    }
//
//	// Registro de usuário com validação e retorno do status correto
//	@PostMapping("/register")
//    public ResponseEntity<?> register(@Valid @RequestBody User user) {
//		User registeredUser = userService.register(user);
//		return ResponseEntity.status(HttpStatus.CREATED).body(mapConverter.toJsonMap(registeredUser)); // Retorna 201 Created
//	}
//}