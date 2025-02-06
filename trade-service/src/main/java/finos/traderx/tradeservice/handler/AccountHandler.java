package finos.traderx.tradeservice.handler;

import finos.traderx.tradeservice.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AccountHandler {
    private static final Logger log = LoggerFactory.getLogger(AccountHandler.class);

    private final RestTemplate restTemplate;
    private final String accountServiceAddress;

    public AccountHandler(RestTemplate restTemplate,
                         @Value("${account.service.address}") String accountServiceAddress) {
        this.restTemplate = restTemplate;
        this.accountServiceAddress = accountServiceAddress;
    }

    /**
     * Validates an account by checking its existence in the account service.
     *
     * @param id The account ID to validate
     * @return true if the account exists and is valid, false otherwise
     */
    public boolean validateAccount(Integer id) {
        String url = this.accountServiceAddress + "/account/" + id;

        try {
            ResponseEntity<Account> response = this.restTemplate.getForEntity(url, Account.class);
            log.info("Validated account: {}", response.getBody());
            return true;
        } catch (HttpClientErrorException ex) {
            if (ex.getRawStatusCode() == 404) {
                log.info("Account {} not found in account service.", id);
            } else {
                log.error("Error validating account {}: {}", id, ex.getMessage());
            }
            return false;
        }
    }
}