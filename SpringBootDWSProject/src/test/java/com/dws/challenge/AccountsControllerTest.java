package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;
  
  @Mock
  private AccountsService mockAccountsService;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    //accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-111\",\"balance\":2000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-111");
    assertThat(account.getAccountId()).isEqualTo("Id-111");
    assertThat(account.getBalance()).isEqualByComparingTo("2000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-555\",\"balance\":3000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-555\",\"balance\":3000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountWithNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":2000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountWithNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-111\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountWithNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountWithNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-111\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountWithEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("555.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":555.45}"));
  }
  
  @Test
  void transferMoney() throws Exception 
  {
	  BigDecimal amount =new BigDecimal(5000.00);
	   Account account = new Account("888", new BigDecimal("4000.00"));
	   Account account1 = new Account("999", new BigDecimal("6000.00"));
	   this.accountsService.createAccount(account);
	   this.accountsService.createAccount(account1);
	   Mockito.doNothing().when(this.mockAccountsService).transferMoney(Mockito.anyString(),Mockito.anyString(),Mockito.any());
	   this.mockMvc.perform(get("/v1/accounts/" + account.getAccountId()+"/"+account1.getAccountId()+"/"+ amount))
	      .andExpect(status().isOk())
	      .andExpect(
	        content().string("transfer done successfully"));
	   this.mockAccountsService.transferMoney(account.getAccountId(), account1.getAccountId(), amount);
	   verify(this.mockAccountsService,times(1)).transferMoney(account.getAccountId(), account1.getAccountId(), amount);
  }
  
	@Test
	void transferMoneyNegativeCase() throws Exception {
		BigDecimal amount = new BigDecimal(-5000.00);
		Account account = new Account("888", new BigDecimal("10000.00"));
		Account account1 = new Account("999", new BigDecimal("5000.00"));
		this.accountsService.createAccount(account);
		this.accountsService.createAccount(account1);
		assertThrows(Exception.class, () -> {
			doThrow().when(this.mockAccountsService).transferMoney(Mockito.anyString(), Mockito.anyString(),
					Mockito.any());
		});
		this.mockAccountsService.transferMoney(account.getAccountId(), account1.getAccountId(), amount);
		verify(this.mockAccountsService, times(1)).transferMoney(account.getAccountId(), account1.getAccountId(),
				amount);
		this.mockMvc
				.perform(get("/v1/accounts/" + account.getAccountId() + "/" + account1.getAccountId() + "/" + amount))
				.andExpect(status().isBadRequest()).andExpect(content().string("Amount transfer can't be negative"));
	}
	
	
	@Test
	void transferMoneyMoreThanBalance_case() throws Exception {
		BigDecimal amount = new BigDecimal(5000.00);
		Account account = new Account("888", new BigDecimal("1000.00"));
		Account account1 = new Account("999", new BigDecimal("5000.00"));
		this.accountsService.createAccount(account);
		this.accountsService.createAccount(account1);
		assertThrows(Exception.class, () -> {
			doThrow().when(this.mockAccountsService).transferMoney(Mockito.anyString(), Mockito.anyString(),
					Mockito.any());
		});
		this.mockAccountsService.transferMoney(account.getAccountId(), account1.getAccountId(), amount);
		verify(this.mockAccountsService, times(1)).transferMoney(account.getAccountId(), account1.getAccountId(),
				amount);
		this.mockMvc
				.perform(get("/v1/accounts/" + account.getAccountId() + "/" + account1.getAccountId() + "/" + amount))
				.andExpect(status().isBadRequest()).andExpect(content().string("The amount is greater than account balance"));
	}
  
	@Test
	void transferMoneyAccountNotExist() throws Exception {
		BigDecimal amount = new BigDecimal(5000.00);
		Account account = new Account("888", new BigDecimal("1000.00"));
		this.accountsService.createAccount(account);
		assertThrows(Exception.class, () -> {
			doThrow().when(this.mockAccountsService).transferMoney(Mockito.anyString(), Mockito.anyString(),
					Mockito.any());
		});
		this.mockAccountsService.transferMoney(account.getAccountId(), "777", amount);
		verify(this.mockAccountsService, times(1)).transferMoney(account.getAccountId(), "777",
				amount);
		this.mockMvc
				.perform(get("/v1/accounts/" + account.getAccountId() + "/" + "666" + "/" + amount))
				.andExpect(status().isBadRequest()).andExpect(content().string("The accountTo ID 666 to transfer does not exist"));
	}
}
