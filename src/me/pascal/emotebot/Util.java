package me.pascal.emotebot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import me.pascal.emotebot.GifDecoder.GifImage;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;

public class Util {

  private static final int MAX_EMOTES = 50;

  public static void handleAttachment(Attachment attachment, Message message, String name, int cols)
      throws IOException {
    InputStream stream = attachment.getInputStream();
    String filename = attachment.getFileName();

    if (filename.endsWith(".gif")) {
      // Handle as Gif
      handleGif(stream, message, name, cols);
    } else {
      // Handle as normal image
      BufferedImage buffImg = ImageIO.read(stream);
      handleImage(buffImg, message, name, cols);
    }
  }

  public static void handleURL(String url, Message message, String name, int cols)
      throws MalformedURLException, IOException {

    URLConnection openConnection = new URL(url).openConnection();
    openConnection.addRequestProperty("User-Agent",
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
    InputStream is = openConnection.getInputStream();

    if (url.endsWith(".gif")) {
      // Handle as Gif
      handleGif(is, message, name, cols);
    } else {
      // Handle as normal image
      BufferedImage buffImg = ImageIO.read(is);
      handleImage(buffImg, message, name, cols);
    }

  }

  private static void handleImage(BufferedImage image, Message message, String name, int cols) {
    // Determines the chunk width and height and amount
    try {
      int width = image.getWidth() / cols;
      int chunkWidth = width;
      int chunkHeight = width;
      int rows = ((Double.valueOf(image.getHeight()) / Double.valueOf(width)) % 1d == 0.0d)
          ? image.getHeight() / width
          : image.getHeight() / width + 1;
      int chunks = rows * cols;
      AtomicInteger count = new AtomicInteger(0);

      if (!checks(message, chunks, cols, rows)) {
        return;
      }
      handleEmoteSpace(message, chunks, false);

      message.getChannel().sendMessage("Image successfully loaded, starting to upload").queue();

      BufferedImage imgs[] = new BufferedImage[chunks]; // Image array to store image chunks
      StringBuilder b = new StringBuilder();

      for (int x = 0; x < rows; x++) {
        for (int y = 0; y < cols; y++) {
          // Initialize the image array with image chunks
          imgs[count.get()] =
              new BufferedImage(chunkWidth, chunkHeight, BufferedImage.TYPE_INT_ARGB);

          // Draws the image chunk
          Graphics2D gr = imgs[count.get()].createGraphics();
          gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x,
              chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
          gr.dispose();

          // Uploads the image chunk
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          ImageIO.write(imgs[count.get()], "png", os);
          InputStream is = new ByteArrayInputStream(os.toByteArray());
          Emote emote = message.getGuild().getController()
              .createEmote(name + (count.get() + 1), Icon.from(is)).complete();
          b.append(":" + emote.getName() + ":");
          if ((count.get() + 1) % cols == 0) {
            b.append("\n");
          }
          message.getChannel()
              .sendMessage("Uploaded " + (count.get() + 1) + "/" + chunks + "chunks.").complete();
          count.incrementAndGet();
        }
      }
      message.getChannel().sendMessage("`" + b.toString() + "`").queue();
    } catch (Exception e) {
      message.getChannel()
          .sendMessage(
              "Error: " + e.getMessage() + " \n\nPlease contact me on twitter (@xImPascal)")
          .queue();
      e.printStackTrace();
      return;
    }
  }

  private static void handleGif(InputStream gifStream, Message message, String name, int cols) {
    try {
      List<ImageFrame> frames = new ArrayList<>();
      GifImage gif = GifDecoder.read(gifStream);
      for (int i = 0; i < gif.getFrameCount(); i++) {
        BufferedImage img = gif.getFrame(i);
        int delay = gif.getDelay(i);
        frames.add(new ImageFrame(img, delay));
      }

      int width = frames.get(0).getImage().getWidth() / cols;
      int chunkWidth = width;
      int chunkHeight = width;
      int rows = ((Double.valueOf(frames.get(0).getImage().getHeight()) / Double.valueOf(width))
          % 1d == 0.0d) ? frames.get(0).getImage().getHeight() / width
              : frames.get(0).getImage().getHeight() / width + 1;
      int chunks = rows * cols;
      int delay = frames.get(0).getDelay();

      if (!checks(message, chunks, cols, rows)) {
        return;
      }
      handleEmoteSpace(message, chunks, false);

      message.getChannel().sendMessage("Image successfully loaded, starting to upload").queue();

      // Split gif into multiple single image pieces
      List<BufferedImage[]> pieces = new ArrayList<>();

      for (int x = 0; x < rows; x++) {
        for (int y = 0; y < cols; y++) {
          BufferedImage[] imgs = new BufferedImage[frames.size()];
          for (int i = 0; i < frames.size(); i++) {
            BufferedImage image = frames.get(i).getImage();
            // Initialize the image array with image chunks
            imgs[i] = new BufferedImage(chunkWidth, chunkHeight, BufferedImage.TYPE_INT_ARGB);
            // Draws the image chunk
            Graphics2D gr = imgs[i].createGraphics();
            gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x,
                chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
            gr.dispose();

            // Uploads the image chunk
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(imgs[i], "png", os);
          }
          pieces.add(imgs);
        }
      }

      // Single layer pieces back to gifs and upload
      StringBuilder b = new StringBuilder();
      int count = 0;
      for (BufferedImage[] images : pieces) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        GifSequenceWriter writer =
            new GifSequenceWriter(ios, images[0].getType(), delay * 10, true);
        writer.writeToSequence(images[0]);
        for (int i = 1; i < images.length; i++) {
          BufferedImage nextImage = images[i];
          writer.writeToSequence(nextImage);
        }

        writer.close();
        ios.close();

        if (baos.toByteArray().length > 256000) {
          message.getChannel().sendMessage(
              "Error occured, chunk is larger than 256kb. Please change emote width or image resolution")
              .queue();
        }
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        Emote emote = message.getGuild().getController()
            .createEmote(name + (count + 1), Icon.from(is)).complete();
        b.append(":" + emote.getName() + ":");
        if ((count + 1) % cols == 0) {
          b.append("\n");
        }
        message.getChannel().sendMessage("Uploaded " + (count + 1) + "/" + chunks + "chunks.")
            .complete();
        count++;
      }

      // upload emotes
      message.getChannel().sendMessage("`" + b.toString() + "`").queue();

    } catch (Exception e) {
      message.getChannel()
          .sendMessage(
              "Error: " + e.getMessage() + " \n\nPlease contact me on twitter (@xImPascal)")
          .queue();
      e.printStackTrace();
      return;
    }
  }

  private static boolean checks(Message message, int chunks, int cols, int rows) {
    // check chunk amount
    if (chunks > MAX_EMOTES) {
      message.getChannel()
          .sendMessage(
              "Image will have too many chunks (" + cols + "x" + rows + "  total: " + chunks + ")")
          .queue();
      return false;
    }

    return true;
  }

  private static boolean handleEmoteSpace(Message message, int chunks, boolean animated) {
    Guild g = message.getGuild();

    int existent = (int) g.getEmotes().stream().filter(e -> e.isAnimated() == animated).count();
    int remaining = MAX_EMOTES - existent;
    if (remaining < chunks) {
      int toRemove = chunks - remaining;
      message.getChannel().sendMessage("Too many emotes, deleting " + toRemove + " emotes.")
          .queue();
      g.getEmotes().stream().filter(e -> e.isAnimated() == animated).limit(toRemove)
          .forEach(e -> e.delete().complete());
    }
    return true;

  }


}
