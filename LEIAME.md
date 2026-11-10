# Atividade 03 (Cliente) + Bicicleta — arquivos novos

Copie a pasta `src/` para a raiz do projeto (ela só adiciona arquivos).

## Ajustes necessários no que já existe
1. pom.xml — adicionar Spring Security (não está no pom do repositório):
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-security</artifactId>
   </dependency>
2. Habilitar `@EnableMethodSecurity` na classe de configuração de segurança (para o @PreAuthorize).
3. `UsuarioRepository` precisa de: `Optional<Usuario> findByEmail(String email);`
4. `Usuario` precisa de `getId()` e `getEmail()`; o "username" da autenticação deve ser o e-mail
   e o perfil admin deve virar a authority `ROLE_ADMIN`.
5. Auditoria: `@EnableJpaAuditing` + um `AuditorAware<String>` (os mesmos usados em Usuario).
   Se os campos de auditoria de Usuario têm outros nomes, renomeie em Cliente/Bicicleta.
6. SpringDocOpenApiConfig — declarar o esquema "security" usado nos @SecurityRequirement, ex. (Basic):
   .components(new Components().addSecuritySchemes("security",
       new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
   (para JWT use .scheme("bearer").bearerFormat("JWT"))
