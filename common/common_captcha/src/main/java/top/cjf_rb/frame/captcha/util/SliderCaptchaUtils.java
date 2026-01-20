package top.cjf_rb.frame.captcha.util;

import lombok.extern.slf4j.Slf4j;
import top.cjf_rb.core.util.Identifiers;
import top.cjf_rb.frame.captcha.pojo.vo.SliderCaptchaVo;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class SliderCaptchaUtils {
    private static final int bgSize = 7;

    /**
     创建验证码
     */
    public static SliderCaptchaVo createCaptcha() throws IOException {
        int randomNum = ThreadLocalRandom.current()
                                         .nextInt(bgSize) + 1;
        String sourcePath = MessageFormat.format("static/captcha/slider-bg{0}.png", randomNum);
        // 原图图层
        BufferedImage sourceImage;
        try (InputStream inputStream = SliderCaptchaUtils.class.getClassLoader()
                                                               .getResourceAsStream(sourcePath)) {
            sourceImage = ImageIO.read(Objects.requireNonNull(inputStream));
        }

        // 生成随机坐标
        Random random = new Random();
        // 滑动拼图x坐标范围为 [(0+40),(260-40)]，y坐标范围为 [0,(160-40))
        int x = random.nextInt(sourceImage.getWidth() - 2 * ConfigConstant.SMALL_IMG_W) + ConfigConstant.SMALL_IMG_W;
        int y = random.nextInt(sourceImage.getHeight() - ConfigConstant.SMALL_IMG_H);

        // 小图图层
        BufferedImage smallImage;
        try (InputStream inputStream = SliderCaptchaUtils.class.getClassLoader()
                                                               .getResourceAsStream(sourcePath)) {
            smallImage = cutSmallImg(inputStream, x, y);
        }

        // 创建shape区域
        List<Shape> shapes = createSmallShape();
        // 创建用于小图阴影和大图凹槽的图层
        List<BufferedImage> effectImages = createEffectImg(shapes, smallImage);
        // 处理图片的边缘高亮及其阴影效果
        BufferedImage sliceImg = dealLightAndShadow(effectImages.get(0), shapes.getFirst());
        // 将灰色图当做水印印到原图上
        BufferedImage bgImg = createBgImg(effectImages.get(1), sourceImage, x, y);

        SliderCaptchaVo sliderCaptchaVo = new SliderCaptchaVo().setX(x)
                                                               .setY(y)
                                                               .setSliceImage(getImageBASE64(sliceImg));
        sliderCaptchaVo.setKey(Identifiers.nanoId())
                       .setImage(getImageBASE64(bgImg));
        return sliderCaptchaVo;
    }

    /**
     创建小块拼图

     @param sourceInputStream 背景原图输入流
     @param x                 小块拼图x坐标
     @param y                 小块拼图y坐标
     */
    public static BufferedImage cutSmallImg(InputStream sourceInputStream, int x, int y) throws IOException {
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("png");
        ImageReader render = iterator.next();
        ImageInputStream in = ImageIO.createImageInputStream(sourceInputStream);
        render.setInput(in, true);
        BufferedImage bufferedImage;
        try {
            ImageReadParam param = render.getDefaultReadParam();
            Rectangle rect = new Rectangle(x, y, ConfigConstant.SMALL_IMG_W, ConfigConstant.SMALL_IMG_H);
            param.setSourceRegion(rect);
            bufferedImage = render.read(0, param);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return bufferedImage;
    }

    /**
     创建一个灰度化图层， 将生成的小图，覆盖到该图层，使其灰度化，用于作为一个水印图

     @param smallImage 小图
     @param originImg  原图
     @param x          x坐标
     @param y          y坐标
     */
    public static BufferedImage createBgImg(BufferedImage smallImage, BufferedImage originImg, int x, int y) {
        // 将灰度化之后的图片，整合到原有图片上
        Graphics2D graphics2d = originImg.createGraphics();
        graphics2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.6F));
        graphics2d.drawImage(smallImage, x, y, null);
        // 释放
        graphics2d.dispose();
        return originImg;
    }

    /**
     处理小图,在4个方向上随机找到2个方向添加凸出
     */
    private static List<Shape> createSmallShape() {
        int face1 = ThreadLocalRandom.current()
                                     .nextInt(4);
        int face2;
        // 使凸出1 与 凸出2不在同一个方向
        do {
            face2 = ThreadLocalRandom.current()
                                     .nextInt(4);
        } while (face1 == face2);

        Shape shape1 = createShape(face1, 0);
        Shape shape2 = createShape(face2, 0);
        // 因为后边图形需要生成阴影，所以生成的小图shape + 阴影宽度 = 灰度化的背景小图shape（即大图上的凹槽）
        Shape bigShape1 = createShape(face1, ConfigConstant.SHADOW);
        Shape bigShape2 = createShape(face2, ConfigConstant.SHADOW);

        // 生成中间正方体Shape,(具体边界 + 弧半径 = x坐标位)
        int xStart = ConfigConstant.CIRCLE_R + ConfigConstant.LIGHT;
        int yStart = ConfigConstant.CIRCLE_R + ConfigConstant.LIGHT;
        Shape center = new Rectangle2D.Float(xStart, yStart, ConfigConstant.SQUARE_W, ConfigConstant.SQUARE_H);
        Shape bigCenter = new Rectangle2D.Float(xStart - (float) ConfigConstant.SHADOW / 2,
                                                yStart - (float) ConfigConstant.SHADOW / 2,
                                                ConfigConstant.SQUARE_W + ConfigConstant.SHADOW,
                                                ConfigConstant.SQUARE_H + ConfigConstant.SHADOW);

        // 合并Shape
        Area area = new Area(center);
        area.add(new Area(shape1));
        area.add(new Area(shape2));
        // 合并大Shape
        Area bigArea = new Area(bigCenter);
        bigArea.add(new Area(bigShape1));
        bigArea.add(new Area(bigShape2));

        List<Shape> list = new ArrayList<>();
        list.add(area);
        list.add(bigArea);
        return list;
    }

    /**
     创建圆形区域,半径为5 由于小图边缘阴影的存在,坐标需加上此宽度

     @param type 0=上，1=左，2=下，3=右
     @param size 圆外接矩形边长
     */
    private static Shape createShape(int type, int size) {
        if (type < 0 || type > 3) {
            type = 0;
        }
        int x;
        int y;
        if (type == 0) {
            x = ConfigConstant.SQUARE_W / 2 + ConfigConstant.SHADOW;
            y = ConfigConstant.SHADOW;
        } else if (type == 1) {
            x = ConfigConstant.SHADOW;
            y = ConfigConstant.SQUARE_H / 2 + ConfigConstant.SHADOW;
        } else if (type == 2) {
            x = ConfigConstant.SQUARE_W / 2 + ConfigConstant.SHADOW;
            y = ConfigConstant.SQUARE_H + ConfigConstant.SHADOW;
        } else {
            x = ConfigConstant.SQUARE_W + ConfigConstant.SHADOW;
            y = ConfigConstant.SQUARE_H / 2 + ConfigConstant.SHADOW;
        }
        int halfSize = size / 2;
        int wSide = ConfigConstant.CIRCLE_D + size;
        return new Arc2D.Float(x - halfSize, y - halfSize, wSide, wSide, 90 * type, 190, Arc2D.CHORD);
    }

    /**
     创建用于小图阴影和大图凹槽的图层

     @param shapes   形状
     @param smallImg 小图原图
     @return 创建的阴影图层
     */
    private static List<BufferedImage> createEffectImg(List<Shape> shapes, BufferedImage smallImg) {
        Shape area = shapes.get(0);
        Shape bigArea = shapes.get(1);
        // 创建图层用于处理小图的阴影
        BufferedImage bfm1 = new BufferedImage(ConfigConstant.SMALL_IMG_W, ConfigConstant.SMALL_IMG_H,
                                               BufferedImage.TYPE_INT_ARGB);
        // 创建图层用于处理大图的凹槽
        BufferedImage bfm2 = new BufferedImage(ConfigConstant.SMALL_IMG_W, ConfigConstant.SMALL_IMG_H,
                                               BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < ConfigConstant.SMALL_IMG_W; i++) {
            for (int j = 0; j < ConfigConstant.SMALL_IMG_W; j++) {
                if (area.contains(i, j)) {
                    bfm1.setRGB(i, j, smallImg.getRGB(i, j));
                }
                if (bigArea.contains(i, j)) {
                    bfm2.setRGB(i, j, Color.black.getRGB());
                }
            }
        }
        List<BufferedImage> list = new ArrayList<>();
        list.add(bfm1);
        list.add(bfm2);
        return list;
    }

    /**
     处理小图的边缘灯光及其阴影效果

     @param bfm   图层
     @param shape 形状
     @return 处理后的图层
     */
    private static BufferedImage dealLightAndShadow(BufferedImage bfm, Shape shape) {
        // 创建新的透明图层，该图层用于边缘化阴影， 将生成的小图合并到该图上
        BufferedImage buffimg = ((Graphics2D) bfm.getGraphics()).getDeviceConfiguration()
                                                                .createCompatibleImage(ConfigConstant.SMALL_IMG_W,
                                                                                       ConfigConstant.SMALL_IMG_H,
                                                                                       Transparency.TRANSLUCENT);
        Graphics2D graphics2d = buffimg.createGraphics();
        Graphics2D g2 = (Graphics2D) bfm.getGraphics();
        // 原有小图，边缘亮色处理
        paintBorderGlow(g2, shape);
        // 新图层添加阴影
        paintBorderShadow(graphics2d, shape);
        graphics2d.drawImage(bfm, 0, 0, null);
        return buffimg;
    }

    /**
     处理边缘亮色

     @param g2        画笔
     @param clipShape 形状
     */
    private static void paintBorderGlow(Graphics2D g2, Shape clipShape) {
        int gw = ConfigConstant.LIGHT * 2;
        for (int i = gw; i >= 2; i -= 2) {
            float pct = (float) (gw - i) / (gw - 1);
            Color mixHi = getMixedColor(ConfigConstant.CIRCLE_GLOW_I_H, pct, ConfigConstant.CIRCLE_GLOW_O_H,
                                        1.0f - pct);
            Color mixLo = getMixedColor(ConfigConstant.CIRCLE_GLOW_I_L, pct, ConfigConstant.CIRCLE_GLOW_O_L,
                                        1.0f - pct);
            g2.setPaint(new GradientPaint(0.0f, 35 * 0.25f, mixHi, 0.0f, 35, mixLo));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, pct));
            g2.setStroke(new BasicStroke(i));
            g2.draw(clipShape);
        }
    }

    /**
     处理阴影
     */
    private static void paintBorderShadow(Graphics2D g1, Shape clipShape) {
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int sw = ConfigConstant.SHADOW * 2;
        for (int i = sw; i >= 2; i -= 2) {
            float pct = (float) (sw - i) / (sw - 1);
            // pct<03. 用于去掉阴影边缘白边， pct>0.8用于去掉过深的色彩， 如果使用Color.lightGray. 可去掉pct>0.8
            if (pct < 0.3 || pct > 0.8) {
                continue;
            }
            g1.setColor(getMixedColor(new Color(54, 54, 54), pct, Color.WHITE, 1.0f - pct));
            g1.setStroke(new BasicStroke(i));
            g1.draw(clipShape);
        }
    }

    public static String getImageBASE64(BufferedImage image) throws IOException {
        byte[] imageByte;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bao);
        imageByte = bao.toByteArray();
        String base64Image = new String(Base64.getEncoder()
                                              .encode(imageByte), StandardCharsets.UTF_8);
        base64Image = base64Image.replaceAll("[\r\n]", "");
        return base64Image;
    }

    private static Color getMixedColor(Color c1, float pct1, Color c2, float pct2) {
        float[] clr1 = c1.getComponents(null);
        float[] clr2 = c2.getComponents(null);
        for (int i = 0; i < clr1.length; i++) {
            clr1[i] = (clr1[i] * pct1) + (clr2[i] * pct2);
        }
        return new Color(clr1[0], clr1[1], clr1[2], clr1[3]);
    }

    static class ConfigConstant {

        /**
         小图的宽 (SQUARE_W + CIRCLE_R * 2 + LIGHT * 2)
         */
        public static final int SMALL_IMG_W = 52;

        /**
         小图的高 (SQUARE_H + CIRCLE_R * 2 + LIGHT * 2)
         */
        public static final int SMALL_IMG_H = 52;

        /**
         正方形的宽
         */
        public static final int SQUARE_W = 32;

        /**
         正方形的高
         */
        public static final int SQUARE_H = 32;

        /**
         小图突出圆的直径 (CIRCLE_D * 2)
         */
        public static final int CIRCLE_D = 12;

        /**
         小图突出圆的半径
         */
        public static final int CIRCLE_R = 6;

        /**
         小图阴影宽度
         */
        public static final int SHADOW = 3;

        /**
         小图边缘高亮宽度
         */
        public static final int LIGHT = 4;

        /**
         小图边缘高亮颜色
         */
        public static final Color CIRCLE_GLOW_I_H = new Color(255, 255, 255, 148);
        public static final Color CIRCLE_GLOW_I_L = new Color(255, 255, 255);
        public static final Color CIRCLE_GLOW_O_H = new Color(255, 255, 255, 124);
        public static final Color CIRCLE_GLOW_O_L = new Color(255, 255, 255);

    }

}
