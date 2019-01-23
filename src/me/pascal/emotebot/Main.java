package me.pascal.emotebot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class Main {

  private JDA jda;
  private File configFile = new File("config.properties");
  private String token;


  public static void main(String[] args) {
    new Main().main();
  }

  public void main() {
    // create config file if not existing
    if (!configFile.exists()) {
      try {
        configFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // load config file
    loadToken();
  }

  public void startJDA() {
    try {
      this.jda = new JDABuilder(AccountType.BOT).setToken(this.token)
          .addEventListener(new MessageListener()).build().awaitReady();
    } catch (LoginException | InterruptedException e) {
      System.out.println(e.getMessage());
      if (e.getMessage().equals("The provided token is invalid!")) {
        setToken(requestToken());
        loadToken();
      } else {
        e.printStackTrace();
      }
    }
  }

  public void loadToken() {
    try {
      FileReader reader = new FileReader(configFile);
      Properties props = new Properties();
      props.load(reader);

      String token = props.getProperty("token");
      reader.close();

      if (token == null || token.isEmpty()) {
        setToken(requestToken());
        loadToken();
        return;
      } else {
        this.token = token;
      }

      // start bot after loading the token
      startJDA();

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public String requestToken() {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Please enter the bots token: ");
      return br.readLine();
    } catch (Exception e) {
      e.printStackTrace();
      requestToken();
    }
    return null;
  }

  public void setToken(String token) {
    try {
      Properties props = new Properties();
      props.setProperty("token", token);
      FileWriter writer = new FileWriter(configFile);
      props.store(writer, null);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      requestToken();
    }
  }

}
