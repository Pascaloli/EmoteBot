package me.pascal.emotebot;

import java.io.IOException;
import java.util.List;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {

    Message message = event.getMessage();
    String content = message.getContentRaw();
    List<Attachment> attachments = message.getAttachments();
    User author = message.getAuthor();
    MessageChannel channel = message.getChannel();

    if (content.startsWith("-emote")) {

      if (!message.getGuild().getMember(author).hasPermission(Permission.MANAGE_EMOTES)) {
        message.getChannel()
            .sendMessage(":x: You are not allowed to access this command :x:. (Missing "
                + Permission.MANAGE_EMOTES.getName() + " permission)");
        return;
      }

      if (!message.getGuild().getMember(message.getJDA().getSelfUser())
          .hasPermission(Permission.MANAGE_EMOTES)) {
        message.getChannel()
            .sendMessage("BOT is missing permission: " + Permission.MANAGE_EMOTES.getName())
            .queue();;
        return;
      }


      String[] arguments = content.substring(7).split(" ");

      new Thread(() -> {
        try {
          if (arguments.length == 2 && attachments.size() > 0 && attachments.get(0).getFileName()
              .matches("([^\\s]+(\\.(?i)(jpg|png|jpeg|gif))$)")) {
            // Image as Attachment
            Util.handleAttachment(attachments.get(0), message, arguments[0],
                Integer.valueOf(arguments[1]));
          } else if (arguments.length == 3
              && arguments[2].matches("([^\\s]+(\\.(?i)(jpg|png|jpeg|gif))$)")) {
            // Image as url
            Util.handleURL(arguments[2], message, arguments[0], Integer.valueOf(arguments[1]));
          } else {
            // Invalid Argument / Invalid Format
            channel.sendMessage("Invalid Argument/Image format").queue();
          }
        } catch (Exception e) {
          message.getChannel()
              .sendMessage(
                  "Error: " + e.getMessage() + " \n\nPlease contact me on twitter (@xImPascal)")
              .queue();
          e.printStackTrace();
          return;
        }
      }).start();
    }

    super.onMessageReceived(event);
  }

}
