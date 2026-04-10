package store.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import store.account.AccountController;
import store.account.AccountIn;
import store.account.AccountOut;

@Service
public class AuthService {

    private static Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${store.jwt.duration}")
    private Long duration;

    @Value("${store.jwt.httpOnly}")
    private Boolean httpOnly;

    @Autowired
    private AccountController accountController;

    @Autowired
    private JwtService jwtService;

    public void register(RegisterIn in) {

        // record the input into account
        accountController.create(AccountIn.builder()
            .name(in.name())
            .email(in.email())
            .password(in.password())
            .build()
        );

    }

    public TokenOut login(String email, String password) {
        // search account
        final AccountOut account = accountController.findByEmailAndPassword(
            AccountIn.builder()
                .email(email)
                .password(password)
                .build()
        ).getBody();

        return TokenOut.builder()
            .token(jwtService.generate(account, duration))
            .build();
    }

    public Long getDuration() {
        return duration;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public String solveToken(String token) {
        logger.debug(token);
        // devolver o idAccount
        return jwtService.getId(token);
    }

    public AccountOut whoIAm(String idAccount) {
        return accountController.findById(idAccount).getBody();
    }
    
}
