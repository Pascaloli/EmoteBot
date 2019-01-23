package me.pascal.emotebot;

import java.awt.image.BufferedImage;

public class ImageFrame {
  private final int delay;
  private BufferedImage image;

  public ImageFrame(BufferedImage image, int delay) {
    this.image = image;
    this.delay = delay;
  }

  public BufferedImage getImage() {
    return image;
  }

  public void setImage(BufferedImage newImage) {
    this.image = newImage;
  }

  public int getDelay() {
    return delay;
  }
}
