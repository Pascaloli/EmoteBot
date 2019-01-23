package me.pascal.emotebot;

import java.io.IOException;
import java.util.List;
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
      String[] arguments = content.substring(7).split(" ");

      if (arguments.length == 2 && attachments.size() > 0
          && attachments.get(0).getFileName().matches("([^\\s]+(\\.(?i)(jpg|png|jpeg|gif))$)")) {
        // Image as Attachment
        try {
          Util.handleAttachment(attachments.get(0), message, arguments[0],
              Integer.valueOf(arguments[1]));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else if (arguments.length == 3
          && arguments[2].matches("([^\\s]+(\\.(?i)(jpg|png|jpeg|gif))$)")) {
        // Image as url
        try {
          Util.handleURL(arguments[2], message, arguments[0], Integer.valueOf(arguments[1]));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        // Invalid Argument / Invalid Format
        channel.sendMessage("Invalid Argument/Image format").queue();
      }

    }

    super.onMessageReceived(event);
  }

}
