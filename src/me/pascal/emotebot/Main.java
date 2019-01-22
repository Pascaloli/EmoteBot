package me.pascal.emotebot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Emote;

public class Main {

  private JDA jda;


  public static void main(String[] args) {
    new Main().main();
  }

  public void main() {
    try {
      this.jda = new JDABuilder(AccountType.BOT)
          .setToken("")
          .addEventListener(new MessageListener()).build().awaitReady();
    } catch (LoginException | InterruptedException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

}
