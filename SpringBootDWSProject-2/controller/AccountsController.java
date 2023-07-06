package com.dws.challenge.controller;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountRelatedException;
import com.dws.challenge.service.AccountsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account)throws Exception {
    //log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (AccountRelatedException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>("New Account got created successfully...!!!",HttpStatus.OK);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    //log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }
  
  @GetMapping
  public List<Account> listAllAccounts(){
	  return this.accountsService.listAllAccounts();
  }
  
  @GetMapping("/deposit/{accountId}/{amount}")
  public ResponseEntity<Object> deposit(@PathVariable String accountId, @PathVariable BigDecimal amount)throws Exception  {
	  try {
	  this.accountsService.deposit(accountId,amount);
	  } catch (Exception ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	  }
	  return new ResponseEntity<>("The Money "+amount+" deposited successfully...!!!",HttpStatus.OK);
  }
  
  @GetMapping("/withdraw/{accountId}/{amount}")
  public ResponseEntity<Object> withdraw(@PathVariable String accountId, @PathVariable BigDecimal amount)throws Exception  {
	  try {
	  this.accountsService.withdraw(accountId,amount);
	  } catch (Exception ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	  }
	  return new ResponseEntity<>("The Money "+amount+" withdrawn successfully...!!!",HttpStatus.OK);
  }
  
  @GetMapping("/transfer/{fromAccId}/{toAccId}/{amount}")
  public ResponseEntity<Object> moneyTransfer(@PathVariable String fromAccId, @PathVariable String toAccId, @PathVariable BigDecimal amount)throws Exception {
	  try {
	  this.accountsService.transferMoney(fromAccId, toAccId,amount);
	  } catch (Exception ex) {
			return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	  }
	 return new ResponseEntity<>("The Money "+amount+" transfer had successfully...!!!",HttpStatus.OK);
  }


}
