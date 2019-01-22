package me.pascal.emotebot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;

public class Util {

  public static void handleAttachment(Attachment attachment, Message message, String name, int cols)
      throws IOException {
    InputStream stream = attachment.getInputStream();
    String filename = attachment.getFileName();

    if (filename.endsWith(".gif")) {
      // Handle as Gif

    } else {
      // Handle as normal image
      BufferedImage buffImg = ImageIO.read(stream);
      handleImage(buffImg, message, name, cols);
    }
  }

  public static void handleURL(String url, Message message) {

  }

  public static void handleImage(BufferedImage image, Message message, String name, int cols) {

    // Determines the chunk width and height and amount
    int width = image.getWidth() / cols;
    int chunkWidth = width;
    int chunkHeight = width;
    int rows = ((Double.valueOf(image.getHeight()) / Double.valueOf(width)) % 1d == 0.0d)
        ? image.getHeight() / width
        : image.getHeight() / width + 1;
    int chunks = rows * cols;
    AtomicInteger count = new AtomicInteger(0);

    handleEmoteSpace(message, chunks, false);

    BufferedImage imgs[] = new BufferedImage[chunks]; // Image array to store image chunks
    StringBuilder b = new StringBuilder();

    for (int x = 0; x < rows; x++) {
      for (int y = 0; y < cols; y++) {
        // Initialize the image array with image chunks
        imgs[count.get()] = new BufferedImage(chunkWidth, chunkHeight, BufferedImage.TYPE_INT_ARGB);

        // Draws the image chunk
        Graphics2D gr = imgs[count.get()].createGraphics();
        gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x,
            chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
        gr.dispose();

        // Uploads the image chunk
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
          ImageIO.write(imgs[count.get()], "png", os);
        } catch (IOException e) {
          e.printStackTrace();
          message.getChannel().sendMessage("Error occured, check console for more info.").queue();
          return;
        }
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        try {
          Emote emote = message.getGuild().getController()
              .createEmote(name + (count.get() + 1), Icon.from(is)).complete();
          b.append(":" + emote.getName() + ":");
          if ((count.get() + 1) % cols == 0) {
            b.append("\n");
          }
          message.getChannel()
              .sendMessage("Uploaded " + (count.get() + 1) + "/" + chunks + "chunks.").complete();
        } catch (IOException e) {
          e.printStackTrace();
          message.getChannel().sendMessage("Error occured, check console for more info.").queue();
          return;
        }
        count.incrementAndGet();
      }
    }
    message.getChannel().sendMessage("`" + b.toString() + "`").queue();
  }

  public static void handleGif() {

  }

  private static final int MAX_EMOTES = 50;

  public static void handleEmoteSpace(Message message, int chunks, boolean animated) {
    Guild g = message.getGuild();
    int existent = (int) g.getEmotes().stream().filter(e -> e.isAnimated() == animated).count();
    int remaining = MAX_EMOTES - existent;
    if (remaining < chunks) {
      int toRemove = chunks - remaining;
      message.getChannel().sendMessage("Too many emotes, deleting " + toRemove + " emotes.");
      g.getEmotes().stream().filter(e -> e.isAnimated() == animated).limit(toRemove)
          .forEach(e -> e.delete().queue());
    }
  }
}
