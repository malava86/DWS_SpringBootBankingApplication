package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountRelatedException;
import com.dws.challenge.exception.MoneyRelatedException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;
	
	private final EmailNotificationService emailNotificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository,EmailNotificationService emailNotificationService) {
		this.accountsRepository = accountsRepository;
		this.emailNotificationService = emailNotificationService;
	}

	public void createAccount(Account account) throws AccountRelatedException, Exception {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * @param fromAccId
	 * @param toAccId
	 * @param amount
	 */
	public void transferMoney(String fromAccId, String toAccId, BigDecimal amount) throws Exception {
		
		Account accountFrom = getAccount(fromAccId);
		Account accountTo = getAccount(toAccId);
		
		if(fromAccId.compareTo(toAccId) !=0 ) {
			
		Object lock1 = fromAccId.compareTo(toAccId) < 0  ? accountFrom : accountTo;
		Object lock2 = fromAccId.compareTo(toAccId) > 0  ? accountFrom : accountTo;

		//To prevent a deadlock, always have to lock the Objects in the following order
		synchronized (lock1) {
			synchronized (lock2) {
				deposit(toAccId, amount);
				withdraw(fromAccId, amount);
			}
		}
		
		}else {
			throw new MoneyRelatedException("Both Account Should not be same...!!!");
		}
		
	}

	/**
	 * @param accountId
	 * @param amount
	 */
	public void withdraw(String accountId, BigDecimal amount) {
		
		Account actualAccount = accountsRepository.getAccounts().get(accountId);
		String message = "Acc Balance or Money value should not be negative...!!!";
		checkValidations(actualAccount,amount,message);
		withdrawValidations(actualAccount,amount);
		actualAccount.setBalance(actualAccount.getBalance().subtract(amount));
		accountsRepository.getAccounts().putIfAbsent(actualAccount.getAccountId(), actualAccount);
		emailNotificationService.notifyAboutTransfer(actualAccount, " " + amount + " transfer from " + accountId);
	}
	
	/**
	 * @param accountId
	 * @param amount
	 */
	public void deposit(String accountId, BigDecimal amount) {
		Account actualAccount = accountsRepository.getAccounts().get(accountId);
		String message = "Deposit or Money value should not be negative...!!!";
		checkValidations(actualAccount,amount,message);
		actualAccount.setBalance(actualAccount.getBalance().add(amount));
		accountsRepository.getAccounts().putIfAbsent(actualAccount.getAccountId(), actualAccount);
		emailNotificationService.notifyAboutTransfer(actualAccount, " " + amount + " transfer to " + accountId);
	}
	
	public List<Account> listAllAccounts() {

		List<Account> listAccounts = new ArrayList<Account>();
		for (Map.Entry<String, Account> entry : accountsRepository.getAccounts().entrySet()) {
			listAccounts.add(entry.getValue());
		}
		return listAccounts;
	}
	
	private void checkValidations(Account actualAccount, BigDecimal amount,String message) {
		
		if (actualAccount.getBalance().signum() == -1 || ( amount!=null && amount.signum() == -1 )) {
			throw new MoneyRelatedException(message);
		}
	}
	
	private void withdrawValidations(Account actualAccount, BigDecimal amount) {
		
		if (amount.compareTo(actualAccount.getBalance()) > 0) {
			throw new MoneyRelatedException("Money value should not be greater then the Account Balance...!!!");
		}
	}
}