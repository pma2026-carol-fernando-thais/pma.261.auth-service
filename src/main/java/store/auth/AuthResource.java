package store.auth;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import store.account.AccountOut;

@RestController
public class AuthResource implements AuthController {

    private static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Override
    public ResponseEntity<Void> login(LoginIn in) {
        final TokenOut out = authService.login(in.email(), in.password());
        return ResponseEntity
            .ok()
            .header(
                HttpHeaders.SET_COOKIE,
                buildTokenCookie(out.token(), authService.getDuration()).toString()
            )
            .build()
        ;
    }
    
    @Override
    public ResponseEntity<Void> logout() {
        return ResponseEntity
            .ok()
            .header(
                HttpHeaders.SET_COOKIE,
                buildTokenCookie(null, 0l).toString()
            )
            .build()
        ;
    }

    @Override
    public ResponseEntity<Void> register(RegisterIn in) {
        authService.register(in);
        return ResponseEntity.created(null).build();
    }

    @Override
    public ResponseEntity<AccountOut> whoIAm(String idAccount) {
        return ResponseEntity.ok(authService.whoIAm(idAccount));
    }

    @Override
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }

    private ResponseCookie buildTokenCookie(String content, Long duration) {
        return ResponseCookie.from(AuthController.AUTH_COOKIE_TOKEN, content)
            .httpOnly(authService.getHttpOnly())
            .sameSite("None")
            .secure(true)
            .path("/")
            .maxAge(Duration.ofMillis(duration))
            .build();
    }

    @Override
    public ResponseEntity<Map<String, String>> solveToken(TokenOut map) {
        logger.debug("Chegou: " + map.toString());
        final String idAccount = authService.solveToken(map.token());
        return ResponseEntity.ok(Map.of(
            "idAccount", idAccount
        ));
    }

}
